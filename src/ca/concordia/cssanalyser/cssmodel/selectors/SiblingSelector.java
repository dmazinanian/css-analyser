package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.ArrayList;
import java.util.List;

/**
 * selector1 ~ selector2
 * @author Davood Mazinanian
 */
public class SiblingSelector extends Combinator {
	
	protected final BaseSelector beforeMainSelector;
	protected final SimpleSelector mainSelector;
	
	public SiblingSelector(BaseSelector firstSelector, SimpleSelector secondSelector) {
		beforeMainSelector = firstSelector;
		mainSelector = secondSelector;
	}
	
	public BaseSelector getFirstSelector() {
		return beforeMainSelector;
	}
	
	public SimpleSelector getSecondSelector() {
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
		SiblingSelector otherObj = (SiblingSelector)obj;
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
		if (!(obj instanceof SiblingSelector))
			return false;
		if (mediaQueryLists != null) {
			SiblingSelector otherIndirectAdjacentSelector = (SiblingSelector)obj;
			if (otherIndirectAdjacentSelector.mediaQueryLists == null)
				return false;
			if (!mediaQueryLists.equals(otherIndirectAdjacentSelector.mediaQueryLists))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!generalEquals(otherSelector))
			return false;
		SiblingSelector otherIndirectAdjacentSelector = (SiblingSelector)otherSelector;
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
	public SiblingSelector clone() {
		SiblingSelector newOne = new SiblingSelector(beforeMainSelector.clone(), mainSelector.clone());
		newOne.setLineNumber(lineNumber);
		newOne.setColumnNumber(columnNumber);
		newOne.addMediaQueryLists(mediaQueryLists);
		return newOne;
	}

	@Override
	protected String getXPathConditionsString(List<String> xpathConditions) throws UnsupportedSelectorToXPathException {

		// if selector combinator is "~" or "+"

		BaseSelector left = this.getFirstSelector();
		BaseSelector right = this.getSecondSelector();
		
		List<String> rightXPathConditions = new ArrayList<>();
		String rightXPathPrefix = right.getXPathConditionsString(xpathConditions);
		// If this is a "+" selector:
		if (this instanceof AdjacentSiblingSelector) {
			// In this case we need one another condition, which is position() = 1
			xpathConditions.add("position() = 1");
		}
		String rightXPath = generateXpath(rightXPathPrefix, rightXPathConditions);
		
		List<String> leftXPathConditions = new ArrayList<>();
		String leftXPathPrefix = left.getXPathConditionsString(leftXPathConditions);
		
		String leftXPath = generateXpath(leftXPathPrefix, leftXPathConditions);
		
		return String.format("%s/following-sibling::%s", leftXPath, rightXPath);
	
	}

	@Override
	protected int[] getSpecificityElements() {
		int[] toReturn = new int[3];
		int[] beforeMainSelectorSpecificity = this.beforeMainSelector.getSpecificityElements();
		int[] mainSelectorSpecificity = this.mainSelector.getSpecificityElements();
		toReturn[0] = beforeMainSelectorSpecificity[0] + mainSelectorSpecificity[0];
		toReturn[1] = beforeMainSelectorSpecificity[1] + mainSelectorSpecificity[1];
		toReturn[2] = beforeMainSelectorSpecificity[2] + mainSelectorSpecificity[2];
		return toReturn;
	}

	@Override
	public SimpleSelector getRightHandSideSelector() {
		return mainSelector;
	}

	@Override
	public BaseSelector getLeftHandSideSelector() {
		return beforeMainSelector;
	}

}
