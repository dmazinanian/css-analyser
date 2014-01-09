package ca.concordia.cssanalyser.cssdiff.differences;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class RemovedSelectorDifference extends Difference {
	
	private Selector selector;
	
	public RemovedSelectorDifference(StyleSheet styleSheet1, StyleSheet styleSheet2, Selector removedSelector) {
		super(styleSheet1, styleSheet2);
		this.selector = removedSelector;
	}
	
	
	public Selector getSelector() {
		return this.selector;
	}
	
	@Override
	public String toString() {
		return "Selector " + selector.toString() + " has been removed from the first stylesheet.";
	}
}
