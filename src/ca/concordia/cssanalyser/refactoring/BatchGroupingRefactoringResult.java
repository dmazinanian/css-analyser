package ca.concordia.cssanalyser.refactoring;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

public class BatchGroupingRefactoringResult implements RefactoringResults {
	private final StyleSheet styleSheet;
	private final int numberOfAppliedRefactorings;
	private final int numberOfPositiveRefactorins;
	public BatchGroupingRefactoringResult(StyleSheet styleSheet, int appliedRefactorings, int positiveRefactorins) {
		this.styleSheet = styleSheet;
		this.numberOfAppliedRefactorings = appliedRefactorings;
		this.numberOfPositiveRefactorins = positiveRefactorins;
	}
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}
	public int getNumberOfAppliedRefactorings() {
		return numberOfAppliedRefactorings;
	}
	public int getNumberOfPositiveRefactorins() {
		return numberOfPositiveRefactorins;
	}
}
