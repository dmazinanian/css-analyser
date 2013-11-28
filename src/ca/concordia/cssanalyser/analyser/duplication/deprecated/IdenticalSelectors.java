package ca.concordia.cssanalyser.analyser.duplication.deprecated;
/*
import java.util.ArrayList;
import java.util.List;

import duplication.Duplication;
import duplication.DuplicationType;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class IdenticalSelectors extends Duplication {

	/*
	 * This field is used to keep all the repeating selectors
	 * of the same type.
	 * /
	private final List<Selector> forWhichSelector;
	
	public IdenticalSelectors(Selector selector) {
		super(DuplicationType.IDENTICAL_SELECTOR);	
		forWhichSelector = new ArrayList<>();
		forWhichSelector.add(selector);
	}
	
	/**
	 * Adds a selector object which is repeating
	 * @param selector
	 * /
	public void addSelector(Selector selector) {
		forWhichSelector.add(selector);
	}
	
	/**
	 * Returns the number of cases that the 
	 * selector is repeating.
	 * @return
	 * /
	public int getNumberOfOccurrences() {
		return forWhichSelector.size();
	}
	
	public List<Selector> getListOfSelectors() {
		return forWhichSelector;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == this)
			 return true;
		
		if (!(obj instanceof IdenticalSelectors)) {
			return false;
		}
		
		IdenticalSelectors duplicationOtherObject = (IdenticalSelectors)obj;
		
		return (this.forWhichSelector.equals(duplicationOtherObject.forWhichSelector));
	}
	
	@Override
	public int hashCode() {
		return 31 * 17 * forWhichSelector.hashCode();
	}
	
	@Override
	public String toString() {
		String string = "Duplication for selector " + forWhichSelector.get(0) + " in the following places: \n";
		for (Selector selector : forWhichSelector)
			string += "\t" + selector.getLineNumber() + " : " + selector.getColumnNumber() + "\n";
		return string;
	}
}*/
