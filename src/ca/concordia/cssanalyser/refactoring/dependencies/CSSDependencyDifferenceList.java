package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSSDependencyDifferenceList implements Iterable<CSSDependencyDifference<?>> {

	private final List<CSSDependencyDifference<?>> differences;
	
	public CSSDependencyDifferenceList() {
		differences = new ArrayList<>();
	}
	
	public void add(CSSDependencyDifference<?> difference) {
		differences.add(difference);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (CSSDependencyDifference<?> difference : differences) {
			builder.append(difference + System.lineSeparator());
		}
		return builder.toString();
	}
	
	public int size() {
		return differences.size();
	}

	@Override
	public Iterator<CSSDependencyDifference<?>> iterator() {
		return differences.iterator();
	}
	

	
}
