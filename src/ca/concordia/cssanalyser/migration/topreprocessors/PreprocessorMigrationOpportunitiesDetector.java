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
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
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
				List<Declaration> declarationsInTheFirstSlector = new ArrayList<>();
				for (Declaration d : firstSelector.getAllDeclarationsIncludingVirtualShorthandDeclarations())
					declarationsInTheFirstSlector.add(d);
				Set<Integer> checkedDeclarationsInTheFirstSelector = new HashSet<>();
				for (int declarationIndex = 0; declarationIndex < declarationsInTheFirstSlector.size(); declarationIndex++) {
					Declaration declarationInTheFirstSelector = declarationsInTheFirstSlector.get(declarationIndex);
					
					// We only care about remaining declarations, which are not equal or equivalent
					if (checkedDeclarationsInTheFirstSelector.contains(declarationIndex) || declarationsInTheItemset.contains(declarationInTheFirstSelector))
						continue;
					
					// Find out if another (real) declaration is overriding this one
					checkedDeclarationsInTheFirstSelector.add(declarationIndex);
					for (int k = declarationIndex + 1; k < declarationsInTheFirstSlector.size(); k++) {
						Declaration checkingDeclarationForOverriding = declarationsInTheFirstSlector.get(k);
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
					for (int i = 1; i < itemSetSelectors.size(); i++) {
						
						Declaration declarationToBeAdded = null;
						for (Declaration declarationInTheSecondSelector : itemSetSelectors.get(i).getAllDeclarationsIncludingVirtualShorthandDeclarations()) {
							
							// Again we only care about remaining declarations, which are not equal or equivalent
							if (declarationsInTheItemset.contains(declarationInTheSecondSelector))
								continue;

							if (declarationInTheFirstSelector.getProperty().equals(declarationInTheSecondSelector.getProperty())) {
								// Here we go: a difference in values should be there
								if (declarationInTheSecondSelector instanceof ShorthandDeclaration && ((ShorthandDeclaration) declarationInTheSecondSelector).isVirtual())
									continue;
								declarationToBeAdded = declarationInTheSecondSelector; 
							} 
						} 
						// This approach lets us mimic overriding declarations with the same property
						declarationsToAdd.add(declarationToBeAdded);
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
								opportunity.addDeclarationsWithDifferences(declarationInTheFirstSelector.getProperty(), declarationsToAdd);
							} else {
								// Add the individuals
								for (String individualProperty : propertyToIndividualsMap.keySet()) {
									// Should add as different or equivalent?
									boolean allEquivalent = true;
									// If all the values are missing (having default values) for all the declarations, the declarations should not be added!
									boolean allValuesMissing = true;
									List<Declaration> declarations = propertyToIndividualsMap.get(individualProperty);
									// Compare everything with the declaration in the first selector
									Declaration groundTruth = declarations.get(0);
									for (DeclarationValue dv : groundTruth.getDeclarationValues()) {
										allValuesMissing &= dv.isAMissingValue();
									}
									for (int i = 1; i < declarations.size(); i++) {
										if (!groundTruth.declarationIsEquivalent(declarations.get(i))) {
											allEquivalent = false;
											break;
										} else {
											for (DeclarationValue dv : declarations.get(i).getDeclarationValues()) {
												allValuesMissing &= dv.isAMissingValue();
											}
										}
									}
									if (allEquivalent) {
										if (!allValuesMissing)
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
