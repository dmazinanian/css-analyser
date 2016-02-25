package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.CSSModelObject;
import ca.concordia.cssanalyser.cssmodel.CSSOrigin;
import ca.concordia.cssanalyser.cssmodel.CSSSource;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;


public abstract class Selector extends CSSModelObject  {

	private LocationInfo selectorNameLocationInfo;
	protected StyleSheet parentStyleSheet;
	protected Set<MediaQueryList> mediaQueryLists;
	protected Map<Declaration, Integer> declarations;
	protected CSSSource source = CSSSource.EXTERNAL;
	protected CSSOrigin origin = CSSOrigin.AUTHOR;
	private Selector originalSelector;
	
	public Selector() {
		this(new LocationInfo());
	}
	
	public Selector(LocationInfo locationInfo) {
		this.locationInfo = locationInfo;
		declarations = new LinkedHashMap<>();
		mediaQueryLists = new LinkedHashSet<>();
	}
	
	/**
	 * Gets the parent style sheet of this Selector 
	 * @return the parentStyleSheet
	 */
	public StyleSheet getParentStyleSheet() {
		return parentStyleSheet;
	}

	/** 
	 * Sets the parent style sheet of this Selector.
	 * If the selector is not currently in the StyleSheet,
	 * it will add it to the StyleSheet.
	 * @param parentStyleSheet the parentStyleSheet to set
	 */
	public void setParentStyleSheet(StyleSheet parentStyleSheet) {
		this.parentStyleSheet = parentStyleSheet;
		if (!parentStyleSheet.containsSelector(this))
			parentStyleSheet.addSelector(this);
	}
		
	public LocationInfo getSelectorNameLocationInfo() {
		return this.selectorNameLocationInfo;
	}
	
	public void setSelectorNameLocationInfo(LocationInfo locationInfo) {
		this.selectorNameLocationInfo = locationInfo;
	}
	
	public void addDeclaration(Declaration declaration) {
		if (!declarations.containsKey(declaration))
			declarations.put(declaration, declarations.size() + 1);
		declaration.setSelector(this);
	}
	
	public int getNumberOfDeclarations() {
		return declarations.size();
	}
		
	public Iterable<Declaration> getDeclarations() {
		return declarations.keySet();
	}
	
	public boolean containsDeclaration(Declaration declaration) {
		return this.declarations.containsKey(declaration);
	}	
	
	public int getDeclarationNumber(Declaration declaration) {
		if (!containsDeclaration(declaration))
			return -1;
		return declarations.get(declaration);
	}
	
	/**
	 * Returns the selector number in the style sheet
	 * @return
	 */
	public int getSelectorNumber() {
		return parentStyleSheet.getSelectorNumber(this);
	}
	
	public boolean selectorEquals(Selector otherSelector) {
		return selectorEquals(otherSelector, true);
	}
	
	/**
	 * The equals() method for different selectors have different meanings
	 * but in all of them selectors should be exactly the same and are appeared
	 * in the same location in the file. This method provides a way to compare
	 * two selectors to see weather they are equal, regardless of their
	 * definition location
	 * @param otherSelector
	 * @param considerMediaQueryLists
	 * @return
	 */
	public abstract boolean selectorEquals(Selector otherSelector, boolean considerMediaQueryLists);
	
	public abstract Selector clone();
	
	public abstract String getXPath() throws UnsupportedSelectorToXPathException;
	
	@SuppressWarnings("serial")
	public static class UnsupportedSelectorToXPathException extends Exception {
		private Selector selector;
		public UnsupportedSelectorToXPathException(Selector selector) {
			this.selector = selector;
		}
		public Selector getSelector() {
			return selector;
		}
	}

	/**
	 * Removes a declaration from list of declarations
	 * @param declaration
	 */
	public void removeDeclaration(Declaration declaration) {
		this.declarations.remove(declaration);
		// Update the numbers associated with declarations
		int i = 0;
		for (Declaration d : declarations.keySet())
			declarations.put(d, i++);
		declaration.setSelector(null);
	}

	/**
	 * Copies current BaseSelector without any declaration
	 * @return
	 */
	public Selector copyEmptySelector() {
		Selector newEmptySelector = this.clone();
		newEmptySelector.declarations.clear();
		return newEmptySelector;
	}
	
	public void addMediaQueryLists(Iterable<MediaQueryList> currentMediaQueryLists) {
		for (MediaQueryList currentMediaQueryList : currentMediaQueryLists)
			mediaQueryLists.add(currentMediaQueryList.clone());
	}
	
