package ca.concordia.cssanalyser.parser;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

/**
 * @author Davood Mazinanian
 *
 */
public interface CSSParser {
	
	public StyleSheet parseCSSString(String css);
	
	public StyleSheet parseExternalCSS(String path) throws ParseException;
	
}
