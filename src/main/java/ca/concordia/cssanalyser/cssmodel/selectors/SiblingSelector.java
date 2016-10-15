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
	private int hashCode = -1;
	private int selectorHashCode = -1;

	public SiblingSelector(BaseSelector firstSelector, SimpleSelector secondSelector) {
		this(firstSelector, secondSelector, '~');
	}

	public SiblingSelector(BaseSelector firstSelector, SimpleSelector secondSelector, char combinatorCharacter) {
		super(combinatorCharacter);
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
	public boolean equals(Object obj) {
		if (!generalEquals(obj))
			return false;
		return hashCode() == obj.hashCode();
	}

	private boolean generalEquals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SiblingSelector))
			return false;
		return true;
	}

	@Override
	public boolean selectorEquals(Selector otherSelector, boolean considerMediaQueryLists) {
		if (!generalEquals(otherSelector))
			return false;
		if (considerMediaQueryLists && !mediaQueryListsEqual(otherSelector))
			return false;
		SiblingSelector otherIndirectAdjacentSelector = (SiblingSelector)otherSelector;
		return mainSelector.selectorEquals(otherIndirectAdjacentSelector.mainSelector, considerMediaQueryLists) &&
				beforeMainSelector.selectorEquals(otherIndirectAdjacentSelector.beforeMainSelector, considerMediaQueryLists);

	}

    @Override
	public int selectorHashCode(boolean considerMediaQueryLists) {
		if (selectorHashCode == -1) {
			selectorHashCode = 17;
			selectorHashCode = 31 * selectorHashCode + Character.hashCode(getCombinatorCharacter());
			selectorHashCode = 31 * selectorHashCode + (beforeMainSelector == null ? 0 : beforeMainSelector.selectorHashCode(considerMediaQueryLists));
			selectorHashCode = 31 * selectorHashCode + (mainSelector == null ? 0 : mainSelector.selectorHashCode(considerMediaQueryLists));
			if (considerMediaQueryLists) {
				selectorHashCode = 31 * selectorHashCode + mediaQueryListsHashCode();
			}
		}
		return selectorHashCode;
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = 17;
			hashCode = 31 * hashCode + Character.hashCode(getCombinatorCharacter());
			hashCode = 31 * hashCode + getLocationInfo().hashCode();
			hashCode = 31 * hashCode + (beforeMainSelector == null ? 0 : beforeMainSelector.hashCode());
			hashCode = 31 * hashCode + (mainSelector == null ? 0 : mainSelector.hashCode());
			if (mediaQueryLists != null) {
				hashCode = 31 * hashCode + mediaQueryLists.hashCode();
			}
		}
		return hashCode;
	}

	@Override
	public SiblingSelector clone() {
		SiblingSelector newOne = new SiblingSelector(beforeMainSelector.clone(), mainSelector.clone());
		newOne.setLocationInfo(getLocationInfo());
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

	@Override
	public String toString() {
		return getLeftHandSideSelector() + " " + String.valueOf(getCombinatorCharacter()) + " " + getRightHandSideSelector();
	}

}
