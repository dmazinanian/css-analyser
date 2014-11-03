package ca.concordia.cssanalyser.analyser.duplication;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ca.concordia.cssanalyser.analyser.duplication.TypeFourDuplicationInstance.TypeIVBDuplication;
import ca.concordia.cssanalyser.analyser.duplication.apriori.Apriori;
import ca.concordia.cssanalyser.analyser.duplication.fpgrowth.FPGrowth;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;

/**
 * This class is responsible for finding various types of duplications in a
 * style sheet. The stylesheet is kept in a <{@link StyleSheet} object in the
 * memory.
 * 
 * @author Davood Mazinanian
 */
public class DuplicationDetector {
	
	private static Logger LOGGER = LoggerFactory.getLogger(DuplicationDetector.class);


	private StyleSheet stylesheet;

	/*
	 * This map contains all selectors for a given declaration In apriori (and FP-Growth)
	 * algorithm, this map is used in order to accelerate finding supports
	 */
	private Map<Declaration, Item> declarationItemMap = new HashMap<>();

	public DuplicationDetector(StyleSheet stylesheet) {
		this.stylesheet = stylesheet;
	}

	private DuplicationIncstanceList typeOneDuplicationsList;
	private DuplicationIncstanceList typeTwoDuplicationsList;
	private DuplicationIncstanceList typeThreeDuplicationsList;
	private DuplicationIncstanceList typeFourADuplicationsList;
	private DuplicationIncstanceList typeFourBDuplicationsList;

	public DuplicationIncstanceList getTypeIDuplications() {
		return typeOneDuplicationsList;
	}

	public DuplicationIncstanceList getTypeIIDuplications() {
		return typeTwoDuplicationsList;
	}

	public DuplicationIncstanceList getTypeIIIDuplications() {
		return typeThreeDuplicationsList;
	}

	public DuplicationIncstanceList getTypeIVADuplications() {
		return typeFourADuplicationsList;
	}

	public DuplicationIncstanceList getTypeIVBDuplications() {
		return typeFourBDuplicationsList;
	}

	// For apriori
	private ItemSetList C1;

	/**
	 * Performs typeI through typeIVA duplication finding in the current
	 * stylesheet which has been given through constructor.
	 */
	public void findDuplications() {
		findTypeOneAndTwoDuplications();
		findTypeThreeDuplication();
		findTypeFourADuplication();
	}

