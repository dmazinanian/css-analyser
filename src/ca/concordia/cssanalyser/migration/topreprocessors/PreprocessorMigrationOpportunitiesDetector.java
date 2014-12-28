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
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;

public abstract class PreprocessorMigrationOpportunitiesDetector {
	
	private final StyleSheet styleSheet;
	
	protected abstract MixinMigrationOpportunity getNewPreprocessorSpecificOpportunity(Iterable<Selector> forSelectors);

	public PreprocessorMigrationOpportunitiesDetector(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public List<MixinMigrationOpportunity> findMixinOpportunities() {
		
		// Apply FP-Growth on normal duplications
		DuplicationDetector duplicationDetector = new DuplicationDetector(this.styleSheet);
		duplicationDetector.findDuplications();
		// Subsets will not be there
		List<ItemSetList> itemSetLists = duplicationDetector.fpGrowth(2, true);
		
		List<MixinMigrationOpportunity> mixinRefactoringOpportunities = new ArrayList<>();
		
		/*
		 * Each of the duplications can be a opportunity.
		 * We will add declarations with differences as well.
		 */
		for (ItemSetList itemSetList : itemSetLists) {
			
			for (ItemSet itemSet : itemSetList) {
				
				MixinMigrationOpportunity opportunity = getNewPreprocessorSpecificOpportunity(itemSet.getSupport());
				mixinRefactoringOpportunities.add(opportunity);
				

				// First, add all the equal or equivalent declarations to this opportunity
				for (Item item : itemSet)
					opportunity.addEquivalentDeclarations(item);
				
				// A Mixin refactoring opportunity also needs the selectors involved in the duplication
				List<Selector> itemSetSelectors = new ArrayList<>();
				for (Selector s : itemSet.getSupport())
					itemSetSelectors.add(s);
				
				// We want to skip the declarations in the ItemSet (equivalent ones)
				Set<Declaration> declarationsInTheItemset = new HashSet<>();
				for (Item item : itemSet)
					for (Declaration declaration : item)
						declarationsInTheItemset.add(declaration);
			
				// Try to add declarations with differences
				Selector firstSelector = itemSetSelectors.get(0);
				Iterable<Declaration> declarationsInTheFirstSlector = firstSelector.getAllDeclarationsIncludingVirtualShorthandDeclarations();
				
				for (Declaration declarationInTheFirstSelector : declarationsInTheFirstSlector) {
					// We only care about remaining declarations, which are not equal or equivalent
					if (declarationsInTheItemset.contains(declarationInTheFirstSelector))
						continue;
					
					List<Declaration> declarationsToAdd = new ArrayList<>();
					declarationsToAdd.add(declarationInTheFirstSelector);
					
					// Compare all other declarations in other selectors with the current declaration in the first selector
					for (int i = 1; i < itemSetSelectors.size(); i++) {
						
						for (Declaration declarationInTheSecondSelector : itemSetSelectors.get(i).getAllDeclarationsIncludingVirtualShorthandDeclarations()) {
							
							// Again we only care about remaining declarations, which are not equal or equivalent
							if (declarationsInTheItemset.contains(declarationInTheSecondSelector))
								continue;

							if (declarationInTheFirstSelector.getProperty().equals(declarationInTheSecondSelector.getProperty())) {
								// Here we go: a difference in values should be there
								declarationsToAdd.add(declarationInTheSecondSelector);
								break;
							} 
						} 
						
					}
					// If declarations are present in all selectors
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
								opportunity.addDeclarationsWithDifferences(declarationInTheFirstSelector.getProperty(), declarationsToAdd);
							} else {
								// Add the individuals
								for (String individualProperty : propertyToIndividualsMap.keySet()) {
									// Should add as different or equivalent?
									boolean allEquivalent = true;
									List<Declaration> declarations = propertyToIndividualsMap.get(individualProperty);
									Declaration groundTruth = declarations.get(0);
									for (int i = 1; i < declarations.size(); i++) {
										if (!groundTruth.declarationIsEquivalent(declarations.get(i))) {
											allEquivalent = false;
											break;
										}
									}
									if (allEquivalent) {
										opportunity.addEquivalentDeclarations(individualProperty, declarations);
									} else { 
										opportunity.addDeclarationsWithDifferences(individualProperty, declarations);
									}
								}
							}
								
						} else {
							opportunity.addDeclarationsWithDifferences(declarationInTheFirstSelector.getProperty(), declarationsToAdd);	
						}
					}
				}
			}
		}
		
		return mixinRefactoringOpportunities;
		
	}

}
