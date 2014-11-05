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

import ca.concordia.cssanalyser.cssmodel.CSSOrigin;
import ca.concordia.cssanalyser.cssmodel.CSSSource;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;


public abstract class Selector  {
	
	protected int lineNumber;
	protected int columnNumber;
	protected int offset;
	protected int lenghth;
	protected StyleSheet parentStyleSheet;
	protected Set<MediaQueryList> mediaQueryLists;
	protected Map<Declaration, Integer> declarations;
	protected CSSSource source = CSSSource.EXTERNAL;
	protected CSSOrigin origin = CSSOrigin.AUTHOR;
	
	public Selector() {
		this(-1, -1);
	}
	
	public Selector(int fileLineNumber, int fileColNumber) {
		lineNumber = fileLineNumber;
		columnNumber = fileColNumber;
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
	
	public void addDeclaration(Declaration declaration) {
		if (!declarations.containsKey(declaration))
			declarations.put(declaration, declarations.size() + 1);
		declaration.setSelector(this);
	}
	
	public int getNumberOfDeclarations() {
		return declarations.size();
	}

	public int getOffset() {
		return this.offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getLength() {
		return this.lenghth;
	}
	
	public void setLength(int length) {
		this.lenghth = length;
	}
		
	public Iterable<Declaration> getDeclarations() {
		return declarations.keySet();
	}
	
	public boolean containsDeclaration(Declaration declaration) {
		return this.declarations.containsKey(declaration);
	}
	

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int linNumber) {
		lineNumber = linNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int fileColumnNumber) {
		columnNumber = fileColumnNumber;
	}

	public Set<MediaQueryList> getMediaQueryLists() {
		return mediaQueryLists;
	}
	
	public int getDeclarationNumber(Declaration declaration) {
		return declarations.get(declaration);
	}
	
	/**
	 * Returns the selector number in the style sheet
	 * @return
	 */
	public int getSelectorNumber() {
		return parentStyleSheet.getSelectorNumber(this);
	}
	
	/**
	 * The equals() method for different selectors have different meanings
	 * but in all of them selectors should be exactly the same and are appeared
	 * in the same location in the file. This method provides a way to compare
	 * two selectors to see weather they are equal, regardless of their
	 * definition location
	 * @param otherSelector
	 * @return
	 */
	public abstract boolean selectorEquals(Selector otherSelector);
	
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
		declaration.setSelector(null);
		this.declarations.remove(declaration);
		// Update the numbers associated with declarations
		int i = 0;
		for (Declaration d : declarations.keySet())
			declarations.put(d, i++);
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
	
	public Iterable<MediaQueryList> mediaQueryLists() {
		return mediaQueryLists;
	}

	public void addMediaQueryList(MediaQueryList forMedia) {
		mediaQueryLists.add(forMedia);		
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
			ShorthandDeclaration virtualShorthand = new ShorthandDeclaration(entry.getKey(), new ArrayList<DeclarationValue>(), this, -1, -1, false, false);
			// Important, important
			virtualShorthand.isVirtual(true);
			for (Declaration dec : entry.getValue()) {
				virtualShorthand.addIndividualDeclaration(dec);
			}
			
			virtualShorthands.add(virtualShorthand);
			
		}
		return virtualShorthands;
	}
	
}
