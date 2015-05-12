package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSDependencyDetector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

/**
 * Represents the selectors which are not grouped
 * (could be combined)
 * 
 * @author Davood Mazinanian
 * 
 */
public abstract class BaseSelector extends Selector {

	private GroupingSelector parentGroupingSelector;

	public BaseSelector() {
		this(null, new LocationInfo());
	}
	
	public BaseSelector(LocationInfo locationInfo) {
		this(null, locationInfo);
	}

	public BaseSelector(GroupingSelector parent) {
		this(parent, new LocationInfo());
	}

	public BaseSelector(GroupingSelector parent, LocationInfo locationInfo) {
		super(locationInfo);
		parentGroupingSelector = parent;
	}

	public void setParentGroupSelector(GroupingSelector newGroup) {
		parentGroupingSelector = newGroup;
	}

	public GroupingSelector getParentGroupingSelector() {
		return parentGroupingSelector;
	}
	
	/**
	 * Returns the specificity value for this base selector. <br />
	 * <br />
	 * From http://www.w3.org/TR/selectors/#specificity: <br />
	 * <br />
	 * A selector's specificity is calculated as follows: <br />
	 * - count the number of ID selectors in the selector (= a) <br />
	 * - count the number of class selectors, attributes selectors, and pseudo-classes in the selector (= b) <br />
	 * - count the number of type selectors and pseudo-elements in the selector (= c) <br />
	 * - ignore the universal selector <br />
	 * Selectors inside the negation pseudo-class are counted like any other, but the negation itself does not count as a pseudo-class. <br />
	 * Concatenating the three numbers a-b-c (in a number system with a large base) gives the specificity. <br />
	 * <br />
	 * Note: Repeated occurrances of the same simple selector are allowed and do increase specificity. <br />
	 * Note: the specificity of the styles specified in an HTML style attribute is described in CSS 2.1.. <br />
	 * 
	 * @return
	 */
	public int getSpecificity() {
		int[] specificityElements = getSpecificityElements();
		String a = String.valueOf(specificityElements[0]);
		String b = String.valueOf(specificityElements[1]);
		String c = String.valueOf(specificityElements[2]);
		
		return Integer.valueOf(a + b + c);
	}
	
	/**
	 * Returns the three parts of the specificity for this base selector,
	 * where a,b and c are stored in the first to third cells of the returned array respectively
	 * The calculation of the specificity value is done using {@link #getSpecificity()}
	 * @return
	 */
	protected abstract int[] getSpecificityElements();
	
	protected abstract String getXPathConditionsString(List<String> xpathConditions) throws UnsupportedSelectorToXPathException;

	/**
	 * Convert our <code>ca.concordia.cssanalyser.cssmodel.AtomicSelector</code> objects to <code>XPath</code> strings
	 * @param selector Different <code>ca.concordia.cssanalyser.cssmodel.AtomicSelector</code> objects
			(SimpleSelector, DescendantSelector, etc)
	 * @return String of XPath for current <code>BaseSelector</code>
	 * @throws UnsupportedSelectorToXPathException 
	 */
	@Override
	public String getXPath() throws UnsupportedSelectorToXPathException {
		
		String xpath = "";
		
		// List of the conditions coming between []
		List<String> xpathConditions = new ArrayList<>();
		// Using template design pattern
		String prefix = getXPathConditionsString(xpathConditions);			
		
		xpath = generateXpath(prefix, xpathConditions);
		
		xpath = "//" + xpath;
		
		return xpath;
		
	}

	/**
	 * Gets a prefix and a list of conditions and creates a complete xpath
	 * in this form: prefix[condition1 and condition2 and ... and condition n]
	 * @param prefix
	 * @param xpathConditions
	 * @return
	 */
	protected String generateXpath(String prefix, List<String> xpathConditions) {
		
		StringBuilder xpath = new StringBuilder(prefix);
		
		// Add the conditions inside brackets and put "and" between conditions
		if (xpathConditions.size() > 0) {
		
			xpath.append("[");
		
			for (int i = 0; i < xpathConditions.size(); i++) {
				xpath.append("(" + xpathConditions.get(i) + ")");
				if (i != xpathConditions.size() - 1)
					xpath.append(" and ");
			}
			
			xpath.append("]");
		}
		
		return xpath.toString();
	}
	
	public abstract BaseSelector clone();
	
	public abstract DOMNodeWrapperList getSelectedNodes(Document document);
	
	@Override
	public CSSValueOverridingDependencyList getIntraSelectorOverridingDependencies() {
		return CSSDependencyDetector.getValueOverridingDependenciesForSelector(this);
	}

}
