package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorCondition;
import ca.concordia.cssanalyser.dom.DOMHelper;
import ca.concordia.cssanalyser.dom.DOMHelper.BadXPathException;
import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;


/**
 * An atomic element selector, is a selector 
 * in the format element#id.class1.class2....:PseudoClass1...:PseudoClassK::PseudoElement
 * 
 * @author Davood Mazinanian
 *
 */
public class SimpleSelector extends BaseSelector {
	
	private String selectedElementName = "";
	private List<String> selectedClasses;
	private String selectedID = "";
	private List<SelectorCondition> conditions;
	private List<PseudoClass> pseudoClasses;
	private List<PseudoElement> pseudoElements;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSelector.class);

	public SimpleSelector() {
		this(null);
	}

	public SimpleSelector(GroupingSelector parent) {
		this(parent, -1, -1);
	}

	public SimpleSelector(int fileLineNumber, int fileColNumber) {
		this(null, fileColNumber, fileLineNumber);
	}

	/**
	 * 
	 * @param 	parent Parent GroupingSelector object. In a selector 
	 * 			like "p, div", the "p, div" is a parent GroupingSelector
	 * 			object and "p" and "div" would be the atomic element selectors
	 * @param fileLineNumber
	 * 			Line number of the source of container stylesheet.
	 * @param fileColumnNumber
	 * 			Column number of the source of container stylesheet.
	 * 
	 */
	public SimpleSelector(GroupingSelector parent, 
			int fileLineNumber,
			int fileColumnNumber) {
		super(parent, fileLineNumber, fileColumnNumber);
		conditions = new ArrayList<>();
		pseudoClasses = new ArrayList<>();
		selectedClasses = new ArrayList<>();
		pseudoElements = new ArrayList<>();
	}

	public void setSelectedElementName(String elementName) {
		selectedElementName = elementName;
	}

	public String getSelectedElementName() {
		return selectedElementName;
	}
	
	public void addClassName(String className) {
		selectedClasses.add(className);
	}
	
	public List<String> getClassNames() {
		return selectedClasses;
	}

	public void setElementID(String idName) {
		selectedID = idName;
	}
	
	/**
	 * Returns the ID of the current selector. For example,
	 * selector "#test" would have "test" as its ID name
	 * @return
	 */
	public String getElementID() {
		return selectedID;
	}

	/**
	 * Adds different conditions to current selector.
	 * @param condition
	 * @see ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorCondition
	 * @see ca.concordia.cssanalyser.cssmodel.selectors.conditions.SelectorConditionType
	 */
	public void addCondition(SelectorCondition condition) {
		conditions.add(condition);
	}
	
	public List<SelectorCondition> getConditions() {
		return conditions;
	}

	public void addPseudoClass(PseudoClass pseudoClass) {
		pseudoClasses.add(pseudoClass);
	}
	
	/**
	 * Returns all PseudoClasses of current selector
	 * @return
	 */
	public List<PseudoClass> getPseudoClasses() {
		return pseudoClasses;
	}

	/**
	 * Adds a PseudoElement to current selector
	 * @param pseudoElement
	 */
	public void addPseudoElement(PseudoElement pseudoElement) {
		pseudoElements.add(pseudoElement);
	}
	
	/**
	 * Returns the pseudo elements of current selector (like ::selector)
	 * @return
	 */
	public List<PseudoElement> getPseudoElements() {
		return pseudoElements;
	}
	
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		
		SimpleSelector otherSimpleSelector = (SimpleSelector)otherSelector;
		
		return selectedElementName.equalsIgnoreCase(otherSimpleSelector.selectedElementName) &&
				selectedID.equalsIgnoreCase(otherSimpleSelector.selectedID) &&
				selectedClasses.size() == otherSimpleSelector.selectedClasses.size() && 
					selectedClasses.containsAll(otherSimpleSelector.selectedClasses) &&
				conditions.size() == otherSimpleSelector.conditions.size() &&
					conditions.containsAll(otherSimpleSelector.conditions) &&
				pseudoClasses.equals(otherSimpleSelector.pseudoClasses) &&
				pseudoElements.equals(otherSimpleSelector.pseudoElements) &&
				mediaQueryLists.equals(otherSimpleSelector.mediaQueryLists);
	}
	
	/**
	 * Two atomic element selectors are equal
	 * if they are in the same line anc column in the file, 
	 */
	@Override
	public boolean equals(Object obj) {

		if (!generalEquals(obj))
		return false;
		
		BaseSelector otherBaseSelector = (BaseSelector) obj;

		if (lineNumber != otherBaseSelector.lineNumber ||
				columnNumber != otherBaseSelector.columnNumber)
			return false;
		
		if (getParentGroupingSelector() == null) {
			if (otherBaseSelector.getParentGroupingSelector() != null)
				return false;
		} else {
			if (!getParentGroupingSelector().equals(otherBaseSelector.getParentGroupingSelector()))
				return false;
		}
				
		return selectorEquals(otherBaseSelector);
	}

	private boolean generalEquals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != SimpleSelector.class)
			return false;
		if (mediaQueryLists != null) {
			BaseSelector otherAtomicElementSelector = (BaseSelector)obj;
			if (otherAtomicElementSelector.mediaQueryLists == null)
				return false;
			if (!mediaQueryLists.equals(otherAtomicElementSelector.mediaQueryLists))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + lineNumber;
		result = 31 * result + columnNumber;
		if (selectedID != null)
			result = 31 * result + selectedID.hashCode();
		if (selectedElementName != null)
			result = 31 * result + selectedElementName.hashCode();
		for (String c : selectedClasses)
			result += c.hashCode();
		for (SelectorCondition condition : conditions)
			result += (condition == null ? 0 : condition.hashCode());
		for (PseudoClass pseudoClass : pseudoClasses)
			result = 31 * result + (pseudoClass == null ? 0 : pseudoClass.hashCode());
		for (PseudoElement pElement : pseudoElements)
			result = 31 * result + (pElement == null ? 0 : pElement.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (selectedElementName != null) {
			if (!selectedElementName.equals("*") || (
					"".equals(selectedID) && 
					selectedClasses.size() == 0 &&
					conditions.size() == 0 &&
					pseudoClasses.size() == 0 &&
					pseudoElements.size() == 0)
					)
				result.append(selectedElementName);
		}
		if (selectedID != "")
			result.append("#" + selectedID);
		if (selectedClasses.size() > 0)
			for (String c : selectedClasses)
				result.append("." + c);
		for (SelectorCondition condition : conditions)
			result.append("[" + condition + "]");
		for (PseudoClass pseudoClass : pseudoClasses)
			result.append(":" + pseudoClass);
		for (PseudoElement pelement : pseudoElements)
			result.append("::" + pelement);
		return result.toString();
	}

	@Override
	public SimpleSelector clone() {
		SimpleSelector newOne = new SimpleSelector(lineNumber, columnNumber);
		if (mediaQueryLists != null)
			newOne.addMediaQueryLists(mediaQueryLists);
		newOne.selectedElementName = selectedElementName;
		newOne.selectedClasses = new ArrayList<>();
		for (String clazz : selectedClasses)
			newOne.addClassName(clazz);
		
		newOne.selectedID = selectedID;
		
		newOne.conditions = new ArrayList<>();
		for (SelectorCondition condition : this.conditions)
			newOne.addCondition(condition.clone());
		
		newOne.pseudoClasses = new ArrayList<>();
		for (PseudoClass pseudoClass : this.pseudoClasses)
			newOne.addPseudoClass(pseudoClass.clone());
		
		newOne.pseudoElements = new ArrayList<>();
		for (PseudoElement pseudoElement : this.pseudoElements)
			newOne.addPseudoElement(pseudoElement.clone());
		
		newOne.declarations = new LinkedHashSet<>();
		for (Declaration d : this.declarations)
			newOne.addDeclaration(d.clone());
		
		return newOne;
	}
	
	/**
	 * Returns the condition based on the given value which could be an+b, even, odd
	 * @param function
	 * @param value
	 * @return
	 * @throws UnsupportedSelectorToXPathException 
	 */
	protected String getPositionCondition(String function, String value) throws UnsupportedSelectorToXPathException {
		// Treat even and odd as the general an+b pattern
		if ("even".equals(value))
			value = "2n";
		else if ("odd".equals(value))
			value = "2n+1";
		
		/* 
		 * Based on http://www.w3.org/TR/css3-selectors/#nth-child-pseudo
		 */
		String patternString = "(([-+]?(?:\\d+)?)[nN])?+\\s*([-+]?\\s*\\d+)?";
		
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(value);
		
		//if (!Pattern.matches(patternString, value))
		//	return null; 
		if (!matcher.matches())
			throw new UnsupportedSelectorToXPathException(this); // To select nothing, if the pattern is not correct
		
		try {
		
			String aString = matcher.group(2);
			/*
			 * In cases like +n or -n, we have the aString would be + or -
			 * so we have to make them as +1 or -1
			 */
			if (aString == null)
				aString = "0";
			if (aString.equals(""))
				aString = "1";
			if ("+".equals(aString) || "-".equals(aString))
				aString += "1";
			int a = Integer.valueOf(aString);
			
			String bString = matcher.group(3);
			if (bString == null)
				bString = "0";
			int b = Integer.valueOf(bString);
			
			if (a != 0) {// "an" or "an+b"

				String s = "";
				if ("nth-child".equals(function) || "nth-of-type".equals(function)) {
					s = "((position() - (%s)) mod (%s) = 0) and (position() >= (%s))";
				} else { // nth-last-child
					s = "((position() + (%s)) mod -(%s) = 0) and (position() < (last() - (%s)))";
				}
				
				return String.format(s, b, a, b);
				
			} else {

				String s = "";
				if ("nth-child".equals(function) || "nth-of-type".equals(function)) {
					s = "position() - (%s) = 0";	
				} else { // nth-last-child
					s = "position() + (%s - 1) = last()";
				}
				
				return String.format(s, b);

			}
		
		} catch (Exception ex) {
			LOGGER.warn("Error in an+b pattern: " + value);
			throw new UnsupportedSelectorToXPathException(this);
		}
		
	}
	
	protected String getXPathConditionsString(List<String> xpathConditions) throws UnsupportedSelectorToXPathException {
		
		/* 
		 * We will postpone adding the element name to the XPath.
		 * The reason is, for some of the selectors, we need to add the
		 * element name to a place which is not right after beginning "//" 
		 */
		boolean elementAdded = false;

		String prefix = "";
		
		if (this.getElementID() != "") 
			xpathConditions.add(String.format("@id='%s'", this.getElementID()));

		if (this.getClassNames().size() > 0) {
			for (String className : this.getClassNames()) {
				xpathConditions.add(String.format("contains(concat(' ', normalize-space(@class), ' '), concat(' ', '%s', ' '))", className));
			}
		}

		if (this.getPseudoClasses().size() > 0) {
			// TODO: Move this code to the PseudoClass class?? 
			for (PseudoClass pseudoClass : this.getPseudoClasses()) {
				/* In case if the pseudo class is unsupported, we need to return an
				 * empty string so the analyzer would skip this selector 
				 */
				if (pseudoClass.isPseudoclassWithNoXpathEquivalence())
					//throw new UnsupportedSelectorToXPathException(this);
					continue;

				switch (pseudoClass.getName()) {
				case "lang":
					xpathConditions.add(String.format("@lang='%s'", pseudoClass.getValue()));
					break;
				case "first-child":
				case "last-child":
				case "nth-child":
				case "nth-last-child":
					// Select the element if it is the first, last, first nth or last nth child of its parent

					/* 
					 * In these cases the xpath condition would start with name() = 'elementName'
					 * (if elementName != *) and we will add more conditions to it. 
					 */
					String xpathCondition = "";
					if (!"*".equals(this.getSelectedElementName()))
							xpathCondition = String.format("(name() = '%s') and ", this.getSelectedElementName().toUpperCase());

					prefix = "*/*";

					String function = pseudoClass.getName();
					// Lets extract the value
					String value = pseudoClass.getValue();

					// Treat first and last as nth-first and nth-last
					if ("first-child".equals(function)) {
						// treat as nth-child(1)
						value = "1"; 
						function = "nth-child";
					}
					else if ("last-child".equals(function)) {
						// treat as nth-last-child(1)
						value = "1";
						function = "nth-last-child";
					}

					xpathCondition += "(" + getPositionCondition(function, value) + ")";

					elementAdded = true; // no need to write the element name to the xpath after "//"
					xpathConditions.add(xpathCondition);
					break;
				case "first-of-type":
				case "nth-of-type":
				case "last-of-type":
				case "nth-last-of-type":
					// Select the element if this element is the first or nth-first, last or nth-last sibling of its type
					prefix = "*/";

					function = pseudoClass.getName();
					// Lets extract the value
					value = pseudoClass.getValue();

					// Treat first and last as nth-first and nth-last
					if ("first-of-type".equals(function)) {
						// treat as nth-of-type(1)
						value = "1"; 
						function = "nth-of-type";
					}
					else if ("last-of-type".equals(function)) {
						// treat as nth-last-of-type(1)
						value = "1";
						function = "nth-last-of-type";
					}

					xpathCondition = getPositionCondition(function, value);					
					xpathConditions.add(xpathCondition);
					break;

				case "only-of-type":
					/*
					 * Select the element if the element is the only sibling of its type
					 */
					xpathConditions.add("(last() = 1)");
					break;
				case "only-child":
					/* 
					 * Select the element if it is the only child of its parent 
					 * xpath must be //* /*[(name = '%s') and (last() = 1)]
					 */
					prefix = "*/*";
					String s = "";
					if (!"*".equals(this.getSelectedElementName()))
						s = String.format("(name() = '%s') and ", this.getSelectedElementName().toUpperCase());
					
					xpathConditions.add(s + "(last() = 1)");
					elementAdded = true;
					break;
				case "empty":
					/* 
					 * Select the element if it has no children and text nodes 
					 * xpath must be element[not(*) and not(normalize-space())]
					 */
					xpathConditions.add("not(*) and not(normalize-space())");
					break;
				case "checked":
					/*
					 * Select only input and option elements which are selected 
					 */
					xpathConditions.add("(@selected or @checked) and (name() = 'INPUT' or name() = 'OPTION')");
					break;
				case "contains": 
					/*
					 * Seems to be deprecated, but we are supporting it
					 */
					xpathConditions.add(String.format("contains(., '%s')", pseudoClass.getValue()));
					break;
				case "not":
					NegationPseudoClass negPseudoClass = (NegationPseudoClass)pseudoClass;
					BaseSelector negSelector = negPseudoClass.getSelector();
					List<String> negativeConditions = new ArrayList<>();
					/*String pref =*/ negSelector.getXPathConditionsString(negativeConditions);
					String finalCondition = "not(%s)";
					String insideConditions = "";
					for (String insideCondition : negativeConditions) {
						insideConditions += insideCondition;
					}
					xpathConditions.add(String.format(finalCondition, insideConditions));
					break;
				}
			}
		} // End of pseudo classes!

		// Lets go for CSS conditions between brackets []
		if (this.getConditions().size() > 0) {

			for (SelectorCondition condition : this.getConditions()) {

				switch (condition.getConditionType()) {
				case HAS_ATTRIBUTE:
					xpathConditions.add("@" + condition.getAttributeName());
					break;
				case VALUE_CONTAINS:
					xpathConditions.add(String.format("contains(@%s, '%s')", condition.getAttributeName(), condition.getValue()));
					break;
				case VALUE_CONTAINS_WORD_SPACE_SEPARATED:
					xpathConditions.add(String.format("contains(concat(' ', normalize-space(@%s), ' '), ' %s ')", condition.getAttributeName(), condition.getValue()));
					break;
				case VALUE_ENDS_WITH:
					xpathConditions.add(String.format("substring(@%s, string-length(@%s) - %s) = '%s'", 
							condition.getAttributeName(), condition.getAttributeName(), condition.getValue().length() - 1, condition.getValue()));
					break;
				case VALUE_EQUALS_EXACTLY:
					xpathConditions.add(String.format("@%s='%s'", condition.getAttributeName(), condition.getValue()));
					break;
				case VALUE_STARTS_WITH:
					xpathConditions.add(String.format("starts-with(@%s, '%s')", condition.getAttributeName(), condition.getValue()));
					break;
				case VALUE_START_WITH_DASH_SEPARATED:
					xpathConditions.add(String.format("@%s = '%s' or starts-with(@%s, '%s-')",
							condition.getAttributeName(), condition.getValue(), condition.getAttributeName(), condition.getValue()));
					break;
				}
			}

		}

		// Currently, no support for pseudo elements like ::selector

		if (!elementAdded) {
			prefix = "*"; 
			if (!"*".equals(this.getSelectedElementName()))
				xpathConditions.add(String.format("name() = '%s'", this.getSelectedElementName().toUpperCase()));
		}
		
		return prefix;
	}

	@Override
	protected int[] getSpecificityElements() {
		int[] toReturn = new int[3];
		// count the number of ID selectors in the selector (= a)
		if (!this.selectedID .equals(""))
			toReturn[0] = 1;
		// count the number of class selectors, attributes selectors, and pseudo-classes in the selector (= b)
		// ignore negation pseudo class, while counting the inner selector as a 
		toReturn[1] = selectedClasses.size() + conditions.size();
		for (PseudoClass pseudoClass : pseudoClasses)
			if (pseudoClass instanceof NegationPseudoClass) {
				NegationPseudoClass negation = (NegationPseudoClass)pseudoClass;
				int[] negationSelectorSpecificity = negation.getSelector().getSpecificityElements();
				toReturn[0] += negationSelectorSpecificity[0];
				toReturn[1] += negationSelectorSpecificity[1];
				toReturn[2] += negationSelectorSpecificity[2];
			}
			else
				toReturn[1] += pseudoClasses.size();
		// count the number of type selectors and pseudo-elements in the selector (= c)
		// ignore the universal selector
		toReturn[2] += pseudoElements.size() + (selectedElementName.equals("*") ? 0 : 1);
		
		return toReturn;
	}

	@Override
	public DOMNodeWrapperList getSelectedNodes(Document document) {
		DOMNodeWrapperList toReturn = new DOMNodeWrapperList();
		try {
			NodeList nodes = DOMHelper.queryDocument(document, getXPath());
			Set<PseudoClass> unsupportedPseudoClasses = getUnsupportedPseudoClasses();
			for (int i = 0; i < nodes.getLength(); i++) {
				toReturn.add(new DOMNodeWrapper(nodes.item(i), unsupportedPseudoClasses));
			}
			
		} catch (BadXPathException | UnsupportedSelectorToXPathException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public Set<PseudoClass> getUnsupportedPseudoClasses() {
		Set<PseudoClass> unsupportedPseudoClasses = new HashSet<>();
		for (PseudoClass pseudoClass : getPseudoClasses()) {
			if (pseudoClass.isPseudoclassWithNoXpathEquivalence()) {
				unsupportedPseudoClasses.add(pseudoClass);
			}
		}
		return unsupportedPseudoClasses;
	}
}
