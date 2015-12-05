package ca.concordia.cssanalyser.analyser.duplication.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * This class keeps the data of a itemset, in addition to its support 
 * In our definition, every itemset is a set of declarations and
 * support means the number of selectors that have all these declarations.
 * In fact, instead of keeping the support as a pure percentage or number of supports,
 * we keep the selectors for further uses.
 * 
 * Every ItemSet is in fact a clone set.
 * Every <i>frequent</i> itemset (output of the FP-Growth or Apriori algorithms) 
 * will be one grouping refactoring opportunities.
 * 
 * @author Davood Mazinanian
 *
 */
public class ItemSet implements Set<Item>, Cloneable {
	
	private final Set<Item> itemset;
	private final Set<Selector> support;
	private ItemSetList parentItemSetList;
	// Store refactoring impact, so don't compute it again
	protected int refactoringImpact = -1;
	protected Iterable<MediaQueryList> mediaQueryLists;
	
	public ItemSet() {
		itemset = new HashSet<>();
		support = new HashSet<>();
	} 
	
	public ItemSet(Set<Item> items) {
		this();
		for (Item item : items)
			add(item);
	}
	
	public Iterable<Selector> getSupport() {
		return support;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;

		ItemSet otherObj = (ItemSet)obj;	
		
		return itemset.equals(otherObj.itemset);
	}

	@Override
	public int hashCode() {
		return itemset.hashCode();
	}
	
	@Override
	public ItemSet clone() {
		return new ItemSet(itemset);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("(");
		for (Item d : itemset)
			s.append(d.toString() + " - ");
		if (s.length() > 3)
			s.delete(s.length() - 3, s.length());
		s.append(") : {") ;
		if (support != null) {
			for (Selector sel : support)
				s.append(sel + ", ");
			s.delete(s.length() - 2, s.length());
		}
		s.append("}");
		return s.toString();
	}

	@Override
	public boolean add(Item e) {
		boolean itemsChanged = itemset.add(e);
		boolean supportChanged = false;
		if (itemsChanged) {
			if (itemset.size() == 1) {
				supportChanged = true;
				for (Selector s : e.getSupport())
					support.add(s);
				mediaQueryLists = e.getMediaQueryLists();
			}
			else
				supportChanged = support.retainAll((Collection<Selector>)e.getSupport());
			refactoringImpact = -1;
		}
		e.setParentItemSet(this);
		if (parentItemSetList != null && supportChanged)
			parentItemSetList.calculateMaxSupport();
		return itemsChanged;
		
	}

	@Override
	public boolean addAll(Collection<? extends Item> c) {
		boolean changed = false;
		for (Item i : c) {
			if (add(i))
				changed = true;
		}
		return changed;
	}

	@Override
	public void clear() {
		itemset.clear();
		support.clear();
		refactoringImpact = -1;
		mediaQueryLists = null;
	}

	@Override
	public boolean contains(Object o) {
		return itemset.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return itemset.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return itemset.isEmpty();
	}

	@Override
	public Iterator<Item> iterator() {
		return itemset.iterator();
	}

	@Override
	public boolean remove(Object o) {
		boolean changed = itemset.remove(o);
		if (changed) {
			rebuildSupport();
			refactoringImpact = -1;
		}
		return changed;
	}

