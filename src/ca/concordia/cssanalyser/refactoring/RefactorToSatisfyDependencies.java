package ca.concordia.cssanalyser.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class RefactorToSatisfyDependencies {
	
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet originalStyleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		StyleSheet styleSheet = new StyleSheet();
		
		// Only selectors which have a role in the dependencies list are important to be considered.
		List<Selector> selectorsToReArrange = new ArrayList<>();
		for (Selector selector : originalStyleSheet.getAllSelectors()) {
			for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
				if (dependency.containsSelector(selector)) {
					selectorsToReArrange.add(selector);
				} else {
					styleSheet.addSelector(selector);
				}
			}
		}
		
		int valueStartFrom = originalStyleSheet.getAllSelectors().size() - styleSheet.getAllSelectors().size();
		
		// Solve the CSP for remaining selectors and add them to the style sheet
		Map<Selector, Integer> selectorsAssignment = new HashMap<>();
		//selectorsToReArrange ??
		backTrackingSearch(selectorsAssignment, selectorsToReArrange, valueStartFrom, listOfDependenciesToBeHeld);
		
//		for (int i = 0; i < selectorsAssignment.size(); i++)
//			styleSheet.addSelector(selectorsAssignment.get(i));
		
		return styleSheet;
		
	}

	private void backTrackingSearch(Map<Selector, Integer> selectorsAssignment, List<Selector> selectorsToReArrange, int startFrom, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		if (selectorsAssignment.size() == selectorsToReArrange.size() && satisfies(selectorsAssignment, listOfDependenciesToBeHeld))
			return;
		else {
			
		}
	}

	private boolean satisfies(Map<Selector, Integer> selectorsAssignment, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {

		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
			// Wont happen to be equal though we are adding it
			if (selectorsAssignment.get(dependency.getSelector1()) >= 
					selectorsAssignment.get(dependency.getSelector2())) 
				return false;
		}
		return true;
	}
	
}
