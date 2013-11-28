package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.List;

/**
 * selector1 ~ selector2
 * @author Davood Mazinanian
 */
public class IndirectAdjacentSelector extends AtomicSelector {
	
	protected final AtomicSelector beforeMainSelector;
	protected final AtomicSelector mainSelector;
	
	public IndirectAdjacentSelector(AtomicSelector firstSelector, AtomicSelector secondSelector) {
		beforeMainSelector = firstSelector;
		mainSelector = secondSelector;
	}
	
	public AtomicSelector getFirstSelector() {
		return beforeMainSelector;
	}
	
	public AtomicSelector getSecondSelector() {
		return mainSelector;
	}
	
	@Override
	public String toString() {
		return beforeMainSelector + " ~ " + mainSelector;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!generalEquals(obj))
			return false;
		IndirectAdjacentSelector otherObj = (IndirectAdjacentSelector)obj;
		if (lineNumber == otherObj.lineNumber &&
				columnNumber == otherObj.columnNumber &&
				beforeMainSelector.equals(otherObj.beforeMainSelector) &&
				mainSelector.equals(otherObj.mainSelector))
			return true;
		return false;
	}
	
	private boolean generalEquals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof IndirectAdjacentSelector))
			return false;
		if (parentMedia != null) {
			IndirectAdjacentSelector otherIndirectAdjacentSelector = (IndirectAdjacentSelector)obj;
			if (otherIndirectAdjacentSelector.parentMedia == null)
				return false;
			if (!parentMedia.equals(otherIndirectAdjacentSelector.parentMedia))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		IndirectAdjacentSelector otherIndirectAdjacentSelector = (IndirectAdjacentSelector)otherSelector;
		return mainSelector.selectorEquals(otherIndirectAdjacentSelector.mainSelector) &&
				beforeMainSelector.selectorEquals(otherIndirectAdjacentSelector.beforeMainSelector);
				
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + lineNumber;
		result = 31 * result + columnNumber;
		result = 31 * result + (beforeMainSelector == null ? 0 : beforeMainSelector.hashCode());
		result = 31 * result + (mainSelector == null ? 0 : mainSelector.hashCode());
		return result;
	}
	
	@Override
	public Selector clone() {
		return new IndirectAdjacentSelector(beforeMainSelector, mainSelector);
	}

	@Override
	protected String getXPathConditionsString(List<String> xpathConditions) {

		// if selector combinator is "~" or "+"

		AtomicSelector left = this.getFirstSelector();
		AtomicSelector right = this.getSecondSelector();
		
		List<String> rightXPathConditions = new ArrayList<>();
		String rightXPathPrefix = right.getXPathConditionsString(xpathConditions);
		// If this is a "+" selector:
		if (this instanceof ImmediatelyAdjacentSelector) {
			// In this case we need one another condition, which is position() = 1
			xpathConditions.add("position() = 1");
		}
		String rightXPath = generateXpath(rightXPathPrefix, rightXPathConditions);
		
		List<String> leftXPathConditions = new ArrayList<>();
		String leftXPathPrefix = left.getXPathConditionsString(leftXPathConditions);
		
		String leftXPath = generateXpath(leftXPathPrefix, leftXPathConditions);
		
		return String.format("%s/following-sibling::%s", leftXPath, rightXPath);
	
	}

}
