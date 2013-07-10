package CSSModel;

public enum SelectorConditionType {
	HAS_ATTRIBUTE 			(""), 	// [attribute]
	VALUE_EQUALS_EXACTLY	("="), 	// [attr=blah]
	VALUE_CONTAINS_WORD 	("~="),	// [attribute~=value] [attribute*=value]
	VALUE_STARTING_WITH 	("|="),	// [attribute|=value] [attribute^=value]
	VALUE_ENDS_WITH 		("$=");	// [attribute$=val]
	
	private String operatorString = "";
	private SelectorConditionType(String operator) {
		operatorString = operator; 
	}
	
	@Override
	public String toString() {
		return operatorString;
	}
	
}
