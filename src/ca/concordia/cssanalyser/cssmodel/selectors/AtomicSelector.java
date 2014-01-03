package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the selectors which are not grouped
 * (not combined using any combinator)
 * 
 * @author Davood Mazinanian
 * 
 */
public abstract class AtomicSelector extends Selector {

	private GroupedSelectors parentGroupSelector;

	public AtomicSelector() {
		this(null, -1, -1);
	}

	public AtomicSelector(GroupedSelectors parent) {
		this(parent, -1, -1);
	}

	public AtomicSelector(int line, int coloumn) {
		this(null, line, coloumn);
	}

	public AtomicSelector(GroupedSelectors parent, int line, int coloumn) {
		super(line, coloumn);
		parentGroupSelector = parent;
	}

	public void setParentGroupSelector(GroupedSelectors newGroup) {
		parentGroupSelector = newGroup;
	}

	public GroupedSelectors getParentGroupSelector() {
		return parentGroupSelector;
	}
	
	protected abstract String getXPathConditionsString(List<String> xpathConditions) throws UnsupportedSelectorToXPathException;

	/**
	 * Convert our <code>ca.concordia.cssanalyser.cssmodel.AtomicSelector</code> objects to <code>XPath</code> strings
	 * @param selector Different <code>ca.concordia.cssanalyser.cssmodel.AtomicSelector</code> objects
			(AtomicElementSelector, DescendantSelector, etc)
	 * @return String of XPath for current <code>AtomicSelector</code>
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
	
	/*
	 * http://www.w3.org/TR/CSS21/cascade.html#specificity
	 * 
	 * 6.4.3 Calculating a selector's specificity
	 * 
	 * A selector's specificity is calculated as follows:
	 * 
	 * 1)	Count 1 if the declaration is from is a 'style' attribute rather than
	 * 		a rule with a selector, 0 otherwise (= a) (In HTML, values of an
	 * 		element's "style" attribute are style sheet rules. These rules have
	 * 		no selectors, so a=1, b=0, c=0, and d=0.)
	 * 
	 * 2) 	Count the number of ID attributes in the selector (= b)
	 * 3) 	Count the number of other attributes and pseudo-classes in the selector (= c)
	 * 4) 	Count the number of element names and pseudo-elements in the selector (= d)
	 *	
	 * The specificity is based only on the form of the selector. In particular, a selector of
	 * the form "[id=p33]" is counted as an attribute selector (a=0, b=0, c=1, d=0), even if 
	 * the id attribute is defined as an "ID" in the source document's DTD.
	 * 
	 * Concatenating the four numbers a-b-c-d (in a number system with a large base) gives the specificity.
	 */

	//public abstract int getSpecificity(); 

}
