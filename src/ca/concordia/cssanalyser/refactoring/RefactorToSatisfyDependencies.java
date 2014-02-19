package ca.concordia.cssanalyser.refactoring;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
		Set<Selector> markedSelectorsToRemove = new HashSet<>();
		final Map<Selector, Integer> selectorToConstraintCountMap = new HashMap<>();
		for (BaseSelector selector : styleSheet.getAllBaseSelectors()) {
			for (Declaration d : selector.getDeclarations()) {
				for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
					
					if (dependency.getSelector1().selectorEquals(selector) && dependency.getDeclaration1().declarationEquals(d)) {
												
						dependencyToNewSelectroMapping.put(dependency.getStartingNode(), d.getSelector());
						markedSelectorsToRemove.add(d.getSelector());
						incrementSelectorConstraintsNumber(selectorToConstraintCountMap, d.getSelector());
						
					} else if (dependency.getSelector2().selectorEquals(selector) && dependency.getDeclaration2().declarationEquals(d)) {
						
						
						dependencyToNewSelectroMapping.put(dependency.getEndingNode(), d.getSelector());
						markedSelectorsToRemove.add(d.getSelector());
						incrementSelectorConstraintsNumber(selectorToConstraintCountMap, d.getSelector());
					}
				}
			}	
		}
		
		TreeSet<Selector> ts = new TreeSet<>(new Comparator<Selector>() {

			@Override
			public int compare(Selector o1, Selector o2) {
				if (o1 == o2)
					return 0;
				if (selectorToConstraintCountMap.get(o1) > selectorToConstraintCountMap.get(o2))
					return -1;
				return 1;
			}
		});
		
		for (Selector s : selectorToConstraintCountMap.keySet())
			ts.add(s);
		
		LinkedList<Selector> selectorsToReArrange = new LinkedList<>(ts);

		
		// Add other selectors to the style sheet
		for (Selector selectorToBeAdded : styleSheet.getAllSelectors()) {
			if (!markedSelectorsToRemove.contains(selectorToBeAdded))
				refactoredStyleSheet.addSelector(selectorToBeAdded);
		}
		
		// Assign numbers 1 to N (positions of the selectors)
		int maxNumberToAssign = selectorsToReArrange.size();
		
		// Solve the CSP for remaining selectors and add them to the style sheet
		Map<Selector, Integer> selectorsAssignment = new HashMap<>();
		LinkedList<Integer> values = new LinkedList<>();
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

	private void incrementSelectorConstraintsNumber(Map<Selector, Integer> selectorToConstraintCountMap, Selector selector) {
		Integer count = selectorToConstraintCountMap.get(selector);
		if (count == null) {
			count = 0;
		}
		count++;
		selectorToConstraintCountMap.put(selector, count);
	}

	private boolean backTrackingSearch(Map<Selector, Integer> selectorsAssignment, 
			LinkedList<Selector> selectorsToReArrange, LinkedList<Integer> values,
			CSSValueOverridingDependencyList listOfDependenciesToBeHeld,
			int numberOfItems, Map<CSSValueOverridingDependencyNode, Selector> dependencyToNewSelectroMapping) {
		// If assignment is complete, return
		if (selectorsAssignment.size() == numberOfItems) {
			return true;
		}
		else {
			LinkedList<Integer> valuesCopy = new LinkedList<>(values); // Value domain for this selector
			Selector variable = selectorsToReArrange.removeFirst();
			while (valuesCopy.size() > 0) {
				int i = valuesCopy.removeFirst();
				selectorsAssignment.put(variable, i);
				if (consistent(selectorsAssignment, listOfDependenciesToBeHeld, dependencyToNewSelectroMapping)) {
					int indexOfValueToBeRemoved = values.indexOf(i);
					if (indexOfValueToBeRemoved >= 0)
						values.remove(indexOfValueToBeRemoved);
					boolean result = backTrackingSearch(selectorsAssignment, selectorsToReArrange, values, listOfDependenciesToBeHeld, numberOfItems, dependencyToNewSelectroMapping);
					if (result)
						return true;
					else {
						selectorsAssignment.remove(variable);
						if (indexOfValueToBeRemoved >= 0)
							values.add(i);
					}
				} else {
					selectorsAssignment.remove(variable);
					//selectorsToReArrange.addFirst(variable);
				}
			}
			selectorsToReArrange.addFirst(variable);
		}
		return false;
	}

	private boolean consistent(Map<Selector, Integer> selectorsAssignment, CSSValueOverridingDependencyList listOfDependenciesToBeHeld, Map<CSSValueOverridingDependencyNode, Selector> dependencyToNewSelectroMapping) {
		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {

			Selector selector1 = dependencyToNewSelectroMapping.get(dependency.getStartingNode());
			Integer selector1Location = selectorsAssignment.get(selector1);
			
			Selector selector2 = dependencyToNewSelectroMapping.get(dependency.getEndingNode());
			Integer selector2Location = selectorsAssignment.get(selector2);
			
			if (selector1Location != null && selector2Location != null)
				if (selector1Location > selector2Location)
					return false;
				else if (selector1Location == selector2Location && selector1 != selector2)
					return false;
		}
		return true;
	}
	
}
