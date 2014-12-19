package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
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
		 * Each of the duplications will be a opportunity.
		 * We will add declarations with differences as well.
		 */
		for (ItemSetList itemSetList : itemSetLists) {
			
			for (ItemSet itemSet : itemSetList) {
				
				MixinMigrationOpportunity opportunity = getNewPreprocessorSpecificOpportunity(itemSet.getSupport());
				mixinRefactoringOpportunities.add(opportunity);
				
				// First, add all the equal or equivalent declarations to this opportunity
				List<Declaration> declarationsInTheItemset = itemSet.getRepresentativeDeclarations();
				
				for (Item item : itemSet)
					opportunity.addEquivalentDeclarations(item);
				
				// A Mixin refactoring opportunity also needs the selectors involved in the duplication
				List<Selector> itemSetSelectors = new ArrayList<>();
				for (Selector s : itemSet.getSupport())
					itemSetSelectors.add(s);
			
				// Try to add declarations with differences
				Selector firstSelector = itemSetSelectors.get(0);
				for (Declaration declarationInTheFirstSelector : firstSelector.getDeclarations()) {
					// We only care about remaining declarations, which are not equal or equivalent
					if (declarationsInTheItemset.contains(declarationInTheFirstSelector))
						continue;
					
					List<Declaration> declarationsToAdd = new ArrayList<>();
					declarationsToAdd.add(declarationInTheFirstSelector);
					
					// Compare all other declarations in other selectors with the current declaration in the first selector
					for (int i = 1; i < itemSetSelectors.size(); i++) {
						
						for (Declaration declarationInTheSecondSelector : itemSetSelectors.get(i).getDeclarations()) {
							
							// Again we only care about remaining declarations, which are not equal or equivalent
							if (declarationsInTheItemset.contains(declarationInTheSecondSelector))
								continue;

							if (declarationInTheFirstSelector.getProperty().equals(declarationInTheSecondSelector.getProperty())) {
								// Here we go: a difference in values should be there
								// Every value can be a difference
								
								declarationsToAdd.add(declarationInTheSecondSelector);
								
							}

						} 


						/*
						 * Now go for virtual shorthand declarations.
						 */
//						for (ShorthandDeclaration shorthand : itemSetSelectors.get(i).getVirtualShorthandDeclarations()) {
//							// TODO
//						}

					}
					
					// If declarations are present in all selectors
					if (declarationsToAdd.size() == itemSetSelectors.size()) {
						opportunity.addDeclarationsWithDifferences(declarationsToAdd);
					}
				}
			}
		}
		
		return mixinRefactoringOpportunities;
		
	}

}
