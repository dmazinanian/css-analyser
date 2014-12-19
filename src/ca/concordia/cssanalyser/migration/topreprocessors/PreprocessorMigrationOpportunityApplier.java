package ca.concordia.cssanalyser.migration.topreprocessors;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;

public interface PreprocessorMigrationOpportunityApplier<T> {
	
	public T apply(MixinMigrationOpportunity opportunity, StyleSheet styleSheet);
	
}
