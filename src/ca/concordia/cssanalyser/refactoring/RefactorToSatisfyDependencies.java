package ca.concordia.cssanalyser.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class RefactorToSatisfyDependencies {
	
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet originalStyleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		StyleSheet styleSheet = new StyleSheet();
		
		// Only selectors which have a role in the dependencies list are important to be considered.
		LinkedList<Selector> selectorsToReArrange = new LinkedList<>();
		for (Selector selector : originalStyleSheet.getAllSelectors()) {
			boolean selectorFound = false;
			for (Declaration d : selector.getDeclarations()) {
				for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
					if ((dependency.getSelector1().selectorEquals(selector) || dependency.getSelector2().selectorEquals(selector)) &&
							(dependency.getDeclaration1().declarationEquals(d) || dependency.getDeclaration2().declarationEquals(d))) {
						selectorFound = true;
						break;
					}
				}
			}
			if (!selectorFound)
				styleSheet.addSelector(selector);
		}
		
		// Assign numbers 1 to N (positions of the selectors)
		int maxNumberToAssign = selectorsToReArrange.size();
		
		// Solve the CSP for remaining selectors and add them to the style sheet
		Map<Selector, Integer> selectorsAssignment = new HashMap<>();
		Set<Integer> values = new HashSet<>();
		for (int i = 1; i < maxNumberToAssign; i++)
			values.add(i);
		
		boolean result = backTrackingSearch(selectorsAssignment, selectorsToReArrange, values, listOfDependenciesToBeHeld);
		
		if (result) {
			for (Selector s : selectorsAssignment.keySet()) {
				System.out.println(s + " " + selectorsAssignment.get(s));
			}
		}
		
//		for (int i = 0; i < selectorsAssignment.size(); i++)
//			styleSheet.addSelector(selectorsAssignment.get(i));
		
		return styleSheet;
		
	}

	private boolean backTrackingSearch(Map<Selector, Integer> selectorsAssignment, LinkedList<Selector> selectorsToReArrange, Set<Integer> values, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		// If assignment is complete, return
		if (selectorsAssignment.size() == selectorsToReArrange.size()) {
			return true;
		}
		else {
			Set<Integer> valuesCopy = new HashSet<>(values);
			Selector variable = selectorsToReArrange.removeFirst();
			for (int i : values) {
				selectorsAssignment.put(variable, i);
				valuesCopy.remove(i);
				boolean result = backTrackingSearch(selectorsAssignment, selectorsToReArrange, valuesCopy, listOfDependenciesToBeHeld);
				if (result == true && satisfies(selectorsAssignment, listOfDependenciesToBeHeld))
					return true;
				valuesCopy.add(i);
				selectorsAssignment.remove(variable);
				selectorsToReArrange.addFirst(variable);
			}
		}
		return false;
	}

	private boolean satisfies(Map<Selector, Integer> selectorsAssignment, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
			// Wont happen to be equal though we are adding it
			Integer selector1Location = selectorsAssignment.get(dependency.getSelector1());
			Integer selector2Location = selectorsAssignment.get(dependency.getSelector2());
			
			if (selector1Location == null || selector2Location == null || selector1Location < selector2Location)
				return false;
		}
		return true;
	}
	
}