	public void removeMediaQueryList(MediaQueryList mediaQueryList) {
		mediaQueryLists.remove(mediaQueryList);
	}
	
	public Set<MediaQueryList> getMediaQueryLists() {
		return mediaQueryLists;
	}

	public void addMediaQueryList(MediaQueryList forMedia) {
		mediaQueryLists.add(forMedia);		
	}
	
	/**
	 * Compares two selectors only based on their MediaQueryList's.
	 * It uses {@link MediaQueryList#mediaQueryListEquals()}
	 * and does not consider order of MediaQueryLists.
	 * @param otherSelector
	 * @return
	 */
	public boolean mediaQueryListsEqual(Selector otherSelector) {
		Set<MediaQueryList> alreadyMatchedQueryListInOther = new HashSet<>();
		if (mediaQueryLists.size() != otherSelector.mediaQueryLists.size())
			return false;
		for (MediaQueryList mediaQueryList : mediaQueryLists) {
			boolean found = false;
			for (MediaQueryList mediaQueryList2 : otherSelector.mediaQueryLists) {
				if (!alreadyMatchedQueryListInOther.contains(mediaQueryList2) &&
						mediaQueryList.mediaQueryListEquals(mediaQueryList2)) {
					alreadyMatchedQueryListInOther.add(mediaQueryList2);
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/**
	 * Returns all virtual shorthand declarations for this selector.
	 * Please note that, virtual shorthand declarations <b>DO NOT</b> have the 
	 * real values, because it is not easy to get, from individual declarations, the values
	 * for the equivalent shorthand declarations.
	 * Therefore, from a virtual shorthand declaration, only use 1) property name and 2) individual properties 
	 * @return
	 */
	public Iterable<ShorthandDeclaration> getVirtualShorthandDeclarations() {
		/*
		 * For each selector, we loop over the declarations to see whether a
		 * declaration could become the individual property for one
		 * shorthand property (like margin-left which is an individual
		 * declaration of margin). We keep all individual properties of a
		 * same type (like margin-top, -left, -right and -bottom) which are
		 * in the same selector in a set (namely currentIndividuals) and add
		 * this set to a map (named shorthandedDeclarations) which maps the
		 * name of corresponding shorthand property (margin, in our example)
		 * to the mentioned set.
		 */
		Map<String, Set<Declaration>> shorthandedDeclarations = new HashMap<>();
	
		for (Declaration declaration : getDeclarations()) {
			String property = declaration.getProperty();
			/*
			 * If a property is the individual property for one or more
			 * shorthand properties,
			 * ShorthandDeclaration#getShorthandPropertyNames() method
			 * returns a set containing the name of those shorthand
			 * properties (for example, providing margin-left would result
			 * to margin)
			 */
			Set<String> shorthands = ShorthandDeclaration.getShorthandPropertyNames(property);
			/*
			 * We add the entry (or update the existing entry) in the
			 * HashMap, which maps the mentioned shorthand properties to the
			 * individual properties
			 */
			for (String shorthand : shorthands) {
				Set<Declaration> currentIndividuals = shorthandedDeclarations.get(shorthand);
				if (currentIndividuals == null)
					currentIndividuals = new HashSet<>();
				currentIndividuals.add(declaration);
				shorthandedDeclarations.put(shorthand, currentIndividuals);
			}
		}
	
		/*
		 * Make a virtual shorthand for every possibility
		 * 
		 */
		List<ShorthandDeclaration> virtualShorthands = new ArrayList<>();
		for (Entry<String, Set<Declaration>> entry : shorthandedDeclarations.entrySet()) {
	
			// Create a shorthand
			ShorthandDeclaration virtualShorthand = new ShorthandDeclaration(entry.getKey(), new ArrayList<DeclarationValue>(), this, false, false, new LocationInfo(-1, -1));

			virtualShorthand.isVirtual(true);
			boolean isImportant = true;
			for (Declaration individual : entry.getValue()) {
				virtualShorthand.addIndividualDeclaration(individual.clone());
				isImportant &= individual.isImportant();
			}
			virtualShorthand.isImportant(isImportant);
			
			/*
			 * Check if the new virtual shorthand is styling the same
			 * properties as the individual ones 
			 * This is, BTW, a HACK.
			 * A better implementation needs using default values for all properties
			 * to insert them when the corresponding individual declarations do not exist.
			 */
			List<DeclarationValue> values = new ArrayList<>();
			values.add(new DeclarationValue("none", ValueType.IDENT));
			Declaration toTest = DeclarationFactory.getDeclaration(entry.getKey(), values, this, isImportant, true, new LocationInfo(-1, -1));
			((ShorthandDeclaration)toTest).isVirtual(true);
			if (toTest.getAllSetPropertyAndLayers().equals(virtualShorthand.getAllSetPropertyAndLayers()))
				virtualShorthands.add(virtualShorthand);
			
		}
		return virtualShorthands;
	}	
	
	/**
	 * Returns all the declarations for this selector including virtual shorthand ones
	 * @return
	 */
	public Iterable<Declaration> getAllDeclarationsIncludingVirtualShorthands() {
		List<Declaration> toReturn = new ArrayList<>();
		for (Declaration d : getDeclarations()) {
			toReturn.add(d);
		}
		for (Declaration d : getVirtualShorthandDeclarations()) {
			toReturn.add(d);
		}
		return toReturn;
	}
	
	/**
	 * Returns all individual declarations which are styled in this selector.
	 * First, we convert all shorthand declarations to their individual ones (as individual as possible!)
	 * Then, in case of intra-selector dependencies, we remove the redundant declarations.
	 */
	public Iterable<Declaration> getFinalStylingIndividualDeclarations() {
		List<Declaration> allIndividualDeclarations = new ArrayList<>();
		for (Declaration declaration : getDeclarations()) {
			if (declaration instanceof ShorthandDeclaration) {
				ShorthandDeclaration shorthandDeclaration = (ShorthandDeclaration) declaration;
				for (Declaration d : shorthandDeclaration.getIndividualDeclarationsAtTheDeepestLevel())
					allIndividualDeclarations.add(d);
			} else {
				allIndividualDeclarations.add(declaration);
			}
		}
		
		// Handle overrides
		Set<String> visitedProperties = new HashSet<>();
		List<Declaration> toReturn = new ArrayList<>();
		
		for (int i = allIndividualDeclarations.size() - 1; i >= 0; i--) {
			Declaration currentIndividualDeclaration = allIndividualDeclarations.get(i);
			if (visitedProperties.contains(currentIndividualDeclaration.getProperty()))
				continue;
			toReturn.add(0, currentIndividualDeclaration);;
			visitedProperties.add(currentIndividualDeclaration.getProperty());
		}
		
		return toReturn;
	}
	
	/**
	 * Returns all the declarations of this selector normally, except when we have an intra-selector dependency.
	 * In this case, this method removes the redundant declarations.
	 * If one of the participating declarations is a shorthand, the method will break it down to the individual ones.
	 * 
	 */
	public Iterable<Declaration> getDeclarationsWithIntraSelectorDependenciesRemoved() {
		Map<String, Declaration> toReturn = new LinkedHashMap<>();
		
		Set<Declaration> toExpand = new HashSet<>();
		CSSValueOverridingDependencyList intraSelectorOverridingDependencies = getIntraSelectorOverridingDependencies();
		for (CSSValueOverridingDependency dependency : intraSelectorOverridingDependencies) {
			Declaration d1 = dependency.getDeclaration1();
			Declaration d2 = dependency.getDeclaration2();
			if (d1 instanceof ShorthandDeclaration) {
				toExpand.add(d1);
			}
			if (d2 instanceof ShorthandDeclaration) {
				toExpand.add(d2);
			}
		}
		
		for (Declaration declaration : getDeclarations()) {
			if (toExpand.contains(declaration)) {
				for (Declaration individual : ((ShorthandDeclaration)declaration).getIndividualDeclarationsAtTheDeepestLevel()) {
					toReturn.put(individual.getProperty(), individual);
				}
			} else {
				toReturn.put(declaration.getProperty(), declaration);
			}
		}
		
		return new ArrayList<>(toReturn.values());
	}

	public abstract CSSValueOverridingDependencyList getIntraSelectorOverridingDependencies();

	/**
	 * If you are making any changes to a selector, you can keep track of the
	 * original one.
	 * Use {@link #setOriginalSelector(Selector)} to set the original selector that
	 * you are changing and {@link #getOriginalSelector()} to get it.
	 * @return
	 */
	public Selector getOriginalSelector() {
		return originalSelector;
	}

	/**
	 * If you are making any changes to a selector, you can keep track of the
	 * original one.
	 * Use {@link #setOriginalSelector(Selector)} to set the original selector that
	 * you are changing and {@link #getOriginalSelector()} to get it.
	 * @return
	 */
	public void setOriginalSelector(Selector originalSelector) {
		this.originalSelector = originalSelector;
	}
	
}
