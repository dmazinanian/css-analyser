package duplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import CSSModel.StyleSheet;
import CSSModel.declaration.Declaration;
import CSSModel.selectors.AtomicSelector;
import CSSModel.selectors.Selector;

/**
 * This class is responsible for finding various types of duplications in a
 * style sheet, kept in a <{@link StyleSheet} object in the memory.
 * 
 * @author Davood Mazinanian
 */
public class DuplicationFinder {

	private StyleSheet stylesheet;
	
	private Map<Declaration, List<Selector>> declarationSelector = new HashMap<>();

	public DuplicationFinder(StyleSheet stylesheet) {
		this.stylesheet = stylesheet;
	}

	/**
	 * This method finds the selectors which are repetitive in the list of all
	 * selectors. Although the duplicated selectors are not necessary because we
	 * have grouping in CSS.
	 * Similarity function:
	 * 
	 * @return An object of {@link DuplicationsList}
	 */
	public DuplicationsList findIdenticalSelectors() {

		DuplicationsList duplicationList = new DuplicationsList();

		/*
		 * This is the list of the indices of already visited selectors I have
		 * used the indices so we won't rely on the equals() method of the
		 * selectors. Also, I am using a HashSet to get a constant time
		 * search.
		 */
		Set<Integer> visited = new HashSet<>();

		List<AtomicSelector> allAtomicSelectors = stylesheet.getAllAtomicSelectors();

		// So start from the first selector
		int currentSelectorIndex = -1;
		while (++currentSelectorIndex < allAtomicSelectors.size()) {

			AtomicSelector currentSelector = allAtomicSelectors.get(currentSelectorIndex);

			if (visited.contains(currentSelectorIndex))
				continue;
			/*
			 * Start checking from next index in the temporary list. First we
			 * assume that a duplication has just happened. so we create the
			 * appropriate object for it (considering that we only can have
			 * one duplication object of every selector).
			 */
			IdenticalSelectors duplication = new IdenticalSelectors(currentSelector);

			int checkingSelectorIndex =  currentSelectorIndex;
			
			while (++checkingSelectorIndex < allAtomicSelectors.size()) {
				
				AtomicSelector checkingSelector = allAtomicSelectors.get(checkingSelectorIndex);

				if (currentSelector.equals(checkingSelector)) {
					// So it seems that we have found a duplication in selectors
					duplication.addSelector(checkingSelector);
					visited.add(checkingSelectorIndex);
				}

			}
			
			// If we have more than one occurrence of one selector in the list:
			if (duplication.getNumberOfOccurrences() > 1) {
				duplicationList.addDuplication(duplication);
			}

		}

		return duplicationList;
	}

	/**
	 * This method finds the cases in which the property and value
	 * (i.e. the declarations) are the same across different selectors.
	 * @return An object of {@link DuplicationsList}
	 */
	public DuplicationsList findIdenticalDeclarations() {
		
		DuplicationsList duplicationsList = new DuplicationsList();
		
		// Lets get all the declarations
		List<Declaration> allDeclarations = stylesheet.getAllDeclarations();
		
		// We don't want to repeat.
		Set<Integer> visitedDeclarations = new HashSet<>();
		
		IdenticalDeclarations duplication = new IdenticalDeclarations();
		
		int currentDecIndex = -1;		
		while (++currentDecIndex < allDeclarations.size()) {
			
			Declaration currentDeclaration = allDeclarations.get(currentDecIndex);

			// Don't repeat
			if (visitedDeclarations.contains(currentDecIndex))
				continue;
			
			/* Only when add the current duplication to the duplications list
			 * that we have really found a duplication
			 */
			boolean mustAdd = false;
			
			int checkingDecIndex = currentDecIndex;
		
			List<Declaration> currentEqualDeclarations = new ArrayList<Declaration>();
			List<Selector> selectors = new ArrayList<>(); 
			selectors.add(currentDeclaration.getSelector());
			currentEqualDeclarations.add(currentDeclaration);
			
			while (++checkingDecIndex < allDeclarations.size()) {

				Declaration checkingDeclaration = allDeclarations.get(checkingDecIndex);

				if (currentDeclaration.equals(checkingDeclaration)) {
					//We add the checkingDeclaration, it will add the Selector itself.
					currentEqualDeclarations.add(checkingDeclaration);
					visitedDeclarations.add(checkingDecIndex);
					mustAdd = true;
					selectors.add(checkingDeclaration.getSelector());
				}
				
			}
			
			declarationSelector.put(currentDeclaration, selectors);
			
			// Only if we have at least one declaration in the list (at list one duplication)
			if (mustAdd) {
				if (duplication.hasAllSelectorsForADuplication(currentEqualDeclarations)) {
					duplication.addAllDeclarations(currentEqualDeclarations);
				} else {
					duplication = new IdenticalDeclarations();
					duplication.addAllDeclarations(currentEqualDeclarations);
					duplicationsList.addDuplication(duplication);
				}
			}
			
		}
		return duplicationsList;
	}

