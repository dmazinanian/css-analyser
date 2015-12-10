package ca.concordia.cssanalyser.migration.topreprocessors;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMigrationOpportunitiesDetector;

public class PreprocessorMigrationOpportunitiesDetectorFactory {
	
	public static PreprocessorMigrationOpportunitiesDetector<?> get(PreprocessorType type, StyleSheet styleSheet) {
		switch (type) {
		case LESS:
			return new LessMigrationOpportunitiesDetector(styleSheet);
		case SASS:
		case SCSS:
		default:
			throw new RuntimeException("Not yet implemented");
		}
	}
	
}
