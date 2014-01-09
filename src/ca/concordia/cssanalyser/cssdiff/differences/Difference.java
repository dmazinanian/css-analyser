package ca.concordia.cssanalyser.cssdiff.differences;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;

/**
 * Represents a difference between first and second given stylesheets .
 * @author Davood Mazinanian
 * 
 */

public class Difference {

	private StyleSheet styleSheet1;
	private StyleSheet styleSheet2;

	public Difference(StyleSheet styleSheet1, StyleSheet styleSheet2) {
		this.styleSheet1 = styleSheet1;
		this.styleSheet2 = styleSheet2;
	}

	public StyleSheet getStyleSheet1() {
		return styleSheet1;
	}
	
	public StyleSheet getStyleSheet2() {
		return styleSheet2;
	}

}