	/**
	 * Finds all the duplications, where only values for different 
	 * properties across different selectors are exactly the same.
	 * @return An object of {@link DuplicationsList}
	 */
	// TODO: Consider the cases such as red and #F00 and #FF0000
	// Consider very carefully about different variations in the values.
	public DuplicationsList findIdenticalValues() {
		
		DuplicationsList duplicationList = new DuplicationsList();

		// Get a list of all declarations
		List<Declaration> allDeclarations = stylesheet.getAllDeclarations();
		
		// Yeah don't repeat for different declaration
		Set<Integer> visitedDeclarations = new HashSet<>();
		
		int currentDecIndex = -1;
		
		while (++currentDecIndex < allDeclarations.size()) {
			
			Declaration currentDeclaration = allDeclarations.get(currentDecIndex);

			if (visitedDeclarations.contains(currentDecIndex))
				continue;
			
			IdenticalValues duplication = new IdenticalValues();
			
			duplication.addDeclaration(currentDeclaration);

			// Start from the next index
			int checkingDecIndex = currentDecIndex;
			while (++checkingDecIndex < allDeclarations.size()) {

				Declaration checkingDeclaration = allDeclarations.get(checkingDecIndex);
				
				if (currentDeclaration.valuesEqual(checkingDeclaration)) {
					// Found the desirable duplication
					duplication.addDeclaration(checkingDeclaration);
					visitedDeclarations.add(checkingDecIndex);
				}
			}
			
			if (duplication.getNumberOfDeclarations() > 1) 
				duplicationList.addDuplication(duplication);

		}

		return duplicationList;
	}

	/**
	 * Finds the overriden values.
	 * @return
	 */
	public DuplicationsList findOverridenValues(DuplicationsList identicalSelectorsDuplication) {
		return findIdenticalSelectorAndDeclaration(true, identicalSelectorsDuplication);
	}

	/**
	 * This method finds the cases in which the selector, property and values
	 * are all the same.
	 * @return
	 */
	public DuplicationsList findIdenticalEffects(DuplicationsList identicalSelectorsDuplication) {
		return findIdenticalSelectorAndDeclaration(false, identicalSelectorsDuplication);
	}
	
