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

	public List<? extends MixinMigrationOpportunity<T>> findMixinOpportunities(boolean removeSubsets) {

		// Apply FP-Growth on property duplications
		DuplicationDetector duplicationDetector = new DuplicationDetector(this.styleSheet);
		duplicationDetector.findPropertyDuplications();

		List<ItemSetList> itemSetLists = duplicationDetector.fpGrowth(2, removeSubsets);

		List<MixinMigrationOpportunity<T>> mixinMigrationOpportunities = new ArrayList<>();

		/*
		 * Each of the item sets is an opportunity.
		 * Because each of them consist of a list of selectors having declarations with the same properties 
		 */
		for (ItemSetList itemSetList : itemSetLists) {

			for (ItemSet itemSet : itemSetList) {
				
				MixinMigrationOpportunity<T> opportunity = getMixinOpportunityFromItemSet(itemSet);
				mixinMigrationOpportunities.add(opportunity);
				
			}
		}

		return mixinMigrationOpportunities;

	}

	public MixinMigrationOpportunity<T> getMixinOpportunityFromItemSet(ItemSet itemSet) {
		MixinMigrationOpportunity<T> opportunity = getNewPreprocessorSpecificOpportunity(itemSet.getSupport());

		// Declarations in an Item have the same property
		for (Item item : itemSet) {
			List<Declaration> declarationsToAdd = new ArrayList<>();
			/*
			 * Check if all of the declarations are virtual shorthand,
			 * in this case, we add individual declarations instead 
			 */
			boolean hasVirtual = false;
			// This loop does two things!
			for (Declaration d : item) {
				if (d instanceof ShorthandDeclaration && ((ShorthandDeclaration)d).isVirtual()) {
					hasVirtual = true;
				}
				if (itemSet.supportContains(d.getSelector()))
					declarationsToAdd.add(d);
			}
			// Break down, when there is a shorthand, because you don't know if you can parameterize
			if (hasVirtual) {
				ShorthandDeclaration referenceVirtualShorthand = (ShorthandDeclaration)declarationsToAdd.get(0);
				for (Declaration referenceIndividual : referenceVirtualShorthand.getIndividualDeclarationsAtTheDeepestLevel()) {
					List<Declaration> individualDeclarationsToAdd = new ArrayList<>();
					individualDeclarationsToAdd.add(referenceIndividual);
					String referenceVirtualShorthandProperty = referenceIndividual.getProperty();
					for (int i = 1; i < declarationsToAdd.size(); i++) {
						ShorthandDeclaration virtualShorthand = (ShorthandDeclaration)declarationsToAdd.get(i);
						Declaration individualForThisProperty =
								virtualShorthand.getIndividualDeclarationForPropertyAtTheDeepestLevel(referenceVirtualShorthandProperty);
						if (individualForThisProperty == null) {
							individualForThisProperty = virtualShorthand.getIndividualDeclarationForPropertyAtTheDeepestLevel(referenceVirtualShorthandProperty);
						}
						individualDeclarationsToAdd.add(individualForThisProperty);
					}
					opportunity.addDeclarationsWithTheSameProperty(individualDeclarationsToAdd);
				}
			} else { // If none of the declarations are virtual shorthands
				opportunity.addDeclarationsWithTheSameProperty(declarationsToAdd);
			}
		}
		return opportunity;
	}
}
