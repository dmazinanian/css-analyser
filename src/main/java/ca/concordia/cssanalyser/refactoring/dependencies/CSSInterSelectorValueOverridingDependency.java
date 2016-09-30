package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class CSSInterSelectorValueOverridingDependency extends CSSValueOverridingDependency {
	
	public enum InterSelectorDependencyReason {
		DUE_TO_SPECIFICITY,
		DUE_TO_CASCADING
	}
	
	private final InterSelectorDependencyReason dependencyReason;

	public CSSInterSelectorValueOverridingDependency(Selector selector1, Declaration declaration1, 
			Selector selector2, Declaration declaration2, 
			String property, InterSelectorDependencyReason reason) {
		super(declaration1, selector1, declaration2, selector2, property);
		this.dependencyReason = reason;
	}
	
	public boolean areMediaQueryListsDifferent() {
		return getSelector1().mediaQueryListsEqual(getSelector2());
	}

	public InterSelectorDependencyReason getDependencyReason() {
		return dependencyReason;
	}
	
}