	/**
	 * Checks for identical selectors, and based on the onlyCheckProperties
	 * value, checks to see whether there is an equal declaration (false value for onlyCheckProperties)
	 * or an equal property (true value for onlyCheckProperties).
	 * @param onlyCheckProperties Only check for properties to be the same
	 * @param duplicatedSelectors The result of {@link #findIdenticalSelectors()} method
	 * @return An object of DuplicationList
	 */
	public DuplicationsList findIdenticalSelectorAndDeclaration(boolean onlyCheckProperties, DuplicationsList duplicatedSelectors) {
		
		DuplicationsList duplicationList = new DuplicationsList();

		if (duplicatedSelectors == null)
			duplicatedSelectors = findIdenticalSelectors();
		
		for (Duplication selectorDuplication : duplicatedSelectors) {
			
			// This is a list of identical selectors
			List<Selector> identicalSelectors = ((IdenticalSelectors)selectorDuplication).getListOfSelectors();
			
			Duplication duplication;
			
			if (onlyCheckProperties) {
				duplication = new OverriddenProperties(identicalSelectors.get(0));
			} else {
				duplication = new IdenticalEffects(identicalSelectors.get(0));
			}
			boolean mustAdd = false;

			int currentSelectorIndex = -1;
			while (++currentSelectorIndex < identicalSelectors.size()) {
				
				Selector currentSelector = identicalSelectors.get(currentSelectorIndex);
				List<Declaration> currentDeclarations = currentSelector.getDeclarations();
				
				/* For each declaration for the current selector, 
				 * check all selectors to see whther they have the same declarations or not
				 */
				for (Declaration currentDeclaration : currentDeclarations) {
					
					List<Declaration> currentEqualDeclarations = new ArrayList<>();
					currentEqualDeclarations.add(currentDeclaration);
				
					int checkingSelectorIndex = currentSelectorIndex;
					while (++checkingSelectorIndex < identicalSelectors.size()) {
						Selector checkingSelector = identicalSelectors.get(checkingSelectorIndex);
						List<Declaration> checkingDeclarations = checkingSelector.getDeclarations();
	
						if (onlyCheckProperties) {
							for (Declaration checkingDeclaration : checkingDeclarations) {
								if (currentDeclaration.getProperty().equals(checkingDeclaration.getProperty())
										&& !currentDeclaration.valuesEqual(checkingDeclaration)) {
									currentEqualDeclarations.add(checkingDeclaration);
								}
							}

						} else {

							int index = checkingDeclarations.indexOf(currentDeclaration);

							if (index >= 0) {

								Declaration checkingDeclaration = checkingDeclarations.get(index); 
								currentEqualDeclarations.add(checkingDeclaration);
							}
						}

					}
					
					// If we have found some duplications, add the declarations.
					if (currentEqualDeclarations.size() > 1) {
						if (onlyCheckProperties) {
							((OverriddenProperties)duplication).addAllDeclarations(currentEqualDeclarations);
						}
						else {
							((IdenticalEffects)duplication).addAllDeclarations(currentEqualDeclarations);
						}
						mustAdd = true;
					}
				}
			}
			
			
			if (mustAdd)
				duplicationList.addDuplication(duplication);
			
			
		}

		return duplicationList;
	}
	
	public DuplicationsList findDeclarationIntersections() {
		DuplicationsList duplicationsList = new DuplicationsList();
		
		return duplicationsList;
	}
	
	
	public List<ItemSetList> apriori(final int minSupport) {
				
		List<ItemSetList> c = new ArrayList<>(); // Keeping C(k), the candidate list of itemsets of size k
		List<ItemSetList> l = new ArrayList<>(); // Keeping L(k), the frequent itemsets of size k
		
		c.add(getC1()); // C(1)
		
		l.add(prune(c.get(0), minSupport)); // Generating L(1) by pruning C(1)
		
		int k = 1;
		while (true) {
			
			c.add(generateCandidates(l.get(k - 1))); // Generate C(k). It also gets the supports
			
			ItemSetList Lk = prune(c.get(k), minSupport); // Generate L(k)
			
			if (Lk.getNumberOfItems() == 0) // If L(k) is empty break
				break;
			
			l.add(Lk); // Add L(k)
			
			k++;
		}
		
		return l;
	}

	
	private ItemSetList getC1() { // Gets C(1), the individual declarations with their frequencies
		
		//List<Declaration> allDeclarations = 
		
		ItemSetList C1 = new ItemSetList(); // List of itemsets and their supports

		// Only look for distinct declarations
		Set<Declaration> allDistinctDeclarations = new HashSet<>(stylesheet.getAllDeclarations());
		
		for (Declaration currentDeclaration : allDistinctDeclarations) {
					
			//if (visited.contains(currentDeclaration)) 
			//	continue;
			
			Set<Declaration> declarations = new HashSet<>(); // The 1-itemset
			declarations.add(currentDeclaration);			
			C1.addItemSet(declarations, getSupport(declarations));
			//visited.add(currentDeclaration);
		}

		return C1;
	}