	/**
	 * Finds the intersection between the supports of
	 * all containing items.
	 */
	public void rebuildSupport() {
		support.clear();
		boolean mustUnion = true;
		for (Item i : itemset) {
			Collection<Selector> selectors = (Collection<Selector>)i.getSupport();
			if (mustUnion) {
				support.addAll(selectors);
				mustUnion = false;
			} else {
				support.retainAll(selectors);
			}
		}
		if (itemset.size() == 0)
			mediaQueryLists = null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = itemset.removeAll(c);
		if (changed) {
			rebuildSupport();
			refactoringImpact = -1;
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = itemset.retainAll(c);
		if (changed) {
			rebuildSupport();
			refactoringImpact = -1;
		}
		return changed;
	}

	@Override
	public int size() {
		return itemset.size();
	}

	@Override
	public Object[] toArray() {
		return itemset.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return itemset.toArray(a);
	}

	/**
	 * Gets the ItemSetList which contains this ItemSet
	 * @return
	 */
	public ItemSetList getParentItemSetList() {
		return parentItemSetList;
	}

	/**
	 * Gets the ItemSetList which contains this ItemSet
	 * @param parentItemSetList
	 */
	public void setParentItemSetList(ItemSetList parentItemSetList) {
		this.parentItemSetList = parentItemSetList;
	}
	
	/**
	 * This method returns the value that could be used to rank the 
	 * grouping refactoring opportunities.
	 * This value is the number of characters we save by doing this 
	 * grouping.
	 * @return
	 */
	public int getGroupingRefactoringImpact() {
		if (refactoringImpact != -1)
			return refactoringImpact;
		
		int result = 0;
		
		int newSelectorCharsLength = 0;
		// Adding length of involving selectors
		// TODO Make sure that the replacing of " " will not have an effect?
		for (Selector s : this.support)
			newSelectorCharsLength += s.toString().length();
		newSelectorCharsLength += 2 * (this.support.size() - 1); // adding grouping commas and spaces 
		newSelectorCharsLength += 3; // curly brackets
		
		int groupedDeclarationsLength = 0;
		for (Item item : this.itemset) {
			// Get the declaration with minimum chars in every item
			groupedDeclarationsLength += item.getDeclarationWithMinimumChars().toString().length() + 1; // +1 is for semicolon 
		}
		
		int realDeclarationsLength = 0;
		for (Item item : this.itemset) {
			for (Declaration declaration : item){
				// The declaration must be in the involving selectors in this refactoring opportunity
				if (this.support.contains(declaration.getSelector()))
				{		
					if (declaration instanceof ShorthandDeclaration) {
						ShorthandDeclaration shorthand = (ShorthandDeclaration)declaration;
						if (shorthand.isVirtual()) {
							for (Declaration individual : shorthand.getIndividualDeclarations())
								realDeclarationsLength += individual.toString().length() + 1; // +1 is for semicolon 
						} else {
							realDeclarationsLength += declaration.toString().length() + 1;
						}
					} else {
						realDeclarationsLength += declaration.toString().length() + 1;
					}
				}
			}
		}
		
		result = realDeclarationsLength - groupedDeclarationsLength - newSelectorCharsLength;
		refactoringImpact = result;
		return refactoringImpact;
	}
	
	/**
	 * Returns one declaration for every item in this itemset.
	 * Uses {@link Item#getFirstDeclaration()} for getting the representative declaration from ItemSet 
	 * @return
	 */
	public List<Declaration> getRepresentativeDeclarations() {
		List<Declaration> declarations = new ArrayList<>();
		for (Item item : itemset) {
			declarations.add(item.getFirstDeclaration());
		}
		return declarations;
	}

	public Iterable<MediaQueryList> getMediaQueryLists() {
		return this.mediaQueryLists;
	}

	public boolean supportContains(Selector selector) {
		return support.contains(selector);
	}

	public int getSupportSize() {
		return support.size();
	}

	/**
	 *  If a shorthand declaration is virtual in all its selectors in an item of the itemset,
	 *  the itemset cannot be used for the refactoring.
	 *  It should be real in at least one of the selectors in the current itemset's support.
	 */
	public boolean isApplicable() {
		boolean itemSetIsDoable = true;
		for (Item currentItem : this) {
			boolean declarationIsNotVirtualInAllSelectors = false;
			boolean allDeclarationsAreNonShorthand = false;
			for (Declaration declaration : currentItem) {
				if (declaration instanceof ShorthandDeclaration) {
					ShorthandDeclaration shorthand = (ShorthandDeclaration)declaration;
					if (supportContains(shorthand.getSelector()) && !shorthand.isVirtual()) {
						declarationIsNotVirtualInAllSelectors = true;
						break;
					}
				} else {
					allDeclarationsAreNonShorthand = true;
					break;
				}
			}
			if (!allDeclarationsAreNonShorthand && !declarationIsNotVirtualInAllSelectors) {
				itemSetIsDoable = false;
				break;
			}
		}
		return itemSetIsDoable;
	}

	/**
	* Returns the new grouping selector which has to be created
	* from the given ItemSet for removing duplication
	* If the ItemSet has differences in some values for some declaration,
	* this method returns null.
	* @return
	*/
	public GroupingSelector getGroupingSelector() {
		
		if (containsDifferencesInValues())
			return null;
	
		GroupingSelector newGroupingSelector = new GroupingSelector();
		
		// Sort selectors in this new grouping selector based on their appearance in their original file
		SortedSet<Selector> sortedSet = new TreeSet<>(new Comparator<Selector>() {
	
			@Override
			public int compare(Selector o1, Selector o2) {
				if (o1.getSelectorNumber() >= o2.getSelectorNumber())
					return 1;
				return -1;
			}
			
		});
		
		for (Selector s : getSupport())
				sortedSet.add(s);
		
		for (Selector selector : sortedSet) {
			if (selector instanceof GroupingSelector) {
				GroupingSelector grouping = (GroupingSelector)selector;
				for (BaseSelector baseSelector : grouping.getBaseSelectors()) {
					newGroupingSelector.add((BaseSelector)baseSelector.copyEmptySelector());
				}
			} else {
				newGroupingSelector.add((BaseSelector)selector.copyEmptySelector());
			}
		}
		// Add the media queries to the new grouping selector
		newGroupingSelector.addMediaQueryLists(getMediaQueryLists());
		
		// Add declarations to this new grouping selector
		// We want to add declarations based on their appearance in the original file
		SortedSet<Declaration> sortedDeclarations = new TreeSet<>(new Comparator<Declaration>() {
			@Override
			public int compare(Declaration o1, Declaration o2) {
				if (o1.getDeclarationNumber() >= o2.getDeclarationNumber())
					return 1;
				return -1;
			}
		});
		
		for (Item currentItem : ItemSet.this) {
			sortedDeclarations.add(currentItem.getDeclarationWithMinimumChars());
		}
		
		for (Declaration declaration : sortedDeclarations)
			newGroupingSelector.addDeclaration(declaration.clone());
		
		return newGroupingSelector;
	}

	/**
	 * Returns declarations that have to be removed from style sheet 
	 * @return
	 */
	public Set<Declaration> getDeclarationsToBeRemoved() {
		Set<Declaration> declarationsToBeRemoved = new HashSet<>();
		for (Item currentItem : this) {
			//Mark declarations to be deleted from the original StyleSheet
			for (Declaration currentDeclaration : currentItem) {
				if (supportContains(currentDeclaration.getSelector())) {
					if (currentDeclaration instanceof ShorthandDeclaration && ((ShorthandDeclaration)currentDeclaration).isVirtual()) {
						for (Declaration individual : ((ShorthandDeclaration)currentDeclaration).getIndividualDeclarations())
							declarationsToBeRemoved.add(individual);
					} else {
						declarationsToBeRemoved.add(currentDeclaration);
					}
				}
			}
		}
		return declarationsToBeRemoved;
	}
	
	/**
	 * Indicates weather there exists an Item in this ItemSet
	 * for which {@link Item#containsDifferencesInValues()} returns true.
	 */
	public boolean containsDifferencesInValues() {
		for (Item item : this.itemset) {
			if (item.containsDifferencesInValues()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the selectors which would be empty if we remove the 
	 * declarations 
	 * @return
	 */
	public Set<Selector> getEmptySelectorsAfterRefactoring() {
		Set<Selector> toReturn = new HashSet<>();
		Set<Declaration> declarationsToBeRemoved = getDeclarationsToBeRemoved();
		Map<Selector, Integer> numberOfDeletedDeclarationsMap = new HashMap<>();
		for (Declaration declaration : declarationsToBeRemoved) {
			Selector selector = declaration.getSelector();
			if (!numberOfDeletedDeclarationsMap.containsKey(selector)) {
				numberOfDeletedDeclarationsMap.put(selector, 0);
			}
			int currentNumberOfDeletedDeclarations = numberOfDeletedDeclarationsMap.get(selector);
			currentNumberOfDeletedDeclarations++;
			numberOfDeletedDeclarationsMap.put(selector, currentNumberOfDeletedDeclarations);
		}
		for (Selector selector : numberOfDeletedDeclarationsMap.keySet()) {
			if (selector.getNumberOfDeclarations() == numberOfDeletedDeclarationsMap.get(selector)) {
				// All the declarations of this selector are going to be removed
				toReturn.add(selector);
			}
		}
		return toReturn;
	}
}
