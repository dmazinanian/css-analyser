package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class PreprocessorRefactoringOpportunitiesDetector {
	
	private final StyleSheet styleSheet;

	public PreprocessorRefactoringOpportunitiesDetector(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public Iterable<MixinRefactoringOpportunity> findMixinOpportunities() {
		
		// Apply FP-Growth on normal duplications
		DuplicationDetector duplicationDetector = new DuplicationDetector(this.styleSheet);
		duplicationDetector.findDuplications();
		List<ItemSetList> itemSetLists = duplicationDetector.fpGrowth(2, false);
		
		List<MixinRefactoringOpportunity> mixinRefactoringOpportunities = new ArrayList<>();
		
		/*
		 * Each of the duplications will be a opportunity.
		 * We will add declarations with differences as well.
		 */
		for (ItemSetList itemSetList : itemSetLists) {
			for (ItemSet itemSet : itemSetList) {
				
				// First, add all the equal or equivalent declarations to this opportunity
				List<Declaration> duplicatedDeclarations = itemSet.getRepresentativeDeclarations();
				
				// A Mixin refactoring opportunity also needs the selectors involved in the duplication
				List<Selector> itemSetSelectors = new ArrayList<>(itemSet.getSupport());
				
				List<DeclarationValueDifference> differentDeclarations = new ArrayList<>();
				
				// Try to add declarations with differences
				Selector firstSelector = itemSetSelectors.get(0);
				for (Declaration declarationInTheFirstSelector : firstSelector.getDeclarations()) {
					// We only care about remaining declarations, which are not equal or equivalent
					if (duplicatedDeclarations.contains(declarationInTheFirstSelector))
						continue;
					
					for (int i = 1; i < itemSetSelectors.size(); i++) {
						for (Declaration declarationInTheSecondSelector : itemSetSelectors.get(i).getDeclarations()) {
							
							// Again we only care about remaining declarations, which are not equal or equivalent
							if (duplicatedDeclarations.contains(declarationInTheSecondSelector))
								continue;

							if (declarationInTheFirstSelector.getProperty().equals(declarationInTheFirstSelector.getProperty())) {
								// Here we go: a difference in values should be there
								//DeclarationValueDifference difference = 
							} 
						}
						
						/*
						 * Now go for virtual shorthand declarations.
						 */
						//for (ShorthandDeclaration shorthand : itemSetSelectors.get(i).getVirtualShorthandDeclarations()) {
						//	// TODO
						//}
						
					}
				}
				
				
				MixinRefactoringOpportunity opportunity = new MixinRefactoringOpportunity(itemSetSelectors, duplicatedDeclarations, differentDeclarations);
				mixinRefactoringOpportunities.add(opportunity);
			}
		}
		
		
		
		
		
		
		return mixinRefactoringOpportunities;
		
	}

}
