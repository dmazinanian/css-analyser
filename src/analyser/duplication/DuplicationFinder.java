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
import analyser.duplication.apriori.Apriori;
import analyser.duplication.apriori.Item;
import analyser.duplication.apriori.ItemSet;
import analyser.duplication.apriori.ItemSetList;
import analyser.duplication.fpgrowth.DataSet;
import analyser.duplication.fpgrowth.FPGrowth;

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

	private DuplicationsList typeOneDuplicationsList;
	private DuplicationsList typeTwoDuplicationsList;
	private DuplicationsList typeThreeDuplicationsList;
	private DuplicationsList typeFourDuplicationsList;
	
	public DuplicationsList getTypeIDuplications() {
		return typeOneDuplicationsList;
	}
	
	public DuplicationsList getTypeIIDuplications() {
		return typeTwoDuplicationsList;
	}
	
	public DuplicationsList getTypeIIIDuplications() {
		return typeThreeDuplicationsList;
	}
	
	public DuplicationsList getTypeIVDuplications() {
		return typeFourDuplicationsList;
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
		
		typeOneDuplicationsList = new DuplicationsList();
		typeTwoDuplicationsList = new DuplicationsList();
		
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
					
					// This only used in apriori and fpgrowth
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
					typeOneDuplicationsList.addDuplication(typeOneDuplication);
				}
			}
			
			if (mustAddCurrentTypeTwoDuplication) {
				if (typeTwoDuplication.hasAllSelectorsForADuplication(currentTypeIIDuplicatedDeclarations)) {
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
				} else {
					typeTwoDuplication = new TypeIIDuplication();
					typeTwoDuplication.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
					typeTwoDuplicationsList.addDuplication(typeTwoDuplication);
				}
			}
			
		}

	}
	
	/**
	 * Finds typeIII duplications 
	 */
	public void findTypeThreeDuplication() {
		
		typeThreeDuplicationsList = new DuplicationsList();
		
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
						typeThreeDuplicationsList.addDuplication(duplication);
						
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
	
	


	public List<ItemSetList> apriori(int minsup) {
		Apriori apriori = new Apriori(C1);
		return apriori.apriori(minsup);
	}

	public List<ItemSetList> fpGrowth(int minSupport) {
		DataSet dataSet = new DataSet();
		
		for (Item item : new HashSet<>(declarationItemMap.values())) {
			if (item.getSupport().size() >= minSupport) {
				for (Selector selector : item.getSupport())
					dataSet.addItem(selector, item);	
			}
		}
//		for (Declaration declaration : stylesheet.getAllDeclarations()) {
//			if (declarationItemMap.get(declaration).getSupport().size() >= minSupport)
//				dataSet.addItem(declaration.getSelector(), declarationItemMap.get(declaration));
//		}
		FPGrowth fpGrowth = new FPGrowth(dataSet);
		return fpGrowth.mine(minSupport);	
	}	

}
