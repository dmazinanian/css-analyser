package ca.concordia.cssanalyser.migration.topreprocessors;

public abstract class PreprocessorMigrationOpportunity {
	private double rank;

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}
	
}
