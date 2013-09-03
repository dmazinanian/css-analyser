package analyser.duplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import CSSModel.StyleSheet;

import CSSModel.declaration.Declaration;
import CSSModel.declaration.ShorthandDeclaration;
import CSSModel.declaration.value.DeclarationValue;

import CSSModel.selectors.Selector;

/**
 * This class is responsible for finding various types of duplications in a
 * style sheet. The stylesheet is kept in a <{@link StyleSheet} object in the memory.
 * 
 * @author Davood Mazinanian
 */
public class DuplicationFinder {

	private StyleSheet stylesheet;
	
	/*
	 *  This map contains all selectors for a given declaration
	 *  In apriori algorithm, this map is used in order to accelerate finding supports 
	 */
	private Map<Declaration, List<Selector>> declarationSelector = new HashMap<>();
	
	// TODO !!!!
	public DuplicationsList typeOneDuplications;
	public DuplicationsList typeTwoDuplications;
	private DuplicationsList typeThreeDuplications;
	private DuplicationsList typeFourDuplications;

	public DuplicationFinder(StyleSheet stylesheet) {
		this.stylesheet = stylesheet;
	}

	/**
	 * 
	 */
	public void findDuplications() {
		
		findTypeOneAndTwoDuplications();
		findTypeThreeDuplication();
		//findTypeFourDuplication();
	}

	/**
	 * This method finds the cases in which the property and value
	 * (i.e. the declarations) are the same across different selectors. (Type I)
	 * @return An object of {@link DuplicationsList}
	 */
	private void findTypeOneAndTwoDuplications() {
		
		typeOneDuplications = new DuplicationsList();
		typeTwoDuplications = new DuplicationsList();
		
		// Lets get all the declarations
		List<Declaration> allDeclarations = stylesheet.getAllDeclarations();
		
		// We don't want to repeat, being identical is a symmetric relation
		Set<Integer> visitedIdenticalDeclarations = new HashSet<>();
		Set<Integer> visitedEquivalentDeclarations = new HashSet<>();
		
		TypeOneDuplication typeOneDuplication = new TypeOneDuplication();
		TypeTwoDuplication typeTwoDuplication = new TypeTwoDuplication();
		
		int currentDeclarationIndex = -1;		
		while (++currentDeclarationIndex < allDeclarations.size()) {
			
			Declaration currentDeclaration = allDeclarations.get(currentDeclarationIndex);
		
			int checkingDecIndex = currentDeclarationIndex;
		
			/*
			 * We want to keep all current identical declarations together, and then 
			 * add them to the duplications list when we found all identical declarations 
			 * of current declaration.
			 */
			List<Declaration> currentTypeIDuplicatedDeclarations = new ArrayList<Declaration>();
			
			/*
			 * So we add current declaration to this list and add all identical
			 * declarations to this list 
			 */
			currentTypeIDuplicatedDeclarations.add(currentDeclaration);
			
			/* 
			 * Only when add the current duplication to the duplications list
			 * that we have really found a duplication
			 */
			boolean mustAddCurrentTypeIDuplication = false;
		
			
			/*
			 * As we are going to find the duplications of type II, we repeat three previous expressions
			 * for type II duplications
			 */
			List<Declaration> currentTypeIIDuplicatedDeclarations = new ArrayList<Declaration>();
			currentTypeIIDuplicatedDeclarations.add(currentDeclaration);
			boolean mustAddCurrentTypeTwoDuplication = false;
			
			/*
			 * selectors object will be used to keep all selectors for a
			 * declaration
			 */
			List<Selector> selectors = new ArrayList<>(); 
			selectors.add(currentDeclaration.getSelector());
			
			
			while (++checkingDecIndex < allDeclarations.size()) {

				Declaration checkingDeclaration = allDeclarations.get(checkingDecIndex);
				
				boolean equals = currentDeclaration.equals(checkingDeclaration);

				if (equals && !visitedIdenticalDeclarations.contains(currentDeclarationIndex) && !visitedIdenticalDeclarations.contains(checkingDecIndex) ) {
					
					/*
					 * We have found type I duplication
					 */
					//We add the checkingDeclaration, it will add the Selector itself.
					currentTypeIDuplicatedDeclarations.add(checkingDeclaration);
					visitedIdenticalDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeIDuplication = true;
					// This only used in apriori
					selectors.add(checkingDeclaration.getSelector());
				}
				if (!visitedEquivalentDeclarations.contains(checkingDecIndex) && 
						(!equals && currentDeclaration.equivalent(checkingDeclaration))) {// || (equals && currentTypeIIDuplicatedDeclarations.size() > 1)) {
					/*
					 * We have found type II duplication
					 */
					currentTypeIIDuplicatedDeclarations.add(checkingDeclaration);
					visitedEquivalentDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeTwoDuplication = true;
					selectors.add(checkingDeclaration.getSelector());
				}
				
			}

			if (!visitedIdenticalDeclarations.contains(currentDeclarationIndex) && !visitedEquivalentDeclarations.contains(currentDeclarationIndex))
			// This only used in apriori
				declarationSelector.put(currentDeclaration, selectors);
			
			// Only if we have at least one declaration in the list (at list one duplication)
			if (mustAddCurrentTypeIDuplication) {
				if (typeOneDuplication.hasAllSelectorsForADuplication(currentTypeIDuplicatedDeclarations)) {
					typeOneDuplication.addAllDeclarations(currentTypeIDuplicatedDeclarations);
				} else {
					typeOneDuplication = new TypeOneDuplication();
					typeOneDuplication.addAllDeclarations(currentTypeIDuplicatedDeclarations);
					typeOneDuplications.addDuplication(typeOneDuplication);
				}
			}
			
			if (mustAddCurrentTypeTwoDuplication) {
				if (typeTwoDuplication.hasAllSelectorsForADuplication(currentTypeIIDuplicatedDeclarations)) {
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
				} else {
					typeTwoDuplication = new TypeTwoDuplication();
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
					typeTwoDuplications.addDuplication(typeTwoDuplication);
				}
			}
		}

	}
	
