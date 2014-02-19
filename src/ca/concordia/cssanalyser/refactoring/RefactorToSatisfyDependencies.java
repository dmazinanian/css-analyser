package ca.concordia.cssanalyser.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyNode;

public class RefactorToSatisfyDependencies {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RefactorToSatisfyDependencies.class);
	
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet styleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		
		StyleSheet refactoredStyleSheet = new StyleSheet();
		
		Map<CSSValueOverridingDependencyNode, Selector> dependencyToNewSelectroMapping = new HashMap<>();
		
		// Only selectors which have a role in the dependencies list are important to be considered.
		LinkedList<Selector> selectorsToReArrange = new LinkedList<>();
		Set<Selector> markedSelectorsToRemove = new HashSet<>();
		for (BaseSelector selector : styleSheet.getAllBaseSelectors()) {
			for (Declaration d : selector.getDeclarations()) {
				for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
					if (dependency.getSelector1().selectorEquals(selector) && dependency.getDeclaration1().declarationEquals(d)) {
						selectorsToReArrange.add(d.getSelector()); // Must get the real selector for this declaration
						dependencyToNewSelectroMapping.put(dependency.getStartingNode(), d.getSelector());
						markedSelectorsToRemove.add(d.getSelector());
					} else if (dependency.getSelector2().selectorEquals(selector) && dependency.getDeclaration2().declarationEquals(d)) {
						selectorsToReArrange.add(d.getSelector());
						dependencyToNewSelectroMapping.put(dependency.getEndingNode(), d.getSelector());
						markedSelectorsToRemove.add(d.getSelector());
					}
				}
			}	
		}
		
		// Add other selectors to the style sheet
		for (Selector selectorToBeAdded : styleSheet.getAllSelectors()) {
			if (!markedSelectorsToRemove.contains(selectorToBeAdded))
				refactoredStyleSheet.addSelector(selectorToBeAdded);
		}
		
		// Assign numbers 1 to N (positions of the selectors)
		int maxNumberToAssign = selectorsToReArrange.size();
		
		// Solve the CSP for remaining selectors and add them to the style sheet
		Map<Selector, Integer> selectorsAssignment = new HashMap<>();
		Set<Integer> values = new HashSet<>();
		for (int i = 1; i <= maxNumberToAssign; i++)
			values.add(i);
		
		boolean result = backTrackingSearch(selectorsAssignment, selectorsToReArrange, values, listOfDependenciesToBeHeld, maxNumberToAssign, dependencyToNewSelectroMapping);
		
		if (result) {
			// Reverse the order of the map
			Map<Integer, Selector> assignmentToSelectorMap = new HashMap<>();
			for (Selector s : selectorsAssignment.keySet()) {
				assignmentToSelectorMap.put(selectorsAssignment.get(s), s);
			}
			
			for (int i = 1; i <= selectorsAssignment.size(); i++)
				refactoredStyleSheet.addSelector(assignmentToSelectorMap.get(i));
		
			return refactoredStyleSheet;
			
		} else {
			
			LOGGER.warn("Ordering is not possible!");
			throw new RuntimeException("Ordering is not possible");
			
		}
		
	}

	private boolean backTrackingSearch(Map<Selector, Integer> selectorsAssignment, 
			LinkedList<Selector> selectorsToReArrange, Set<Integer> values,
			CSSValueOverridingDependencyList listOfDependenciesToBeHeld,
			int numberOfItems, Map<CSSValueOverridingDependencyNode, Selector> dependencyToNewSelectroMapping) {
		// If assignment is complete, return
		if (selectorsAssignment.size() == numberOfItems) {
			return true;
		}
		else {
			//if (selectorsToReArrange.size() == 0)
			//	return false;
			Set<Integer> valuesCopy = new HashSet<>(values);
			Selector variable = selectorsToReArrange.removeFirst();
			for (int i : values) {
				selectorsAssignment.put(variable, i);
				valuesCopy.remove(i);
				if (consistent(selectorsAssignment, listOfDependenciesToBeHeld, dependencyToNewSelectroMapping)) {
					boolean result = backTrackingSearch(selectorsAssignment, selectorsToReArrange, valuesCopy, listOfDependenciesToBeHeld, numberOfItems, dependencyToNewSelectroMapping);
					if (result)
						return true;
					else {
						valuesCopy.add(i);
						selectorsAssignment.remove(variable);
						//selectorsToReArrange.addFirst(variable);
					}
				} else {
					selectorsAssignment.remove(variable);
					selectorsToReArrange.addFirst(variable);
				}
			}
		}
		return false;
	}

	private boolean consistent(Map<Selector, Integer> selectorsAssignment, CSSValueOverridingDependencyList listOfDependenciesToBeHeld, Map<CSSValueOverridingDependencyNode, Selector> dependencyToNewSelectroMapping) {
		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {

			Integer selector1Location = selectorsAssignment.get(dependencyToNewSelectroMapping.get(dependency.getStartingNode()));
			Integer selector2Location = selectorsAssignment.get(dependencyToNewSelectroMapping.get(dependency.getEndingNode()));
			
			if (selector1Location != null && selector2Location != null && selector1Location >= selector2Location)
				return false;
		}
		return true;
	}
	
}
