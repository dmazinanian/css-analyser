package ca.concordia.cssanalyser.cssmodel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * This class is the main class storing all CSS data in the memory
 * 
 * @author Davood Mazinanian
 */
public class StyleSheet {

	private Set<Selector> listOfSelectors;
	//private Set<Declaration> listOfDeclarations;
	private String cssFilePath;

	public StyleSheet() {
		listOfSelectors = new LinkedHashSet<>();
	}
	
	public void setPath(String path) {
		cssFilePath = path;
	}

	/**
	 * Adds a new selector (whether single or grouped) to the selectors list of
	 * this style sheet.
	 * 
	 * @param selector
	 */
	public void addSelector(Selector selector) {
		listOfSelectors.add(selector);
	}
	
	/**
	 * Returns all the selectors, whether single or grouped in the style sheet.
	 * 
	 * @return List<Selector>
	 */
	public Set<Selector> getAllSelectors() {
		return listOfSelectors;
	}

	/**
	 * This method returns all the single selectors in the style sheet, in
	 * addition to the all single selectors inside the grouped selectors. It
	 * preserves the order of single selectors.
	 * 
	 * @return List<BaseSelector>
	 */
	public List<BaseSelector> getAllBaseSelectors() {
		List<BaseSelector> allBaseSelectors = new ArrayList<>();

		for (Selector selector : listOfSelectors) { // Look inside all selectors
			if (selector instanceof BaseSelector) {
				allBaseSelectors.add((BaseSelector) selector);
			} else if (selector instanceof GroupingSelector) {
				for (BaseSelector bs : ((GroupingSelector)selector).getBaseSelectors()) {
					allBaseSelectors.add(bs);
				}
				
			}
		}
		return allBaseSelectors;
	}

	/**
	 * This method returns all the declarations inside a style sheet, preserving
	 * their order in which they have been defined.
	 * 
	 * @return List<Declaration>
	 */
	public Set<Declaration> getAllDeclarations() {
		//if (listOfDeclarations == null) {
			Set<Declaration> listOfDeclarations = new LinkedHashSet<>();
			for (Selector selector : listOfSelectors)
				for (Declaration declaration : selector.getDeclarations())
					listOfDeclarations.add(declaration);
		//}
		return listOfDeclarations;
	}

	@Override
	public String toString() {

		StringBuilder toReturn = new StringBuilder();
		for (Selector s : listOfSelectors) {
			toReturn.append(s + " { \n");
			for (Declaration d : s.getDeclarations())
				toReturn.append("    " + d + "; \n");
			toReturn.append("} \n\n");
		}

		return toReturn.toString();
	}

	public void addSelectors(StyleSheet s) {
		for (Selector selector : s.getAllSelectors())
			addSelector(selector);
	}
	
	public String getFilePath() {
		return cssFilePath;
	}
	
	@Override
	public StyleSheet clone() {
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.cssFilePath = cssFilePath;
		for (Selector s : this.listOfSelectors)
			styleSheet.addSelector(s.clone());
		return styleSheet;
	}
}
