package ca.concordia.cssanalyser.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;


/**
 * A model contains one DOM state and all its corresponding external stylesheets.
 * One may extract inline stylesheets from DOM 
 * @author Davood Mazinanian
 *
 */
public class Model {
	
	private final List<StyleSheet> stylesheets;
	private final Document dom;
	
	public Model(Document document) {
		stylesheets = new ArrayList<>();
		dom = document;
	}
	
	/**
	 * Adds a stylesheet for the current ca.concordia.cssanalyser.dom state
	 * @param styleSheet
	 */
	public void addStyleSheet(StyleSheet styleSheet) {
		stylesheets.add(styleSheet);
	}
	
	/**
	 * Returns the current ca.concordia.cssanalyser.dom state
	 * @return
	 */
	public Document getDocument() {
		return dom;
	}
	
	/**
	 * 
	 */
	public List<StyleSheet> getStyleSheets() {
		return stylesheets;
	}

}
