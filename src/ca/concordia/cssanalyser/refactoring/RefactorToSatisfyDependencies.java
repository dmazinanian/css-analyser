package ca.concordia.cssanalyser.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class RefactorToSatisfyDependencies {
	
	/**
	 * Refactores a stylesheet to satisfy the given dependencies
	 * @param styleSheet
	 * @param listOfDependenciesToBeHeld
	 * @return
	 */
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet styleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		
		StyleSheet refactoredStyleSheet = new StyleSheet();
		
		
		/*
		 * Only selectors which have a role in the dependencies list are important to be considered.
		 * Others must be added to the style sheet without any problem 
		 */
	
		Set<Selector> markedSelectorsToRemove = new HashSet<>();
		
		/*
		 * We map every dependency in the original CSS file to the new selectors in the 
		 * given style sheet.
		 * Every dependency has two selectors, so the map is from dependency to an array
		 * of selectors that has two members always.
		 */
		Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap = new HashMap<>();

		for (BaseSelector selector : styleSheet.getAllBaseSelectors()) {

			for (Declaration d : selector.getDeclarations()) {

				for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
					
					if (dependency.getSelector1().selectorEquals(selector) && dependency.getDeclaration1().declarationEquals(d)) {
						markedSelectorsToRemove.add(d.getSelector());
						putCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency, d.getSelector(), 0);
					}
					if (dependency.getSelector2().selectorEquals(selector) && dependency.getDeclaration2().declarationEquals(d)) {
						markedSelectorsToRemove.add(d.getSelector());
						putCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency, d.getSelector(), 1);
					}
				}
			}	
		}
		
		// IntraSelector dependency shouldn't be here
		Set<CSSValueOverridingDependency> markedDependenciesToRemove = new HashSet<>();
		for (CSSValueOverridingDependency d : dependencyNodeToSelectorMap.keySet()) {
			Selector[] selectors = getCorrespondingRealSelectors(dependencyNodeToSelectorMap, d);
			if (selectors[0] == selectors[1]) {
				markedDependenciesToRemove.add(d);
				markedSelectorsToRemove.remove(selectors[0]);
				markedSelectorsToRemove.remove(selectors[1]);
			}
		}
		for (CSSValueOverridingDependency d : markedDependenciesToRemove)
			dependencyNodeToSelectorMap.remove(d);
		
		
		// Add other selectors to the style sheet
		for (Selector selectorToBeAdded : styleSheet.getAllSelectors()) {
			if (!markedSelectorsToRemove.contains(selectorToBeAdded))
				refactoredStyleSheet.addSelector(selectorToBeAdded);
		}
		
		// 1. Create a Solver 
		Solver solver = new Solver("Selector reordering problem");
		
		// Map every selector to a Solver variable
		Map<Selector, IntVar<?>> createdVars = new HashMap<>();
		
		// 2. Create variables through the variable factory
		// For each selector, we define a variable
		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {

			Selector[] correspondingSelectors = dependencyNodeToSelectorMap.get(dependency);
			
			if (correspondingSelectors == null)
				continue;
			
			Selector s1 = correspondingSelectors[0];
			Selector s2 = correspondingSelectors[1];

			IntVar<?> x, y;
			
			if (!createdVars.containsKey(s1)) {
				x = VariableFactory.bounded(s1.toString(), 1, markedSelectorsToRemove.size(), solver);
				createdVars.put(s1, x);
			}
			else {
				x = createdVars.get(s1);
			}

			if (!createdVars.containsKey(s2)) {
				y = VariableFactory.bounded(s2.toString(), 1, markedSelectorsToRemove.size(), solver);
				createdVars.put(s2, y);
			}
			else {
				y = createdVars.get(s2);
			}

			// 3. Create and post constraints by using constraint factories
			solver.post(IntConstraintFactory.arithm(x, "<", y));

		}

		// All the variables have to have unique values
		IntVar<?>[] allVars = new IntVar<?>[createdVars.size()];
		allVars = createdVars.values().toArray(allVars);
		solver.post(IntConstraintFactory.alldifferent(allVars, "BC"));
	 
		// 4. Define the search strategy (?)
		//solver.set(IntStrategyFactory.inputOrder_InDomainMin(test));
		
		// 5. Launch the resolution process
		boolean result = solver.findSolution();
		
		if (result) {
			// Reverse the the map, because we need to see which number is assigned
			// to which selector
			Map<Integer, Selector> assignmentToSelectorMap = new HashMap<>();
			for (Selector s : createdVars.keySet()) {
				assignmentToSelectorMap.put(createdVars.get(s).getValue(), s);
			}
			
			// Put the selectors in the stylesheet in order
			for (int i = 1; i <= assignmentToSelectorMap.size(); i++)
				refactoredStyleSheet.addSelector(assignmentToSelectorMap.get(i));
		
			return refactoredStyleSheet;
			
		} else {
			return null;
			// It is better to throw something at least. I know.
		}
		
	}

	private void putCorrespondingRealSelectors(Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap,
			CSSValueOverridingDependency dependency, Selector selector, int i) {
		
		Selector[] realSelectorsForThisDependency = getCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency);
		realSelectorsForThisDependency[i] = selector;
		dependencyNodeToSelectorMap.put(dependency, realSelectorsForThisDependency);
		
	}

	private Selector[] getCorrespondingRealSelectors(Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap,
			CSSValueOverridingDependency dependency) {
		
		Selector[] realSelectorsForThisDependency = dependencyNodeToSelectorMap.get(dependency); 
		
		if (realSelectorsForThisDependency == null) {
			realSelectorsForThisDependency = new Selector[2];
		}
		return realSelectorsForThisDependency;
	}

	
	
}
