package ca.concordia.cssanalyser.cssdiff;

public interface Difference {
	
	public enum DifferenceType {
		SELECTOR_REMOVED,
		SELECTOR_ADDEDD,
		SELECTOR_CHANGED,
		DECLARATION_ADDED,
		DECLARATION_REMOVED,
		DECLARATION_VALUE_CHANGED
	}

	
}
