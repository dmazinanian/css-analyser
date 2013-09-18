package analyser.duplication;

import io.IOHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.channels.SeekableByteChannel;
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
	private Map<Declaration, Item> declarationItemMap = new HashMap<>();
	
	public DuplicationFinder(StyleSheet stylesheet) {
		this.stylesheet = stylesheet;
	}

	private DuplicationsList typeOneDuplications;
	private DuplicationsList typeTwoDuplications;
	private DuplicationsList typeThreeDuplications;
	private DuplicationsList typeFourDuplications;
	
	public DuplicationsList getTypeIDuplications() {
		return typeOneDuplications;
	}
	
	public DuplicationsList getTypeIIDuplications() {
		return typeTwoDuplications;
	}
	
	public DuplicationsList getTypeIIIDuplications() {
		return typeThreeDuplications;
	}
	
	public DuplicationsList getTypeIVDuplications() {
		return typeFourDuplications;
	}
	
	// For apriori
	private ItemSetList C1;
	
	/**
	 * Performs typeI through typeIV duplication finding in the
	 * current stylesheet which has been given through constructor
	 */
	public void findDuplications() {
		findTypeOneAndTwoDuplications();
		findTypeThreeDuplication();
		findTypeFourDuplication();
	}

	/**
	 * This method finds the cases in which the property and value
	 * (i.e. the declarations) are the same across different selectors. (Type I)
	 * @return An object of {@link DuplicationsList}
	 */
	private void findTypeOneAndTwoDuplications() {
		
		typeOneDuplications = new DuplicationsList();
		typeTwoDuplications = new DuplicationsList();
		
		C1 = new ItemSetList();
		
		// Lets get all the declarations
		List<Declaration> allDeclarations = new ArrayList<>(stylesheet.getAllDeclarations());
		
		// We don't want to repeat, being identical is a symmetric relation
		Set<Integer> visitedIdenticalDeclarations = new HashSet<>();
		Set<Integer> visitedEquivalentDeclarations = new HashSet<>();
		
		TypeIDuplication typeOneDuplication = new TypeIDuplication();
		TypeIIDuplication typeTwoDuplication = new TypeIIDuplication();
		
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
			
			// for apriori
			Item newItem = declarationItemMap.get(currentDeclaration);
			if (newItem == null) {
				newItem = new Item(currentDeclaration);
				declarationItemMap.put(currentDeclaration, newItem);
				ItemSet itemSet = new ItemSet();
				itemSet.add(newItem);
				C1.add(itemSet);
			}
			
			while (++checkingDecIndex < allDeclarations.size()) {

				Declaration checkingDeclaration = allDeclarations.get(checkingDecIndex);
				
				boolean equals = currentDeclaration.declarationEquals(checkingDeclaration);

				if (equals && !visitedIdenticalDeclarations.contains(currentDeclarationIndex) && 
						!visitedIdenticalDeclarations.contains(checkingDecIndex) ) {
					
					// We have found type I duplication
					// We add the checkingDeclaration, it will add the Selector itself.
					currentTypeIDuplicatedDeclarations.add(checkingDeclaration);
					visitedIdenticalDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeIDuplication = true;
					
					// This only used in apriori
					newItem.add(checkingDeclaration);
					declarationItemMap.put(checkingDeclaration, newItem);
				}
				if (!equals && !visitedEquivalentDeclarations.contains(checkingDecIndex) && 
						(currentDeclaration.declarationIsEquivalent(checkingDeclaration))) {// || (equals && currentTypeIIDuplicatedDeclarations.size() > 1)) {
					/*
					 * We have found type II duplication
					 */
					currentTypeIIDuplicatedDeclarations.add(checkingDeclaration);
					visitedEquivalentDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeTwoDuplication = true;
					
					newItem.add(checkingDeclaration);
					declarationItemMap.put(checkingDeclaration, newItem);
				}
				
			}
			
			// Only if we have at least one declaration in the list (at list one duplication)
			if (mustAddCurrentTypeIDuplication) {
				if (typeOneDuplication.hasAllSelectorsForADuplication(currentTypeIDuplicatedDeclarations)) {
					typeOneDuplication.addAllDeclarations(currentTypeIDuplicatedDeclarations);
				} else {
					typeOneDuplication = new TypeIDuplication();
					typeOneDuplication.addAllDeclarations(currentTypeIDuplicatedDeclarations);
					typeOneDuplications.addDuplication(typeOneDuplication);
				}
			}
			
			if (mustAddCurrentTypeTwoDuplication) {
				if (typeTwoDuplication.hasAllSelectorsForADuplication(currentTypeIIDuplicatedDeclarations)) {
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
				} else {
					typeTwoDuplication = new TypeIIDuplication();
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
					typeTwoDuplications.addDuplication(typeTwoDuplication);
				}
			}
			
		}

	}
	
	/**
	 * Finds typeIII duplications 
	 */
	public void findTypeThreeDuplication() {
		
		typeThreeDuplications = new DuplicationsList();
		
		Set<Selector> selectors = stylesheet.getAllSelectors();
		
		for (Selector selector : selectors) {
			
			/*
			 * For each selector, we loop over the declarations to see
			 * whether a declaration could become the individual property for 
			 * one shorthand property (like margin-left which is an
			 * individual declaration of margin). We keep all individual 
			 * properties of a same type (like margin-top, -left, -right and -bottom)
			 * which are in the same selector in a set (namely currentIndividuals)
			 * and add this set to a map (named shorthandedDeclarations) which maps the name of corresponding
			 * shorthand property (margin, in our example) to the mentioned set.  
			 */
			Map<String, Set<Declaration>> shorthandedDeclarations = new HashMap<>();
			
			for (Declaration declaration : selector.getDeclarations()) {
				String property = declaration.getProperty();
				/*
				 * If a property is the individual property for one or more shorthand
				 * properties, ShorthandDeclaration#getShorthandPropertyNames() method
				 * returns a set containing the name of those shorthand properties 
				 * (for example, providing margin-left would result to margin)
				 */
				Set<String> shorthands = ShorthandDeclaration.getShorthandPropertyNames(property);
				/*
				 * We add the entry (or update the existing entry) in the HashMap, which
				 * maps the mentioned shorthand properties to the individual properties 
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
			 * Then we search all the Declarations of current stylesheet
			 * to see whether one shorthand property has the equal (or equivalent)
			 */
			for (Entry<String, Set<Declaration>> entry : shorthandedDeclarations.entrySet()) {
				
				// Create a shorthand and compare it with a real shorthand
				ShorthandDeclaration virtualShorthand = new ShorthandDeclaration(entry.getKey(), new ArrayList<DeclarationValue>(), selector, -1, -1, false);
				for (Declaration dec : entry.getValue()) {
					virtualShorthand.addIndividualDeclaration(dec);
				}
				
				// For each real shorthand:
				for (Declaration checkingDeclaration : stylesheet.getAllDeclarations()) {
					if (checkingDeclaration instanceof ShorthandDeclaration && 
						virtualShorthand.individualDeclarationsEquivalent((ShorthandDeclaration)checkingDeclaration)) {
						
						TypeIIIDuplication duplication = new TypeIIIDuplication((ShorthandDeclaration)checkingDeclaration, entry.getValue());
						typeThreeDuplications.addDuplication(duplication);
						
						// For apriori
						Item item = declarationItemMap.get(checkingDeclaration);
						
						item.add(virtualShorthand, true);
						
					}
				}
			}
		}
		
	}
	
	public void findTypeFourDuplication() {
		// String filePath = styleSheet.getFilePath();

		//System.out.println(styleSheet);
		
		/*for (Selector selector : styleSheet.getAllSelectors()) {
			if (selector instanceof AtomicSelector) {
				AtomicSelector atomicSelector = (AtomicSelector)selector;
				String XPath = xpath.XPathHelper.AtomicSelectorToXPath(atomicSelector);
				System.out.println(atomicSelector + "->" + XPath);
				if (XPath == null) continue;
				NodeList nodeList = DOMHelper.queryDocument(model.getDocument(), XPath);
				if (nodeList == null)
					continue;
				for (int i = 0; i < nodeList.getLength(); ++i) {
					Element e = (Element) nodeList.item(i);
					System.out.println(e.getNodeName() + e.getTextContent());
				}
			}
		}*/
	}
	
	public List<ItemSetList> apriori(final int minSupport, String path) throws IOException {
					
		List<ItemSetList> l = new ArrayList<>(); // Keeping L(k), the frequent itemsets of size k
		
		l.add(getLfromC(C1, minSupport)); // Generating L(1) by cutting C(1)
				
		int k = 1;
		while (true) {
			// Generating L(k) by using L(k-1)
			l.add(generateCandidates(l.get(k - 1), minSupport));
			
			// Removing previous step's redundant 
			List<ItemSet> toRemove = new ArrayList<>(l.get(k - 1).size());
			for (ItemSet itemset : l.get(k - 1)) {
				if (l.get(k).containsSubset(itemset))
					toRemove.add(itemset);
			}
			l.get(k - 1).removeAll(toRemove);
			
			if (l.get(k).size() == 0) { // If L(k) is empty break
				l.remove(k);
				break;
			}

			k++;
		} 
		
		return l;
	}


	private ItemSetList generateCandidates(ItemSetList itemSetList, int minSupport) {
		
		/* itemSetList is L(k-1), which is a table of ItemSets
		 * toReturn is L(k)
		 */
		ItemSetList toReturn = new ItemSetList();
		
		Set<Item> unionAll = new HashSet<>(); 
		/*
		 * First find the union of all L(k-1) declarations
		 * This set will be used later in order to create itemsets with k declarations.		
		 */
		for (ItemSet itemset : itemSetList) {
			unionAll.addAll(itemset);
		}

		for (ItemSet itemset : itemSetList) {
			/* First we create a new set, which will be our new item set. Initially,
			 * this set contains first member of L(k-1). One at a time, we
			 * add one new member to this set,  to create an itemset with k members.
			 */
			
			for (Item item : unionAll) {
				/* 
				 * Each time we add one item from unionAll ( union of all the declarations in L(k-1) )
				 * to create itemset with k members.
				 * newItemSet must not contain new declaration, otherwise, after 
				 * adding this new declaration, newItemSet would not have k members
				 */
				ItemSet newItemSet = itemset.clone();
				if (!newItemSet.contains(item)) {
					
					newItemSet.add(item);
					
					/*
					 * Also, L(k) should not contain this new itemset.
					 */
					if (newItemSet.getSupport().size() >= minSupport && !toReturn.contains(newItemSet)) {		
							toReturn.add(newItemSet.clone()); 
					} 
				}
			}
		}
		return toReturn;
	}

	private ItemSetList getLfromC(ItemSetList itemSetList, final int minSupportCount) {
		
		ItemSetList Lk = new ItemSetList();
		for (ItemSet itemset : itemSetList) {
			if (itemset.getSupport().size() >= minSupportCount) {
				Lk.add(itemset);
			}
		}
		return Lk;	
	}
	
	
	
	
	
	

}
