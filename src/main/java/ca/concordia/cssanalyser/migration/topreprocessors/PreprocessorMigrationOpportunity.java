package ca.concordia.cssanalyser.migration.topreprocessors;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

public abstract class PreprocessorMigrationOpportunity<T> {
	
	private final StyleSheet styleSheet;
	
	public PreprocessorMigrationOpportunity(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public abstract double getRank();
	
	public StyleSheet getStyleSheet() {
		return this.styleSheet;
	}
	
	public abstract T apply();
	
}
