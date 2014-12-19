package ca.concordia.cssanalyser.migration.topreprocessors.less;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;


public class LessMigrationOpportunitiesDetector extends PreprocessorMigrationOpportunitiesDetector {

	public LessMigrationOpportunitiesDetector(StyleSheet styleSheet) {
		super(styleSheet);
	}

	@Override
	public MixinMigrationOpportunity getNewPreprocessorSpecificOpportunity(Iterable<Selector> forSelectors) {
		return new LessMixinRefactoringOpportunity(forSelectors);
	}
	
}
