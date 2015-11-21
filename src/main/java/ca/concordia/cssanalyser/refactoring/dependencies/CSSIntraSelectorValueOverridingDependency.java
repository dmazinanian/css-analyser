package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;

public class CSSIntraSelectorValueOverridingDependency extends CSSValueOverridingDependency {

	public CSSIntraSelectorValueOverridingDependency(BaseSelector selector,
			Declaration declaration1, Declaration declaration2,
			String property) {
		super(declaration1, selector, declaration2, selector, property);
	}
	
}
