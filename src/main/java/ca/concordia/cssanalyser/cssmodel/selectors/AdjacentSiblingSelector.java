package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * selector + selector
 * @author Davood Mazinanian
 *
 */
public class AdjacentSiblingSelector extends SiblingSelector {

	public AdjacentSiblingSelector(BaseSelector firstSelector, SimpleSelector secondSelector) {
		super(firstSelector, secondSelector);
	}

	@Override
	public String toString() {
		return beforeMainSelector + " + " + mainSelector;
	}
	
	@Override
	public AdjacentSiblingSelector clone() {
		AdjacentSiblingSelector newOne = new AdjacentSiblingSelector(getFirstSelector().clone(), getSecondSelector().clone());
		newOne.setLocationInfo(getLocationInfo());
		newOne.addMediaQueryLists(mediaQueryLists);
		return newOne;
	}
}