	public void findTypeThreeDuplication() {
		
		List<Selector> selectors = stylesheet.getAllSelectors();
		
		for (Selector selector : selectors) {
			Map<String, Set<Declaration>> shorthandedDeclarations = new HashMap<>();	
			for (Declaration declaration : selector.getDeclarations()) {
				String property = declaration.getProperty();
				Set<String> shorthands = ShorthandDeclaration.getShorthandPropertyNames(property);
				for (String shorthand : shorthands) {
					Set<Declaration> currentIndividuals = shorthandedDeclarations.get(shorthand);
					if (currentIndividuals == null)
						currentIndividuals = new HashSet<>();
					currentIndividuals.add(declaration);
					shorthandedDeclarations.put(shorthand, currentIndividuals);	
				}
			}
			
			for (Entry<String, Set<Declaration>> entry : shorthandedDeclarations.entrySet()) {
				ShorthandDeclaration shorthand = new ShorthandDeclaration(entry.getKey(), new ArrayList<DeclarationValue>(), selector, -1, -1, false);
				StringBuilder s = new StringBuilder();
				for (Declaration dec : entry.getValue()) {
					shorthand.addIndividualDeclaration(dec);
					s.append(String.format("%s (%s, %s); ", dec, dec.getLineNumber(), dec.getColumnNumber()));
				}
				
				for (Declaration checkingDeclaration : stylesheet.getAllDeclarations()) {
					if (checkingDeclaration instanceof ShorthandDeclaration &&
							shorthand.individualDeclarationsEqual((ShorthandDeclaration)checkingDeclaration)) {
						
						System.out.println(String.format("%s (%s, %s) = %s", checkingDeclaration, 
								checkingDeclaration.getLineNumber(),
								checkingDeclaration.getColumnNumber(),
								s.substring(0, s.length()-2)));
					}
				}
			}
		}
		
	}
	
	public void findTypeFourDuplication() {
		// TODO Auto-generated method stub
		
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
		Set<Declaration> allDistinctDeclarations = declarationSelector.keySet();
		
		for (Declaration currentDeclaration : allDistinctDeclarations) {
					
			//if (visited.contains(currentDeclaration)) 
			//	continue;
			
			Set<Declaration> declarations = new HashSet<>(); // The 1-itemset
			declarations.add(currentDeclaration);			
			C1.addItemSet(declarations, declarationSelector.get(currentDeclaration));
			//visited.add(currentDeclaration);
			//System.out.println(currentDeclaration + ":" + declarationSelector.get(currentDeclaration));
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
