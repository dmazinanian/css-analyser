package ca.concordia.cssanalyser.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency.InterSelectorDependencyReason;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class RefactorToSatisfyDependencies {

    private static Logger LOGGER
        = FileLogger.getLogger(RefactorToSatisfyDependencies.class);

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

		DefaultDirectedGraph<Selector, DefaultEdge> graph
			= buildDirectedGraph(styleSheet, listOfDependenciesToBeHeld);

		if (graphNotCyclic(graph)) {
			StyleSheet refactoredStyleSheet = new StyleSheet();

			// Put the selectors in the style sheet in order
			Iterator<Selector> i = new TopologicalOrderIterator<>(graph);
			while (i.hasNext()) {
				Selector selector = i.next();
				newOrdering.add(selector.getSelectorNumber());
				refactoredStyleSheet.addSelector(selector);
			}

			return refactoredStyleSheet;
		}

		return null;
	}


	private DefaultDirectedGraph<Selector, DefaultEdge>
		buildDirectedGraph(StyleSheet styleSheet,
						   CSSValueOverridingDependencyList listOfDependenciesToBeHeld) {

		DefaultDirectedGraph<Selector, DefaultEdge> graph
			= new DefaultDirectedGraph<>(DefaultEdge.class);

		// to enforce minimal changes, selector order should be maintained
		// except the last selector whose position may move, so add ordering
		// between each selector and it's previous, except the last
		int numSels = styleSheet.getNumberOfSelectors();
		int count = 0;
		Selector lastSel = null;

		for (Selector s : styleSheet.getAllSelectors()) {
			graph.addVertex(s);
			if (lastSel != null && count < numSels - 1)
				graph.addEdge(lastSel, s);
			lastSel = s;
			count++;
		}

		Map<CSSValueOverridingDependency, Selector[]> dependencyNodeToRealSelectorsMap
				= getDependencyToSelectorsMap(styleSheet, listOfDependenciesToBeHeld);

		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
			if (dependency instanceof CSSInterSelectorValueOverridingDependency) {
				CSSInterSelectorValueOverridingDependency interSelectorValueOverridingDependency = (CSSInterSelectorValueOverridingDependency) dependency;
				if (interSelectorValueOverridingDependency.getDependencyReason() == InterSelectorDependencyReason.DUE_TO_CASCADING) {
					Selector[] correspondingSelectors = dependencyNodeToRealSelectorsMap.get(dependency);

					if (correspondingSelectors == null || correspondingSelectors[0] == null || correspondingSelectors[1] == null)
						continue;

					graph.addEdge(correspondingSelectors[0],
								  correspondingSelectors[1]);
				}
			}
		}

		return graph;
	}


	private boolean graphNotCyclic(DefaultDirectedGraph<Selector, DefaultEdge> graph) {
		CycleDetector<Selector, DefaultEdge> detector
			= new CycleDetector<>(graph);
		return !detector.detectCycles();
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

        SelectorEqualsMap lookup
            = new SelectorEqualsMap(styleSheet.getAllBaseSelectors());

		for (CSSValueOverridingDependency dependency : listOfDependenciesToBeHeld) {
            Selector selector = lookup.get(dependency.getSelector1());
            for (Declaration declaration : selector.getDeclarations()) {
                if (declaration.declarationIsEquivalent(dependency.getDeclaration1())) {
                    // Put the declaration's selector (the selector in the new StyleSheet)
                    putCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency, declaration.getSelector(), 0);
                }
            }

            selector = lookup.get(dependency.getSelector2());
            for (Declaration declaration : selector.getDeclarations()) {
                if (declaration.declarationIsEquivalent(dependency.getDeclaration2())) {
                    putCorrespondingRealSelectors(dependencyNodeToSelectorMap, dependency, declaration.getSelector(), 1);
                }
            }
		}

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


    /**
     * Helper class for getDependencyToSelectorsMap -- maps from selector to
     * selector, but uses selectorEquals() and selectorHashCode() instead of
     * equals and hashCode, this way we can find the "real" selectors corresponding to
     * dependendencies without a nested loop
     */
    private class SelectorEqualsMap {
        private class SelWrap {
            private Selector sel;

            public SelWrap(Selector sel) {
                this.sel = sel;
            }

            public final void setSelector(Selector sel) {
                this.sel = sel;
            }

            public final boolean equals(Object o) {
                if (o instanceof SelWrap) {
                    SelWrap w = (SelWrap)o;
                    return sel.selectorEquals(w.sel);
                }
                return false;
            }

            public final int hashCode() {
                return sel.selectorHashCode();
            }
        }

        // So we don't have to create an object when looking up
        private SelWrap lookup = new SelWrap(null);

        private Map<SelWrap, Selector> selMap;

        public SelectorEqualsMap(List<? extends Selector> selectors) {
            selMap = new HashMap<>(selectors.size());
            for (Selector s : selectors)
                add(s);
        }

        public final void add(Selector s) {
            selMap.put(new SelWrap(s), s);
        }

        public final Selector get(Selector s) {
            lookup.setSelector(s);
            return selMap.get(lookup);
        }
    }
}
