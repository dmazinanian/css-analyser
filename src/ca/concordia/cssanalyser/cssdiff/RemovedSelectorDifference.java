package ca.concordia.cssanalyser.cssdiff;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class RemovedSelectorDifference implements Difference {
	
	private Selector selector;
	
	
	public Selector getSelector() {
		return this.selector;
	}
}
