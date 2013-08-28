package dom;

import java.util.ArrayList;
import java.util.List;

import CSSModel.StyleSheet;

import org.w3c.dom.Document;


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
	 * Adds a stylesheet for the current dom state
	 * @param styleSheet
	 */
	public void addStyleSheet(StyleSheet styleSheet) {
		stylesheets.add(styleSheet);
	}
	
	/**
	 * Returns the current dom state
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
