package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;

public abstract class PreprocessorMigrationOpportunitiesDetector<T> {
	
	private final StyleSheet styleSheet;
	
	protected abstract MixinMigrationOpportunity<T> getNewPreprocessorSpecificOpportunity(Iterable<Selector> forSelectors);

	public PreprocessorMigrationOpportunitiesDetector(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	public StyleSheet getStyleSheet() {
		return this.styleSheet;
	}

	public List<? extends MixinMigrationOpportunity<T>> findMixinOpportunities() {
		
		// Remove intra-selector dependencies!
		StyleSheet styleSheetWithRemovedDependencies = this.styleSheet.getStyleSheetWithIntraSelectorDependenciesRemoved();
		
		// Apply FP-Growth on property duplications
		DuplicationDetector duplicationDetector = new DuplicationDetector(styleSheetWithRemovedDependencies);
		duplicationDetector.findPropertyDuplications();
	
		List<ItemSetList> itemSetLists = duplicationDetector.fpGrowth(2, false);
			
		List<MixinMigrationOpportunity<T>> mixinMigrationOpportunities = new ArrayList<>();
			
		/*
		 * Each of the item sets is an opportunity.
		 * Because each of them consist of a list of selectors having declarations with the same properties 
		 */
		for (ItemSetList itemSetList : itemSetLists) {
			
			for (ItemSet itemSet : itemSetList) {
				
				MixinMigrationOpportunity<T> opportunity = getNewPreprocessorSpecificOpportunity(itemSet.getSupport());
				mixinMigrationOpportunities.add(opportunity);
				
				// Declarations in a item have the same property
				for (Item item : itemSet) {
					List<Declaration> declarationsToAdd = new ArrayList<>();
					/*
					 * Check if all of the declarations are virtual shorthand,
					 * in this case, we add individual declarations instead 
					 */
					boolean allVirtual = true;
					// This loop does two things! 
					for (Declaration d : item) {
						if (d instanceof ShorthandDeclaration && !((ShorthandDeclaration)d).isVirtual()) {
							allVirtual = false;
						}
						if (itemSet.supportContains(d.getSelector()))
							declarationsToAdd.add(d);
					}
					// What if all of them are single or multi-valued? you need an extra check
					if (ShorthandDeclaration.isShorthandProperty(declarationsToAdd.get(0).getProperty()) && allVirtual) {
						ShorthandDeclaration referenceVirtualShorthand = (ShorthandDeclaration)declarationsToAdd.get(0);
						for (Declaration referenceIndividual : referenceVirtualShorthand.getIndividualDeclarations()) {
							List<Declaration> individualDeclarationsToAdd = new ArrayList<>();
							individualDeclarationsToAdd.add(referenceIndividual);
							String referenceVirtualShorthandProperty = referenceIndividual.getProperty();
							for (int i = 1; i < declarationsToAdd.size(); i++) {
								ShorthandDeclaration virtualShorthand = (ShorthandDeclaration)declarationsToAdd.get(i);
								Declaration individualForThisProperty = 
										virtualShorthand.getIndividualDeclarationForProperty(referenceVirtualShorthandProperty);
								individualDeclarationsToAdd.add(individualForThisProperty);
							}
							opportunity.addDeclarationsWithTheSameProperty(individualDeclarationsToAdd);
						}
					} else { // If not all the declarations are virtual shorthands
						opportunity.addDeclarationsWithTheSameProperty(declarationsToAdd);	
					}
				}
			}
		}
		
		return mixinMigrationOpportunities;
		
	}
}
