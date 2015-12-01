package ca.concordia.cssanalyser.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class RefactorToSatisfyDependencies {
	
	/**
	 * Refactores a stylesheet (that possibly breaks some dependencies) to satisfy the given dependencies,
	 * by re-ordering selectors.
	 * It tries to minimize the moves.
	 * @param styleSheet
	 * @param listOfDependenciesToBeHeld
	 * @return
	 */
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet styleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		return refactorToSatisfyOverridingDependencies(styleSheet, listOfDependenciesToBeHeld, new ArrayList<>());
	}
	
	/**
	 * Refactores a stylesheet (that possibly breaks some dependencies) to satisfy the given dependencies,
	 * by re-ordering selectors.
	 * @param styleSheet
	 * @param listOfDependenciesToBeHeld
	 * @param newOrdering A List<Integer> should be passed. This list will be cleared,
	 * and will be filled by the selector numbers in the new ordering.
	 * For instance, if this list contains {3, 1, 2}, this means that the selector
	 * which was placed 3rd in the style sheet is 
	 * placed in position 1 of the new style sheet and so forth.
	 * This can be used to track changes in the UI, etc.
	 * @return
	 */
	public StyleSheet refactorToSatisfyOverridingDependencies(StyleSheet styleSheet, CSSValueOverridingDependencyList listOfDependenciesToBeHeld, List<Integer> newOrdering) {
		
		newOrdering.clear();
	
		/*
		 * We map every dependency in the original CSS file to the new selectors in the 
		 * given style sheet.
		 * Every dependency has two selectors, so the map is from dependency to an array
		 * of selectors that has two members always.
		 */
		Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToRealSelectorsMap 
				= getDependencyToSelectorsMap(styleSheet, listOfDependenciesToBeHeld);
		
		
		// 1. Create a Solver 
		Solver solver = new Solver("Selector reordering problem");
		
		// Map every selector to a Solver variable
		Map<Selector, IntVar> createdVars = new HashMap<>();
		
		List<Selector> selectors =  new  ArrayList<>();
		for (Selector s : styleSheet.getAllSelectors())
			selectors.add(s);
		
		// Create one variable for each selector in the style sheet
		for (int i = 0; i < selectors.size(); i++) {
			Selector selectorToBeAdded = selectors.get(i);
			IntVar x = VariableFactory.bounded(i + ": " + selectorToBeAdded.toString(), 1, styleSheet.getNumberOfSelectors(), solver);
			createdVars.put(selectorToBeAdded, x);
		}
		
		/*
		 * Make sure that the changes are minimum.
		 * For all the selectors in the style sheet but the new one, make a constraint so the order
		 * of them are preserved,  
		 */
		for (int i = 0; i < selectors.size() - 2; i++) {
			IntVar x = createdVars.get(selectors.get(i));
			IntVar y = createdVars.get(selectors.get(i + 1));
			solver.post(IntConstraintFactory.arithm(x, "<", y));
		}
		
		// 2. Create variables through the variable factory
		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {

			Selector[] correspondingSelectors = dependencyNodeToRealSelectorsMap.get(dependency);
			
			if (correspondingSelectors == null || correspondingSelectors[0] == null || correspondingSelectors[1] == null)
				continue;

			// Get the ChocoSolver variables for the dependency
			IntVar x = createdVars.get(correspondingSelectors[0]);
			IntVar y = createdVars.get(correspondingSelectors[1]);

			// 3. Create and post constraints by using constraint factories
			solver.post(IntConstraintFactory.arithm(x, "<", y));

		}
		
		// All the variables have to have unique values
		IntVar[] allVars = new IntVar[createdVars.size()];
		allVars = createdVars.values().toArray(allVars);
		// "BC" = bound-consistency
		solver.post(IntConstraintFactory.alldifferent(allVars, "BC"));
	 
		// 4. Define the search strategy (?)
		//solver.set(IntStrategyFactory.inputOrder_InDomainMin(test));
		
		// 5. Launch the resolution process
		boolean result = solver.findSolution();
		
		if (result) {
			/*
			 * Reverse the the map, because we need to see which number is assigned
			 * to which selector
			 */
			Map<Integer, Selector> assignmentToSelectorMap = new HashMap<>();
			for (Selector s : createdVars.keySet()) {
				assignmentToSelectorMap.put(createdVars.get(s).getValue(), s);
			}
			
			StyleSheet refactoredStyleSheet = new StyleSheet();
			
			// Put the selectors in the style sheet in order
			for (int i = 1; i <= assignmentToSelectorMap.size(); i++) {
				Selector selector = assignmentToSelectorMap.get(i);
				newOrdering.add(selector.getSelectorNumber());
				refactoredStyleSheet.addSelector(selector);
			}
		
			return refactoredStyleSheet;
			
		} else {
			return null;
			// It is better to throw something at least. I know.
		}
		
	}

	/**
	 * Returns a Map that maps each dependency to an array (always of size 2) of Selectors.
	 * The first and second items of this array are the selectors in the given style sheet
	 * corresponding to the From and To selectors of each of the given dependencies. 
	 * @param styleSheet
	 * @param listOfDependenciesToBeHeld
	 * @return
	 */
	private Map<CSSValueOverridingDependency, Selector[]> getDependencyToSelectorsMap(StyleSheet styleSheet,
			CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {
		Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap = new HashMap<>();

		for (BaseSelector selector : styleSheet.getAllBaseSelectors()) {

			for (Declaration d : selector.getDeclarations()) {

				for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
					
					if (dependency.getSelector1().selectorEquals(selector) && dependency.getDeclaration1().declarationEquals(d)) {
						// Put the declaration's selector (the selector in the new StyleSheet)
						putCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency, d.getSelector(), 0);
					}
					if (dependency.getSelector2().selectorEquals(selector) && dependency.getDeclaration2().declarationEquals(d)) {
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
			}
		}
		for (CSSValueOverridingDependency d : markedDependenciesToRemove)
			dependencyNodeToSelectorMap.remove(d);
		return dependencyNodeToSelectorMap;
	}

	/**
	 * 
	 * @param dependencyNodeToSelectorMap
	 * @param dependency
	 * @param selector
	 * @param i
	 */
	private void putCorrespondingRealSelectors(Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap,
			CSSValueOverridingDependency dependency, Selector selector, int i) {
		
		Selector[] realSelectorsForThisDependency = getCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency);
		realSelectorsForThisDependency[i] = selector;
		dependencyNodeToSelectorMap.put(dependency, realSelectorsForThisDependency);
		
	}

	/**
	 * Returns real selectors in the style sheet based on the given dependency.
	 * This method returns an array, containing two selectors.
	 * The first item in the array is the selector in the left-hand-side of dependency,
	 * and the second item is the right-hand-side.
	 * If no selectors for this dependency is found, this method returns an empty array.
	 * @param dependencyNodeToSelectorMap
	 * @param dependency
	 * @return
	 */
	private Selector[] getCorrespondingRealSelectors(Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToSelectorMap,
			CSSValueOverridingDependency dependency) {
		
		Selector[] realSelectorsForThisDependency = dependencyNodeToSelectorMap.get(dependency); 
		
		if (realSelectorsForThisDependency == null) {
			realSelectorsForThisDependency = new Selector[2];
		}
		return realSelectorsForThisDependency;
	}
	
}