	/**
	 * This method finds the cases in which the property and value (i.e. the
	 * declarations) are the same across different selectors. (Type I)
	 * 
	 * @return An object of {@link DuplicationIncstanceList}
	 */
	private void findTypeOneAndTwoDuplications() {

		typeOneDuplicationsList = new DuplicationIncstanceList();
		typeTwoDuplicationsList = new DuplicationIncstanceList();

		C1 = new ItemSetList();

		// Lets get all the declarations
		List<Declaration> allDeclarations = new ArrayList<>(
				stylesheet.getAllDeclarations());

		// We don't want to repeat, being identical is a symmetric relation
		Set<Integer> visitedIdenticalDeclarations = new HashSet<>();
		Set<Integer> visitedEquivalentDeclarations = new HashSet<>();

		TypeOneDuplicationInstance typeOneDuplication = new TypeOneDuplicationInstance();
		TypeTwoDuplicationInstance typeTwoDuplication = new TypeTwoDuplicationInstance();

		int currentDeclarationIndex = -1;
		while (++currentDeclarationIndex < allDeclarations.size()) {

			Declaration currentDeclaration = allDeclarations
					.get(currentDeclarationIndex);

			int checkingDecIndex = currentDeclarationIndex;

			/*
			 * We want to keep all current identical declarations together, and
			 * then add them to the duplications list when we found all
			 * identical declarations of current declaration.
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
			 * As we are going to find the duplications of type II, we repeat
			 * three previous expressions for type II duplications
			 */
			List<Declaration> currentTypeIIDuplicatedDeclarations = new ArrayList<Declaration>();
			currentTypeIIDuplicatedDeclarations.add(currentDeclaration);
			boolean mustAddCurrentTypeTwoDuplication = false;

			// for Apriori
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

				if (equals && !visitedIdenticalDeclarations.contains(currentDeclarationIndex)
						&& !visitedIdenticalDeclarations.contains(checkingDecIndex)) {

					// We have found type I duplication
					// We add the checkingDeclaration, it will add the Selector
					// itself.
					currentTypeIDuplicatedDeclarations.add(checkingDeclaration);
					visitedIdenticalDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeIDuplication = true;

					// This only used in apriori and fpgrowth
					newItem.add(checkingDeclaration);
					declarationItemMap.put(checkingDeclaration, newItem);
					newItem.addDuplicationType(1);
				}
				if (!equals && !visitedEquivalentDeclarations.contains(checkingDecIndex)
						    && (currentDeclaration.declarationIsEquivalent(checkingDeclaration))) {
					// || (equals && currentTypeIIDuplicatedDeclarations.size() > 1)) {
					/*
					 * We have found type II duplication
					 */
					currentTypeIIDuplicatedDeclarations.add(checkingDeclaration);
					visitedEquivalentDeclarations.add(checkingDecIndex);
					mustAddCurrentTypeTwoDuplication = true;

					newItem.add(checkingDeclaration);
					declarationItemMap.put(checkingDeclaration, newItem);
					newItem.addDuplicationType(2);
				}

			}

			// Only if we have at least one declaration in the list (at list one
			// duplication)
			if (mustAddCurrentTypeIDuplication) {
				if (typeOneDuplication
						.hasAllSelectorsForADuplication(currentTypeIDuplicatedDeclarations)) {
					typeOneDuplication
							.addAllDeclarations(currentTypeIDuplicatedDeclarations);
				} else {
					typeOneDuplication = new TypeOneDuplicationInstance();
					typeOneDuplication
							.addAllDeclarations(currentTypeIDuplicatedDeclarations);
					typeOneDuplicationsList.addDuplication(typeOneDuplication);
				}
			}

			if (mustAddCurrentTypeTwoDuplication) {
				if (typeTwoDuplication
						.hasAllSelectorsForADuplication(currentTypeIIDuplicatedDeclarations)) {
					typeTwoDuplication
							.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
				} else {
					typeTwoDuplication = new TypeTwoDuplicationInstance();
					typeTwoDuplication
							.addAllDeclarations(currentTypeIIDuplicatedDeclarations);
					typeTwoDuplicationsList.addDuplication(typeTwoDuplication);
				}
			}

		}

	}

	/**
	 * Finds typeIII duplications
	 */
	public void findTypeThreeDuplication() {

		typeThreeDuplicationsList = new DuplicationIncstanceList();

		for (Selector selector : stylesheet.getAllSelectors()) {

			for (ShorthandDeclaration virtualShorthand : selector.getVirtualShorthandDeclarations()) {
				
				// For each real shorthand in the style sheet, we compare it with the virtual shorthands of this selectors
				for (Declaration checkingDeclaration : stylesheet.getAllDeclarations()) {
					if (checkingDeclaration.getSelector() != selector && checkingDeclaration instanceof ShorthandDeclaration) {
						ShorthandDeclaration checkingShorthandDeclaration = (ShorthandDeclaration) checkingDeclaration;
						if(virtualShorthand.individualDeclarationsEquivalent(checkingShorthandDeclaration)) {
							TypeThreeDuplicationInstance duplication = 
									new TypeThreeDuplicationInstance(checkingShorthandDeclaration, virtualShorthand.getIndividualDeclarations());
							typeThreeDuplicationsList.addDuplication(duplication);

							/*
							 * Well, well, when we add individual declarations to a virtual shorthand, it does not add the real values to the
							 * virtual shorthand declaration itself. Indeed it is difficult to get values from individual shorthand declarations.
							 */
													
							//for (DeclarationValue v : checkingShorthandDeclaration.getValues())
							//	virtualShorthand.getValues().add(v.clone());

							// For apriori and FP-Growth
							Item item = declarationItemMap.get(checkingDeclaration);

							item.add(virtualShorthand, true);

							item.addDuplicationType(3);

							selector.addDeclaration(virtualShorthand);
							declarationItemMap.put(virtualShorthand, item);
						}
					}
				}
			}
		}

	}

	/**
	 * Finds type IV_A duplications
	 * 
	 * @see DuplicationInstanceType
	 */
	public void findTypeFourADuplication() {

		typeFourADuplicationsList = new DuplicationIncstanceList();
		int currentSelectorIndex = -1;
		List<Selector> allSelectors = new ArrayList<>();
		for (Selector s : stylesheet.getAllSelectors())
				allSelectors.add(s);
		
		Set<Integer> visitedSelectors = new HashSet<>();
		while (++currentSelectorIndex < allSelectors.size()) {
			if (visitedSelectors.contains(currentSelectorIndex))
				continue;
			Selector currentSelector = allSelectors.get(currentSelectorIndex);
			TypeFourDuplicationInstance.TypeIVADuplication newTypeIVADup = new TypeFourDuplicationInstance.TypeIVADuplication();
			newTypeIVADup.addSelector(currentSelector);
			int checkingSelectorIndex = currentSelectorIndex;
			while (++checkingSelectorIndex < allSelectors.size()) {
				if (visitedSelectors.contains(checkingSelectorIndex))
					continue;
				Selector checkingSelector = allSelectors
						.get(checkingSelectorIndex);
				if (currentSelector.selectorEquals(checkingSelector)) {
					visitedSelectors.add(checkingSelectorIndex);
					newTypeIVADup.addSelector(checkingSelector);
				}
			}
			if (newTypeIVADup.getSelectors().size() > 1) {
				typeFourADuplicationsList.addDuplication(newTypeIVADup);
			}
		}

	}

	/**
	 * Finds type IV_B duplications. For finding type IV_V duplication, we need
	 * a given DOM.
	 * 
	 * @param dom
	 * @see DuplicationInstanceType
	 */
	public void findTypeFourBDuplication(Document dom) {

		typeFourBDuplicationsList = new DuplicationIncstanceList();

		Map<BaseSelector, DOMNodeWrapperList> selectorNodeListMap = stylesheet
				.mapStylesheetOnDocument(dom);

		List<BaseSelector> allSelectors = new ArrayList<>(
				stylesheet.getAllBaseSelectors());

		int currentSelectorIndex = -1;

		Set<Integer> visitedSelectors = new HashSet<>();

		while (++currentSelectorIndex < allSelectors.size()) {

			if (visitedSelectors.contains(currentSelectorIndex))
				continue;

			Selector currentSelector = allSelectors.get(currentSelectorIndex);

			DOMNodeWrapperList currentNodeList = selectorNodeListMap
					.get(currentSelector);
			if (currentNodeList == null)
				continue;
			TypeIVBDuplication typeIVBDuplication = new TypeIVBDuplication();
			typeIVBDuplication.addSelector(currentSelector);
			int checkingSelectorIndex = 0;
			while (++checkingSelectorIndex < allSelectors.size()) {

				if (visitedSelectors.contains(checkingSelectorIndex))
					continue;

				Selector checkingSelector = allSelectors
						.get(checkingSelectorIndex);

				if (currentSelector.selectorEquals(checkingSelector))
					continue;

				DOMNodeWrapperList checkingNodeList = selectorNodeListMap
						.get(checkingSelector);
				if (checkingNodeList == null)
					continue;

				// In case of :hover, etc:
				// if (checkingSelector instanceof SimpleSelector) {
				// boolean mustBreak = false;
				// List<PseudoClass> pseudoClasses = ((SimpleSelector)
				// checkingSelector).getPseudoClasses();
				// for (PseudoClass ps : pseudoClasses)
				// if (ps.isPseudoclassWithNoXpathEquivalence()) {
				// if (currentSelector instanceof SimpleSelector) {
				// if (!((SimpleSelector)
				// currentSelector).getPseudoClasses().contains(ps))
				// mustBreak = true;
				// } else {
				// mustBreak = true;
				// break;
				// }
				// }
				// if (mustBreak)
				// continue;
				// }

				if (currentNodeList.equals(checkingNodeList)) {
					visitedSelectors.add(checkingSelectorIndex);
					typeIVBDuplication.addSelector(checkingSelector);
				}
			}
			if (typeIVBDuplication.getSelectors().size() > 1) {
				typeFourBDuplicationsList.addDuplication(typeIVBDuplication);
			}
		}
	}

	public List<ItemSetList> apriori(int minsup) {
		Apriori apriori = new Apriori(C1);
		return apriori.apriori(minsup);
	}

	public List<ItemSetList> fpGrowth(int minSupport, boolean removeSubsets) {
		
		LOGGER.info("Applying fpgrowth algorithm with minimum support count of " + minSupport + " on " + stylesheet.getFilePath());
		long start = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		
		List<TreeSet<Item>> itemSets = new ArrayList<>(stylesheet.getNumberOfSelectors());

		for (Selector s : stylesheet.getAllSelectors()) {
			TreeSet<Item> currentItems = new TreeSet<>();
			for (Declaration d : s.getDeclarations()) {
				Item item = declarationItemMap.get(d);
				if (item.getSupport().size() >= minSupport) {
					currentItems.add(item);
				}
			}
			if (currentItems.size() > 0)
				itemSets.add(currentItems);
		}

		FPGrowth fpGrowth = new FPGrowth(removeSubsets);
		List<ItemSetList> results = fpGrowth.mine(itemSets, minSupport);
		long end = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		long time = (end - start) / 1000000L;
		LOGGER.info("Done FP-Growth in " + time);
		
		return results;
	}

	/**
	 * Returns all the items including different type instances
	 * 
	 * @param type
	 * @return
	 */
	public Map<Integer, List<Item>> getItemsIncludingTypenstances() {

		Map<Integer, List<Item>> toReturn = new HashMap<>();

		for (int i = 1; i <= 3; i++)
			toReturn.put(i, new ArrayList<Item>());
		
		toReturn.put(12, new ArrayList<Item>());
		toReturn.put(23, new ArrayList<Item>());
		toReturn.put(13, new ArrayList<Item>());
		toReturn.put(123, new ArrayList<Item>());

		for (ItemSet itemSet : C1)
			for (Item item : itemSet) {
				Set<Integer> types = item.getDuplicationTypes();
//				for (int i : types)
//					toReturn.get(i).add(item);
				
				if (types.contains(1)) {
					if (types.contains(2)) {
						if (types.contains(3)) {
							toReturn.get(123).add(item); // 123
						} else { 
							toReturn.get(12).add(item); // Only 12
						}
					} else if (types.contains(3)) {
						toReturn.get(13).add(item); // Only 13
					} else {
						toReturn.get(1).add(item); // Only 1
					}
				} else if (types.contains(2)) {
					if (types.contains(3)) {
						toReturn.get(23).add(item); // Only 23
					} else {
						toReturn.get(2).add(item); // only 2
					}
				} else if (types.contains(3)) {
					toReturn.get(3).add(item); // only 3
				}
				
			}

		return toReturn;
	}
	
	/**
	 * Get the declarations with the same properties across different selectors
	 * @return
	 */
	public Map<String, Item> getDeclarationsWithTheSameProperties() {
			
		Map<String, Item> propertyToItemMapper = new HashMap<>();
		
		for (Declaration declaration : stylesheet.getAllDeclarations()) {
			
			// Skip the virtual shorthand declarations
//			if (declaration instanceof ShorthandDeclaration) {
//				ShorthandDeclaration shorthand = (ShorthandDeclaration)declaration;
//				if (shorthand.isVirtual())
//					continue;
//			}
			
			
			/*
			 * In case of individual properties which could be a part of a
			 * shorthand property 
			 */
			Set<String> correspondingProperties = ShorthandDeclaration.getShorthandPropertyNames(declaration.getProperty());
			correspondingProperties.add(declaration.getProperty());

			for (String property : correspondingProperties) {
	
				Item propertyItem = propertyToItemMapper.get(property);
				
				if (propertyItem == null) {	
					propertyItem = new Item();
					propertyToItemMapper.put(property, propertyItem);
				}
				
				propertyItem.add(declaration);
				
			}
			
		}
		
		return propertyToItemMapper;
	}

}
