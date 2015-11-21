package ca.concordia.cssanalyser.migration.topreprocessors.less;

import java.util.List;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetector;


public class LessMigrationOpportunitiesDetector extends PreprocessorMigrationOpportunitiesDetector<com.github.sommeri.less4j.core.ast.StyleSheet> {

	public LessMigrationOpportunitiesDetector(StyleSheet styleSheet) {
		super(styleSheet);
	}

	@Override
	public LessMixinMigrationOpportunity getNewPreprocessorSpecificOpportunity(Iterable<Selector> forSelectors) {
		return new LessMixinMigrationOpportunity(forSelectors, getStyleSheet());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<LessMixinMigrationOpportunity> findMixinOpportunities() {
		return (List<LessMixinMigrationOpportunity>) super.findMixinOpportunities();
	}
	
}
