package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationFactory;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public abstract class PreprocessorMigrationOpportunitiesDetector {
	
	private final StyleSheet styleSheet;
	
	protected abstract MixinMigrationOpportunity getNewPreprocessorSpecificOpportunity(Iterable<Selector> forSelectors);

	public PreprocessorMigrationOpportunitiesDetector(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public List<MixinMigrationOpportunity> findMixinOpportunities() {
		
		// Apply FP-Growth on normal duplications
		DuplicationDetector duplicationDetector = new DuplicationDetector(this.styleSheet);
		duplicationDetector.findPropertyDuplications();
		// Subsets will not be there
		List<ItemSetList> itemSetLists = duplicationDetector.fpGrowth(2, false);
		
		List<MixinMigrationOpportunity> mixinMigrationOpportunities = new ArrayList<>();
		
		/*
		 * Each of the duplications can be a opportunity.
		 * We will add declarations with differences as well.
		 */
		for (ItemSetList itemSetList : itemSetLists) {
			
			for (ItemSet itemSet : itemSetList) {
				
				MixinMigrationOpportunity opportunity = getNewPreprocessorSpecificOpportunity(itemSet.getSupport());
				mixinMigrationOpportunities.add(opportunity);
				
				// A Mixin refactoring opportunity also needs the selectors involved in the duplication
				List<Selector> itemSetSelectors = new ArrayList<>();
				for (Selector s : itemSet.getSupport())
					itemSetSelectors.add(s);
				
				// Make intra-selector overriding dependencies similar
				similarizeDependencies(itemSetSelectors);
				
				// First, add all the equal or equivalent declarations to this opportunity
//				for (Item item : itemSet)
//					opportunity.addEquivalentDeclarations(item);
				
				// We want to skip the declarations in the ItemSet (equivalent ones)
//				Set<Declaration> declarationsInTheItemset = new HashSet<>();
//				for (Item item : itemSet)
//					for (Declaration declaration : item)
//						declarationsInTheItemset.add(declaration);
				
				
				Map<Selector, List<Declaration>> selectorToDeclarationsMap = new HashMap<>();
				Map<Selector, Set<Integer>> checkedSelectors = new HashMap<>();
				for (Selector s : itemSetSelectors) {
					List<Declaration> l = new ArrayList<>();
					for (Declaration declaration : s.getAllDeclarationsIncludingVirtualShorthandDeclarations()) {
						l.add(declaration);
					}
					selectorToDeclarationsMap.put(s, l);
					checkedSelectors.put(s, new HashSet<Integer>());
				}
				
				// Try to add declarations having the same property
				Selector firstSelector = itemSetSelectors.get(0);
				List<Declaration> declarationsInTheFirstSelector = selectorToDeclarationsMap.get(firstSelector);
				for (int declarationIndex = 0; declarationIndex < declarationsInTheFirstSelector.size(); declarationIndex++) {
					Declaration declarationInTheFirstSelector = declarationsInTheFirstSelector.get(declarationIndex);
					
					Set<Integer> checkedDeclarationsInTheFirstSelector = checkedSelectors.get(firstSelector);
					if (checkedDeclarationsInTheFirstSelector.contains(declarationIndex))
						continue;
					
					// Find out if another (real) declaration is overriding this one
					checkedDeclarationsInTheFirstSelector.add(declarationIndex);
					for (int k = declarationIndex + 1; k < declarationsInTheFirstSelector.size(); k++) {
						Declaration checkingDeclarationForOverriding = declarationsInTheFirstSelector.get(k);
						if (!checkedDeclarationsInTheFirstSelector.contains(k) &&
								checkingDeclarationForOverriding.getProperty().equals(declarationInTheFirstSelector.getProperty())) {
							if (checkingDeclarationForOverriding instanceof ShorthandDeclaration && ((ShorthandDeclaration) checkingDeclarationForOverriding).isVirtual()) {
								continue;
							} 
							declarationInTheFirstSelector = checkingDeclarationForOverriding;
							checkedDeclarationsInTheFirstSelector.add(k);
						}
					}
					
					List<Declaration> declarationsToAdd = new ArrayList<>();
					declarationsToAdd.add(declarationInTheFirstSelector);
					
					// Compare all other declarations in other selectors with the current declaration in the first selector
					for (int selectorIndex = 1; selectorIndex < itemSetSelectors.size(); selectorIndex++) {
						
						Selector secondSelector = itemSetSelectors.get(selectorIndex);
						List<Declaration> declarationsInTheSecondSelector = selectorToDeclarationsMap.get(secondSelector);
						List<Integer> declarationsWithTheSameProperty = new ArrayList<>();
						// First collect all the declarations with the same property in this selector, preserving the order
						for (int declaration2Index = 0; declaration2Index < declarationsInTheSecondSelector.size(); declaration2Index++) {
							
							Declaration declarationInTheSecondSelector = declarationsInTheSecondSelector.get(declaration2Index);
							
							// Again we only care about remaining declarations, which are not equal or equivalent
							// Also don't match a declaration two times. Will it happen?!
							Set<Integer> checkedDeclarationsInTheSecondSelector = checkedSelectors.get(secondSelector);
							if (checkedDeclarationsInTheSecondSelector.contains(declaration2Index))
								continue;

							if (declarationInTheFirstSelector.getProperty().equals(declarationInTheSecondSelector.getProperty())) {
								// Here we go: a difference in values should be there
								checkedDeclarationsInTheSecondSelector.add(declaration2Index);	
								declarationsWithTheSameProperty.add(declaration2Index);
							} 	
						}
						// Get the last real declaration. If not exists, get the last declaration whatever it is.
						int declarationToBeAddedIndex = -1;
						if (declarationsWithTheSameProperty.size() > 0) {
							int index = declarationsWithTheSameProperty.size() - 1;
							do {
								if (index >= 0) {
									declarationToBeAddedIndex = declarationsWithTheSameProperty.get(index);
									index--;
								} else {
									declarationToBeAddedIndex = declarationsWithTheSameProperty.get(declarationsWithTheSameProperty.size() - 1);
									break;
								}
							} while (declarationsInTheSecondSelector.get(declarationToBeAddedIndex) instanceof ShorthandDeclaration && 
									((ShorthandDeclaration) declarationsInTheSecondSelector.get(declarationToBeAddedIndex)).isVirtual());
						}


						// This approach lets us mimic overriding declarations with the same property
						if (declarationToBeAddedIndex >= 0)
							declarationsToAdd.add(declarationsInTheSecondSelector.get(declarationToBeAddedIndex));
					}	
					// If the current declaration is present in all selectors
					if (declarationsToAdd.size() == itemSetSelectors.size()) {
						/*
						 * Check if one of the declarations is a virtual shorthand,
						 * in this case, we add individual declarations instead 
						 */
						if (declarationsToAdd.get(0) instanceof ShorthandDeclaration) {
							boolean shouldAddIndividuals = false;
							Map<String, List<Declaration>> propertyToIndividualsMap = new HashMap<>();
							for (Declaration d : declarationsToAdd) {
								ShorthandDeclaration shorthandDeclaration = (ShorthandDeclaration) d;
								if (shorthandDeclaration.isVirtual())
									shouldAddIndividuals = true;
								for (Declaration individual : shorthandDeclaration.getIndividualDeclarations()) {
									List<Declaration> individualsHavingTheSameProperty = propertyToIndividualsMap.get(individual.getProperty());
									if (individualsHavingTheSameProperty == null) {
										individualsHavingTheSameProperty = new ArrayList<>();
										propertyToIndividualsMap.put(individual.getProperty(), individualsHavingTheSameProperty);
									}
									individualsHavingTheSameProperty.add(individual);
								}

							}
							
							if (!shouldAddIndividuals) {
								// Add the declarations themselves
								opportunity.addDeclarations(declarationInTheFirstSelector.getProperty(), declarationsToAdd);
							} else {
								// Add the individuals
								for (String individualProperty : propertyToIndividualsMap.keySet()) {
									opportunity.addDeclarations(individualProperty, propertyToIndividualsMap.get(individualProperty));
								}
							}
								
						} else {
							opportunity.addDeclarations(declarationInTheFirstSelector.getProperty(), declarationsToAdd);
						}
					}
				}
			}
		}
		
		return mixinMigrationOpportunities;
		
	}

	/**
	 * Making dependencies similar by adding new declarations to the selectors 
	 * @param itemSetSelectors 
	 */
	private void similarizeDependencies(List<Selector> itemSetSelectors) {
		//  This holds all value overriding dependencies across all selectors
		List<CSSValueOverridingDependency> allValueOverridingDependencies = new CSSValueOverridingDependencyList();
		
		// Maps each selector to the indices of the value overriding dependencies in allValueOverridingDependencies
		Map<Selector, Set<Integer>> overridingDependencies = new HashMap<>();
		
		for (Selector selector : itemSetSelectors) {
			CSSValueOverridingDependencyList valueOverridingDependenciesForSelector = null;
			if (selector instanceof BaseSelector) {
				valueOverridingDependenciesForSelector = CSSDependencyDetector.getValueOverridingDependenciesForSelector((BaseSelector) selector);
				
			} else if (selector instanceof GroupingSelector) {
				valueOverridingDependenciesForSelector = CSSDependencyDetector.getValueOverridingDependenciesForSelector(((GroupingSelector) selector).getBaseSelectors().iterator().next());
			}
			int startFromIndex = allValueOverridingDependencies.size();
			for (CSSValueOverridingDependency dependency : valueOverridingDependenciesForSelector) {
				allValueOverridingDependencies.add(dependency);
			}
			int endToIndex = allValueOverridingDependencies.size() - 1;
			Set<Integer> valueOverridingDependenciesIndicesForSelector = new HashSet<>();
			for (int i = startFromIndex; i <= endToIndex; i++)
				valueOverridingDependenciesIndicesForSelector.add(i);
			overridingDependencies.put(selector, valueOverridingDependenciesIndicesForSelector);
		}
		Set<Integer> visitedDependencies = new HashSet<>();
		for (int i = 0; i < allValueOverridingDependencies.size(); i++) {
			CSSValueOverridingDependency dependency = allValueOverridingDependencies.get(i);
			if (visitedDependencies.contains(i))
				continue;
			for (Selector selector : itemSetSelectors) {
				// if the dependency belongs to this selector, ignore it
				if (dependency.getSelector1().equals(selector)) { //intra, only one selector is enough
					continue;
				}
				// See if such a dependency exist in this selector
				boolean dependencyFound = false;
				for (int j : overridingDependencies.get(selector)) {
					CSSValueOverridingDependency dependencyInThisSelector = allValueOverridingDependencies.get(j);
					if (dependencyInThisSelector.getDeclaration1().getProperty().equals(dependency.getDeclaration1().getProperty()) && 
							dependencyInThisSelector.getDeclaration2().getProperty().equals(dependency.getDeclaration2().getProperty())) {
						dependencyFound = true;
						visitedDependencies.add(j);
						break;
					}
				}
				if (!dependencyFound) {
					Declaration startingDeclaration = null;
					// Make a declaration to satisfy the dependency, if needed!
					for (Declaration d : selector.getDeclarations()) {
						if (d.getProperty().equals(dependency.getDeclaration1().getProperty())) {
							startingDeclaration = d;
							break;
						}
					}
					if (startingDeclaration != null) {
						Declaration declaration2 = dependency.getDeclaration2();
						List<DeclarationValue> values = new ArrayList<>();
						for (PropertyAndLayer propertyAndLayer : declaration2.getAllSetPropertyAndLayers()) {

							for (DeclarationValue declarationValue : startingDeclaration.getDeclarationValuesForStyleProperty(propertyAndLayer)) {
								values.add(declarationValue);
							}								

						}
						Declaration newVirtualDeclaration = DeclarationFactory.getDeclaration(declaration2.getProperty(), 
								values, selector, declaration2.isImportant(), false, new LocationInfo());
						selector.addDeclaration(newVirtualDeclaration);
					}
				}
			}
		}		
	}

}
