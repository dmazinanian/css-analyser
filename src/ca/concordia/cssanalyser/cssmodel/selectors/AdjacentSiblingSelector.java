package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * selector + selector
 * @author Davood Mazinanian
 *
 */
public class AdjacentSiblingSelector extends SiblingSelector {

	public AdjacentSiblingSelector(BaseSelector firstSelector, BaseSelector secondSelector) {
		super(firstSelector, secondSelector);
	}

	@Override
	public String toString() {
		return beforeMainSelector + " + " + mainSelector;
	}
	
	@Override
	public AdjacentSiblingSelector clone() {
		return new AdjacentSiblingSelector(getFirstSelector().clone(), getSecondSelector().clone());
	}
}