	private ItemSetList generateCandidates(ItemSetList itemSetList) {
		
		/* itemSetList is L(k-1), which is a table of ItemSetAndSupports
		 * toReturn is C(k)
		 */
		ItemSetList toReturn = new ItemSetList();
		
		Set<Declaration> unionAll = new HashSet<>(); 
		/*
		 * First find the union of all L(k-1) declarations
		 * This set will be used later in order to create itemsets with k declarations.		
		 */
		for (ItemSet itemset : itemSetList) {
			unionAll.addAll(itemset.getItemSet());
		}
		
		for (ItemSet itemset : itemSetList) {
			/* First we create a new set, which will be our new item set. Initially,
			 * this set contains first member of L(k-1). One at a time, we
			 * add one new member to this set,  to create an itemset with k members.
			 */
			Set<Declaration> newItemSet = new HashSet<>(itemset.getItemSet());
			for (Declaration declaration : unionAll) {
				/* Each time we add one item from unionAll ( union of all the declarations in L(k-1) )
				 * to create itemset with k members.
				 * newItemSet must not contain new declaratio, otherwise, after 
				 * adding this new declaration, newItemSet would not have k members
				 */
				if (!newItemSet.contains(declaration)) {
					newItemSet.add(declaration);
					/*
					 * Also, C(k) should not contain this new itemset.
					 */
					if (!toReturn.contains(newItemSet)) {
						
						/* Lets apply apriori attribute:
						 * We need to see whether all subsets of size k-1
						 * of newItemSet are in L(k-1). If not, this itemset
						 * must not be added.
						 * To do this, we remove one member of newItemSet at a 
						 * time and check if this new set is in L(k-1).
						 */
						
						/* First we copy newItemSet because we cannot modify it while we are
						 * iterating over its values
						 */
						Set<Declaration> newSetTemp = new HashSet<>(newItemSet);

						// Flag too see whether we need to add this itemset or not
						boolean dontAddCurrentSet = false;

						for (Declaration declarationToBeRemoved : newItemSet) {
							// Remove declaration, one at a time to get k-1 memebrs
							newSetTemp.remove(declarationToBeRemoved);
							
							// Lets see if this subset of size k-1 is in L(k-1) or not
							if (!itemSetList.contains(newSetTemp)) { 
								dontAddCurrentSet = true;
								break;
							}
							newSetTemp.add(declarationToBeRemoved);
						}
						
						if (!dontAddCurrentSet) {
							Set<Declaration> newSet = new HashSet<>(newItemSet); 
							toReturn.addItemSet(newSet, getSupport(newSet)); 
						}
					}
					newItemSet.remove(declaration); // remove the newly added declaration
				}
			}
		}
		return toReturn;
	}

	private ItemSetList prune(ItemSetList itemSetList, final int minSupportCount) {
		
		ItemSetList Lk = new ItemSetList();
		
		for (ItemSet itemset : itemSetList) {
			if (itemset.getSupport() >= minSupportCount)
				Lk.addItemSet(itemset);
		}
		
		return Lk;	
	}

	private List<Selector> getSupport(Set<Declaration> declarations) {
		List<Selector> selectors = null; // Put first declaration's selector set into the selectors list 

		//int size = declarations.size();
		boolean mustAdd = true;
		for (Declaration d : declarations)
			if (mustAdd) {
				selectors = new ArrayList<>(declarationSelector.get(d));
				mustAdd = false;
			}
			else
				selectors.retainAll(declarationSelector.get(d));
		
		return selectors;
	} 

	/*private boolean isIn(Selector selectorToBeCheckedIn, Selector selectorToFind) {
		if (selectorToBeCheckedIn instanceof GroupedSelectors) {
			GroupedSelectors group2 = (GroupedSelectors) selectorToBeCheckedIn;
			if (group2.contains(selectorToFind))
				return true;
		} else if (selectorToBeCheckedIn instanceof AtomicSelector) {
			return selectorToFind.equals(selectorToBeCheckedIn);
		}
		return false;
	}*/

}
