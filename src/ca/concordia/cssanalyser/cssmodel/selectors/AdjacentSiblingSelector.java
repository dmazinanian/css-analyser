package ca.concordia.cssanalyser.cssmodel.selectors;

/**
 * selector + selector
 * @author Davood Mazinanian
 *
 */
public class AdjacentSiblingSelector extends SiblingSelector {

	public AdjacentSiblingSelector(SingleSelector firstSelector, SingleSelector secondSelector) {
		super(firstSelector, secondSelector);
	}

	@Override
	public String toString() {
		return beforeMainSelector + " + " + mainSelector;
	}
	
	@Override
	public Selector clone() {
		return new AdjacentSiblingSelector(getFirstSelector(), getSecondSelector());
	}
}
