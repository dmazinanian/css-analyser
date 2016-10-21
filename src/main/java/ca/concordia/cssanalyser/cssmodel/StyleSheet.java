package ca.concordia.cssanalyser.cssmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import org.w3c.dom.Document;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;


/**
 * This class is the main class storing all CSS data in the memory
 *
 * @author Davood Mazinanian
 */
public class StyleSheet extends CSSModelObject {

    Logger LOGGER = FileLogger.getLogger(StyleSheet.class);

	private Map<Selector, Integer> selectors;
	private String cssFilePath;
	private CSSValueOverridingDependencyList orderDependencies;



	public StyleSheet() {
		selectors = new LinkedHashMap<>();
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
		if (!selectors.containsKey(selector)) {
			selectors.put(selector, selectors.size() + 1);
		}
		selector.setParentStyleSheet(this);
	}

	/**
	 * Returns all the selectors, whether single or grouped in the style sheet.
	 *
	 * @return List<Selector>
	 */
	public Iterable<Selector> getAllSelectors() {
		return selectors.keySet();
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

		for (Selector selector : selectors.keySet()) { // Look inside all selectors
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
			for (Selector selector : selectors.keySet())
				for (Declaration declaration : selector.getDeclarations())
					listOfDeclarations.add(declaration);
		//}
		return listOfDeclarations;
	}

	@Override
	public String toString() {

		StringBuilder toReturn = new StringBuilder();
		Set<MediaQueryList> lastMediaQueryLists = null;
		Iterator<Selector> selectorIterator = selectors.keySet().iterator();
		int currentIndentation = 0;
		if (selectorIterator.hasNext()) {
			Selector s = selectorIterator.next();
			while(true)  {
				if (lastMediaQueryLists == null || (lastMediaQueryLists != null && !lastMediaQueryLists.equals(s.getMediaQueryLists()))) {
					for (Iterator<MediaQueryList> mediaQueryList = s.getMediaQueryLists().iterator(); mediaQueryList.hasNext();) {
						MediaQueryList mql = mediaQueryList.next();
						if (lastMediaQueryLists == null || (lastMediaQueryLists != null && !lastMediaQueryLists.contains(mql))) {
							toReturn.append(getIndentsString(currentIndentation));
							toReturn.append(mql + " {" + System.lineSeparator() + System.lineSeparator()); // Open media query
							currentIndentation++;
						}
					}
					lastMediaQueryLists = s.getMediaQueryLists();
				}

				toReturn.append(getIndentsString(currentIndentation) + s + " {" + System.lineSeparator());
				for (Declaration d : s.getDeclarations()) {
					if (d instanceof ShorthandDeclaration) {
						if (((ShorthandDeclaration)d).isVirtual())
							continue;
					}
					toReturn.append(getIndentsString(currentIndentation + 1) + d);
					if (d.isImportant())
						toReturn.append(" !important");
					toReturn.append(";" + System.lineSeparator());
				}
				toReturn.append(getIndentsString(currentIndentation) + "}" + System.lineSeparator() + System.lineSeparator());

				if (selectorIterator.hasNext()) {
					s = selectorIterator.next();
					if (lastMediaQueryLists != null) {
						if (!lastMediaQueryLists.equals(s.getMediaQueryLists())) {
							// For each MediaQueryList which is not in the new selector, close the MediaQuery
							for (MediaQueryList mq : lastMediaQueryLists) {
								if (!s.getMediaQueryLists().contains(mq)) {
									currentIndentation--;
									toReturn.append(getIndentsString(currentIndentation) + "}" + System.lineSeparator() + System.lineSeparator()); // close media query
								}
							}
						}
					}
				} else {
					break;
				}
			}

			while (currentIndentation > 0) { // unclosed media queries
				currentIndentation--;
				toReturn.append(getIndentsString(currentIndentation) + "}" + System.lineSeparator()); // close media query

			}

		}

		return toReturn.toString();
	}

	private String getIndentsString(int currentIndentation) {
		String s = "";
		for (int i = 0; i < currentIndentation; i++)
			s += "\t";
		return s;
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
		for (Selector s : this.selectors.keySet())
			styleSheet.addSelector(s.clone());
		return styleSheet;
	}

	/**
	 * Maps every base selector to a node list in the stylesheet
	 * @param document
	 * @param styleSheet
	 * @return
	 */
	public Map<BaseSelector, DOMNodeWrapperList> mapStylesheetOnDocument(Document document) {
		List<BaseSelector> allSelectors = getAllBaseSelectors();
		Map<BaseSelector, DOMNodeWrapperList> selectorNodeListMap = new LinkedHashMap<>();
		for (BaseSelector selector : allSelectors) {
			selectorNodeListMap.put(selector, selector.getSelectedNodes(document));
		}
		return selectorNodeListMap;
	}

	/**
	 * Returns a list of documents' node in addition to the CSS selectors which
	 * select each node
	 * @param document
	 * @param styleSheet
	 * @return
	 */
	public Map<DOMNodeWrapper, List<BaseSelector>> getCSSClassesForDOMNodes(Document document) {

		// Map every node in the DOM tree to a list of selectors in the stylesheet
		Map<DOMNodeWrapper, List<BaseSelector>> nodeToSelectorsMapping = new HashMap<>();
		for (BaseSelector selector : getAllBaseSelectors()) {
			DOMNodeWrapperList matchedNodes = selector.getSelectedNodes(document);
			for (DOMNodeWrapper domNodeWrapper : matchedNodes) {
				List<BaseSelector> correspondingSelectors = nodeToSelectorsMapping.get(domNodeWrapper);
				if (correspondingSelectors == null) {
					correspondingSelectors = new ArrayList<>();
				}
				correspondingSelectors.add(selector);
				nodeToSelectorsMapping.put(domNodeWrapper, correspondingSelectors);
			}
		}
		return nodeToSelectorsMapping;
	}

	public void addMediaQueryList(MediaQueryList forMedia) {
		for (Selector s : selectors.keySet())
			s.addMediaQueryList(forMedia);
	}

	public CSSValueOverridingDependencyList getLastComputetOrderDependencies() {
		return orderDependencies;
	}

	public CSSValueOverridingDependencyList getValueOverridingDependencies(Document dom, boolean domFreeDeps) {

		CSSDependencyDetector dependencyDetector;

		if (dom != null) {
			dependencyDetector = new CSSDependencyDetector(this, dom);
		} else {
			dependencyDetector = new CSSDependencyDetector(this);
		}

		orderDependencies
            = dependencyDetector.findOverridingDependancies(domFreeDeps);

		return orderDependencies;

	}

	public CSSValueOverridingDependencyList getValueOverridingDependencies(boolean domFreeDeps) {

		return getValueOverridingDependencies(null, domFreeDeps);

	}

	public int getNumberOfSelectors() {
		return selectors.size();
	}

	public void removeSelectors(List<Selector> selectorsToBeRemoved) {
		for (Selector s : selectorsToBeRemoved)
			selectors.remove(s);
		// Update the numbers associated with every declaration
		int i = 1;
		for (Selector s : selectors.keySet()) {
			selectors.put(s, i++);
        }
	}

	public int getSelectorNumber(Selector selector) {
		return selectors.get(selector);
	}

	public boolean containsSelector(Selector selector) {
		return selectors.containsKey(selector);
	}

	public StyleSheet getStyleSheetWithIntraSelectorDependenciesRemoved() {
		StyleSheet styleSheetToReturn = new StyleSheet();
		styleSheetToReturn.cssFilePath = this.cssFilePath;
		for (Selector selector : getAllSelectors()) {
			Selector newSelector = selector.copyEmptySelector();
			newSelector.setOriginalSelector(selector);
			for (Declaration declaration : selector.getDeclarationsWithIntraSelectorDependenciesRemoved()) {
				newSelector.addDeclaration(declaration);
				styleSheetToReturn.addSelector(newSelector);
			}
		}
		return styleSheetToReturn;
	}

}
