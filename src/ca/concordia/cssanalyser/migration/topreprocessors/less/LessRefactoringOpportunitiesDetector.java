package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.fpgrowth.FPGrowth;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.PropertyItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorRefactoringOpportunity;

public class LessRefactoringOpportunitiesDetector {
	private StyleSheet styleSheet;
	public LessRefactoringOpportunitiesDetector(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	public List<PreprocessorRefactoringOpportunity> findMixinRefactoringOpportunities() {
		
		int minSupport = 2;

		List<PreprocessorRefactoringOpportunity> opportunities = new ArrayList<>();
		
		DuplicationDetector duplicationDetector = new DuplicationDetector(styleSheet);
		//duplicationDetector.findTypeThreeDuplication();
		Map<String, Item> duplicatedProperties = duplicationDetector.getDeclarationsWithTheSameProperties();
		
		if (duplicatedProperties.size() > 0) {
		
			List<TreeSet<Item>> itemSets = new ArrayList<>(styleSheet.getNumberOfSelectors());
	
			for (Selector s : styleSheet.getAllSelectors()) {
				TreeSet<Item> currentItems = new TreeSet<>();
				for (Declaration d : s.getDeclarations()) {
					String property = d.getProperty();
					Item item = duplicatedProperties.get(property);
					if (item.getSupport().size() >= minSupport) {
						currentItems.add(item);
					}
				}
				if (currentItems.size() > 0)
					itemSets.add(currentItems);
			}
			
			
			FPGrowth fpGrowth = new FPGrowth(false, new PropertyItemSetList());
			List<PropertyItemSetList> fpGrowthResults = fpGrowth.mine(itemSets, minSupport);
			
			for (int i = fpGrowthResults.size() - 1; i >= 0; i--) {
				
				// Foreach one we have a mixin refactoring opportunity?
				PropertyItemSetList isl = fpGrowthResults.get(i);
				
				//LessMixinRefactoringOpportunity mro = new 
				
			}
		
		}
		
		return opportunities;
		
	}
}
