package CSSModel.selectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents CSS "selector1 selector2" selectors
 * selector1 and selector2 are of type {@link AtomicSelector}
 * so for example, for selector1 selector2 selector 3, we have two selectors
 * one of which is again a {@link DescendantSelector} and anoter is an {@link DescendantSelector}
 * @author Davood Mazinanian
 */
public class DescendantSelector extends AtomicSelector {
	
	protected final AtomicSelector parentSelector;
	protected final AtomicSelector childSelector; 
	
	public DescendantSelector(AtomicSelector parent, AtomicSelector child) {
		parentSelector = parent;
		childSelector = child;
	}
	
	/**
	 * Returns the parent selector (the selector on the left hand side
	 * of a descendant selector)
	 * @return
	 */
	public AtomicSelector getParentSelector() {
		return parentSelector;
	}
	
	/**
	 * Returns the child selector (the selector on the right hand side
	 * of a descendant selector)
	 * @return
	 */
	public AtomicSelector getChildSelector() {
		return childSelector;
	}
	
	@Override
	public String toString() {
		return parentSelector + " " + childSelector;
	}
	
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		DescendantSelector otherDesendantSelector = (DescendantSelector)otherSelector;
		return parentSelector.selectorEquals(otherDesendantSelector.parentSelector) &&
				childSelector.selectorEquals(otherDesendantSelector.childSelector);
	}
	
	@Override
	public boolean equals(Object obj) {
		generalEquals(obj);
		DescendantSelector otherDesendantSelector = (DescendantSelector)obj;
		return (lineNumber == otherDesendantSelector.lineNumber &&
				columnNumber == otherDesendantSelector.columnNumber &&
				selectorEquals(otherDesendantSelector));
	}

	private boolean generalEquals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof DescendantSelector))
			return false;
		if (parentMedia != null) {
			DescendantSelector otherDescendantSelector = (DescendantSelector)obj;
			if (otherDescendantSelector.parentMedia == null)
				return false;
			if (!parentMedia.equals(otherDescendantSelector.parentMedia))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + lineNumber;
		result = 31 * result + columnNumber;
		result = 31 * result + (parentSelector == null ? 0 : parentSelector.hashCode());
		result = 31 * result + (childSelector == null ? 0 : childSelector.hashCode());
		return result;
	}
	
	@Override
	public Selector clone() {
		return new DescendantSelector(this.parentSelector, this.childSelector);
	}

	@Override
	protected String getXPathConditionsString(List<String> xpathConditions) {
		 
		// if selector combinator is " " or ">"
		AtomicSelector parent = this.getParentSelector();
		AtomicSelector child = this.getChildSelector();
		String modifier = "descendant::"; // if selector is "s1 > s2"
		if (this instanceof DirectDescendantSelector) // if selector is "s1 s2"
			modifier = "";
		List<String> parentConditions = new ArrayList<>();
		String parentXPath = generateXpath(parent.getXPathConditionsString(parentConditions), parentConditions);
		List<String> childConditions = new ArrayList<>();
		String childXPath = generateXpath(child.getXPathConditionsString(childConditions), childConditions);
		
		return String.format("%s/%s%s", parentXPath, modifier, childXPath);
		
	
	}
	
}
