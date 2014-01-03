package ca.concordia.cssanalyser.cssdiff;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class Diff {
	public DifferenceList diff(StyleSheet styleSheet1, StyleSheet styleSheet2) {
		DifferenceList differences = new DifferenceList(styleSheet1, styleSheet2);
		
		for (Selector selector : styleSheet1.getAllSelectors()) {
			boolean found = false;
			for (Selector s : styleSheet2.getAllSelectors()) {
				if (s.selectorEquals(selector)) {
					// ?
					found = true;
					break;
				}
			}
			if (!found) {
				//Difference difference = new RemovedSelectorDifference(selector);
			}
		}
		
		return differences;
	}
}
