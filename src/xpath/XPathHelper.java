package xpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CSSModel.conditions.SelectorCondition;
import CSSModel.selectors.AtomicElementSelector;
import CSSModel.selectors.AtomicSelector;
import CSSModel.selectors.DescendantSelector;
import CSSModel.selectors.DirectDescendantSelector;
import CSSModel.selectors.ImmediatelyAdjacentSelector;
import CSSModel.selectors.IndirectAdjacentSelector;
import CSSModel.selectors.PseudoClass;
import CSSModel.selectors.PseudoNegativeClass;

/**
 * This class provides functionalities for working with
 * XPath strings.
 * Some of the rule has been adapted from http://css2xpath.appspot.com/
 * 
 * @author Davood Mazinanian
 *
 */
public final class XPathHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XPathHelper.class);
	
	/**
	 * Convert our <code>CSSModel.AtomicSelector</code> objects to <code>XPath</code> strings
	 * @param selector Different <code>CSSModel.AtomicSelector</code> objects
			(AtomicElementSelector, DescendantSelector, etc)
	 * @return String of XPath for current <code>AtomicSelector</code>
	 */
	public static String AtomicSelectorToXPath(AtomicSelector selector) {
			
		String xpath = "";
		
		// List of the conditions coming between []
		List<String> xpathConditions = new ArrayList<>();
		String prefix = getConditions(selector, xpathConditions);			
		if (prefix == null) // There was a problem with CSS selector
			return null; // to select nothing
		
		xpath = generateXpath(prefix, xpathConditions);
		
		xpath = "//" + xpath;
		
		if (selector.getClass() == AtomicElementSelector.class) {
			
			//AtomicElementSelector elementSelector = (AtomicElementSelector)selector;
			
			
		
			
		} else if (selector instanceof DescendantSelector) {
			
			
			
		} else if (selector instanceof IndirectAdjacentSelector) {
		
			
			
		}
		
		return xpath;
		
	}

	/**
	 * Gets an AtomicSelector and returns a prefix of the xpath. The conditions for this
	 * xpath which are going inside [] would be added to xpathConditions
	 * @param elementSelector
	 * @param xpathConditions
	 * @return
	 */
	private static String getConditions(AtomicSelector selector, List<String> xpathConditions) {
		
		String prefix = "";
		
		if (selector.getClass() == AtomicElementSelector.class) {
			
			AtomicElementSelector elementSelector = (AtomicElementSelector)selector;
				
			/* 
			 * We will postpone adding the element name to the XPath.
			 * The reason is, for some of the selectors, we need to add the
			 * element name to a place which is not right after beginning "//" 
			 */
			boolean elementAdded = false;

			if (elementSelector.getElementID() != "") 
				xpathConditions.add(String.format("@id='%s'", elementSelector.getElementID()));

			if (elementSelector.getClassNames().size() > 0) {
				for (String className : elementSelector.getClassNames()) {
					xpathConditions.add(String.format("contains(concat(' ', normalize-space(@class), ' '), concat(' ', '%s', ' '))", className));
				}
			}


			/*
			 * There is no XPath equivalence for these pseudo classes:
			 * (make sure to refer to CSSModel.selectors.PseudoElement for 
			 * a clear explanation about PseudoElements and PseudoClasses) 
			 */

			String[] unsupportedPseudoClasses = new String[] {
					"link", "active", "hover", "visited", "focus", 
					"first-letter", "first-line", "before", "after", "target",
					"root", "enabled", "disabled"
			};

			if (elementSelector.getPseudoClasses().size() > 0) {
				for (PseudoClass pseudoClass : elementSelector.getPseudoClasses()) {
					/* In case if the pseudo class is unsupported, we need to return an
					 * empty string so the analyzer would skip this selector 
					 */
					if (Arrays.asList(unsupportedPseudoClasses).indexOf(pseudoClass.getName()) >= 0)
						return null; // To select nothing

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
						if (!"*".equals(elementSelector.getSelectedElementName()))
								xpathCondition = String.format("(name() = '%s') and ", elementSelector.getSelectedElementName().toUpperCase());

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
						if (!"*".equals(elementSelector.getSelectedElementName()))
							s = String.format("(name() = '%s') and ", elementSelector.getSelectedElementName().toUpperCase());
						
						xpathConditions.add(s + "(last() = 1)");
						elementAdded = true;
						break;
					case "empty":
						/* 
						 * Select the element if it has no children and text nodes 
						 * xpath must be element[not(*) and not(normalize-space())]
						 */
						xpathConditions.add(String.format("not(*) and not(normalize-space())", elementSelector.getSelectedElementName().toUpperCase()));
						break;
					case "checked":
						/*
						 * Select only input and option elements which are selected 
						 */
						xpathConditions.add("(@selected or @checked) and (name() = 'INPUT' or name() = 'OPTION')");
						break;
					case "not":
						PseudoNegativeClass negPseudoClass = (PseudoNegativeClass)pseudoClass;
						AtomicElementSelector negSelector = negPseudoClass.getSelector();
						List<String> negativeConditions = new ArrayList<>();
						/*String pref =*/ getConditions(negSelector, negativeConditions);
						String finalCondition = "not(%s)";
						String insideConditions = "";
						for (String insideCondition : negativeConditions) {
							insideConditions += insideCondition;
						}
						xpathConditions.add(String.format(finalCondition, insideConditions));
						// TODO: test it!
						break;
					}
				}
			} // End of pseudo classes!

			// Lets go for CSS conditions between brackets []
			if (elementSelector.getConditions().size() > 0) {

				for (SelectorCondition condition : elementSelector.getConditions()) {

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
				if (!"*".equals(elementSelector.getSelectedElementName()))
					xpathConditions.add(String.format("name() = '%s'", elementSelector.getSelectedElementName().toUpperCase()));
			}
			
			return prefix;
			
		} else if (selector instanceof DescendantSelector) { 
			// if selector combinator is " " or ">"
			DescendantSelector descendantSelector = (DescendantSelector)selector;
			AtomicSelector parent = descendantSelector.getParentSelector();
			AtomicSelector child = descendantSelector.getChildSelector();
			String modifier = "descendant::"; // if selector is "s1 > s2"
			if (selector instanceof DirectDescendantSelector) // if selector is "s1 s2"
				modifier = "";
			List<String> parentConditions = new ArrayList<>();
			String parentXPath = generateXpath(getConditions(parent, parentConditions), parentConditions);
			List<String> childConditions = new ArrayList<>();
			String childXPath = generateXpath(getConditions(child, childConditions), childConditions);
			
			return String.format("%s/%s%s", parentXPath, modifier, childXPath);
			
		} else if (selector instanceof IndirectAdjacentSelector) {
			// if selector combinator is "~" or "+"
			IndirectAdjacentSelector immediatelyAdjacentSelector = (IndirectAdjacentSelector)selector;
			AtomicSelector left = immediatelyAdjacentSelector.getFirstSelector();
			AtomicSelector right = immediatelyAdjacentSelector.getSecondSelector();
			
			List<String> rightXPathConditions = new ArrayList<>();
			String rightXPathPrefix = getConditions(right, xpathConditions);
			// If this is a "+" selector:
			if (selector instanceof ImmediatelyAdjacentSelector) {
				// In this case we need one another condition, which is position() = 1
				xpathConditions.add("position() = 1");
			}
			String rightXPath = generateXpath(rightXPathPrefix, rightXPathConditions);
			
			List<String> leftXPathConditions = new ArrayList<>();
			String leftXPathPrefix = getConditions(left, leftXPathConditions);
			
			String leftXPath = generateXpath(leftXPathPrefix, leftXPathConditions);
			
			return String.format("%s/following-sibling::%s", leftXPath, rightXPath);
		}
		return prefix;
		
	}
	
	/**
	 * Gets a prefix and a list of conditions and creates a complete xpath
	 * in this form: prefix[condition1 and condition2 and ... and condition n]
	 * @param prefix
	 * @param xpathConditions
	 * @return
	 */
	private static String generateXpath(String prefix, List<String> xpathConditions) {
		
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

	/**
	 * Returns the condition based on the given value which could be an+b, even, odd
	 * @param function
	 * @param value
	 * @return
	 */
	private static String getPositionCondition(String function, String value) {
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
			return null; // To select nothing, if the pattern is not correct
		
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
			return null;
		}
		
	}
	
}
