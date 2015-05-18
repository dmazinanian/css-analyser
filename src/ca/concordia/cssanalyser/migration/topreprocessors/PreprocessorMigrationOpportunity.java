package ca.concordia.cssanalyser.migration.topreprocessors;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

public abstract class PreprocessorMigrationOpportunity {
	
	private double rank;
	private final StyleSheet styleSheet;
	
	public PreprocessorMigrationOpportunity(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}
	
	public StyleSheet getStyleSheet() {
		return this.styleSheet;
	}
	
}
