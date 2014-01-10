package ca.concordia.cssanalyser.cssmodel;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.SingleSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupedSelectors;
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
		listOfSelectors = new HashSet<>();
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
	 * @return List<SingleSelector>
	 */
	public Set<SingleSelector> getAllSingleSelectors() {
		Set<SingleSelector> allSingleSelectors = new HashSet<>();

		for (Selector selector : listOfSelectors) { // Look inside all selectors
			if (selector instanceof SingleSelector) {
				// Just add
				allSingleSelectors.add((SingleSelector) selector);
			} else if (selector instanceof GroupedSelectors) {
				// Loop through all grouped selectors
				for (SingleSelector singleSelector : (GroupedSelectors) selector) {
					allSingleSelectors.add(singleSelector);
				}
			}
		}
		return allSingleSelectors;
	}

	/**
	 * This method returns all the declarations inside a style sheet, preserving
	 * their order in which they have been defined.
	 * 
	 * @return List<Declaration>
	 */
	public Set<Declaration> getAllDeclarations() {
		//if (listOfDeclarations == null) {
			Set<Declaration> listOfDeclarations = new HashSet<>();
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
		styleSheet.listOfSelectors = new HashSet<>(this.listOfSelectors);
		return styleSheet;
	}
}
