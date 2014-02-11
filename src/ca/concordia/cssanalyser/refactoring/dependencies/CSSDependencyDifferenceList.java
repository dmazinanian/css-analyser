package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.ArrayList;
import java.util.List;

public class CSSDependencyDifferenceList {

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
	
}
