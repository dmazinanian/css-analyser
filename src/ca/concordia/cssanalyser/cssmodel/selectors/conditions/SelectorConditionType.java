package ca.concordia.cssanalyser.cssmodel.selectors.conditions;

/**
 * All CSS3 Selector condition types which come inside
 * brackets, like element[attr], element[attr=value], etc.
 * @author Davood Mazinanian
 *
 */
public enum SelectorConditionType {
	/**
	 * [attr] condition
	 */
	HAS_ATTRIBUTE 						(""), 	// [attribute]
	/**
	 * [attr=value] condition
	 */
	VALUE_EQUALS_EXACTLY				("="), 	// [attr=blah]
	/**
	 * [attr~=value]
	 */
	VALUE_CONTAINS_WORD_SPACE_SEPARATED	("~="),	// [attribute~=value]
	/**
	 * [attr|=value]
	 */
	VALUE_START_WITH_DASH_SEPARATED 	("|="),	// [attribute|=value]
	/**
	 * [attr$=value]
	 */
	VALUE_ENDS_WITH 					("$="),	// [attribute$=val]
	/**
	 * [attr*=value]
	 */
	VALUE_CONTAINS						("*="), // [attribute*=value]
	/**
	 * [attr^=value]
	 */
	VALUE_STARTS_WITH					("^="); // [attribute^=value]
	
	
	private String operatorString = "";
	private SelectorConditionType(String operator) {
		operatorString = operator; 
	}
	
	@Override
	public String toString() {
		return operatorString;
	}
	
}
