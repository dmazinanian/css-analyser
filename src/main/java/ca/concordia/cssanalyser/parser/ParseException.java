package ca.concordia.cssanalyser.parser;

public class ParseException extends Exception {

	public ParseException(Exception e) {
		super(e);
	}
	
	public ParseException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
