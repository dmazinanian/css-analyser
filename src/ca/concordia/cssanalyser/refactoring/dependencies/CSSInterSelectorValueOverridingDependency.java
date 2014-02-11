package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class CSSInterSelectorValueOverridingDependency extends
		CSSValueOverridingDependency {

	public CSSInterSelectorValueOverridingDependency(Selector selector1,
			Declaration declaration1, Selector selector2,
			Declaration declaration2, String property) {
		super(declaration1, selector1, declaration2, selector2, property);
	}
	
}
