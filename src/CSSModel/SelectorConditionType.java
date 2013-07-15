package CSSModel;

public enum SelectorConditionType {
	HAS_ATTRIBUTE 						(""), 	// [attribute]
	VALUE_EQUALS_EXACTLY				("="), 	// [attr=blah]
	VALUE_CONTAINS_WORD_SPACE_SEPARATED	("~="),	// [attribute~=value] 
	VALUE_START_WITH_DASH_SEPARATED 	("|="),	// [attribute|=value]
	VALUE_ENDS_WITH 					("$="),	// [attribute$=val]
	VALUE_CONTAINS						("*="), // [attribute*=value]
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
