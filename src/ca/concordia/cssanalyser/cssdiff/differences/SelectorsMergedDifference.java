package ca.concordia.cssanalyser.cssdiff.differences;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class SelectorsMergedDifference extends Difference {

	private Selector finalSelector;
	private List<Selector> mergedSelectors;
	
	public SelectorsMergedDifference(StyleSheet styleSheet1,
			StyleSheet styleSheet2, Selector finalSelector) {
		super(styleSheet1, styleSheet2);
		mergedSelectors = new ArrayList<>();
		this.finalSelector = finalSelector; 
	}

	public List<Selector> getMergedSelectors() {
		return mergedSelectors;
	}
	
	public Selector getFinalselector() {
		return finalSelector;
	}
	
	public void addMergedSelector(Selector selector) {
		mergedSelectors.add(selector);
	}
	
	@Override
	public String toString() {
		StringBuilder torReturn = new StringBuilder("Selectors ");
		for (Selector s : mergedSelectors)
			torReturn.append(s + " - ");
		torReturn.delete(torReturn.length() - 2, torReturn.length());
		torReturn.append("are now merged in selector " + finalSelector);
		return torReturn.toString();
	}

}
