package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class CrossSelectorValueOverridingDependency extends ValueOverridingDependency {
	
	private final Selector selector1;
	private final Selector selector2;
	public CrossSelectorValueOverridingDependency(Selector selector1, Declaration declaration1, 
			Selector selector2, Declaration declaration2, 
			String property) {
		super(declaration1, declaration2, property);
		this.selector1 = selector1;
		this.selector2 = selector2;
	}
	
	public Selector getSelector1() {
		return selector1;
	}

	public Selector getSelector2() {
		return selector2;
	}

	@Override
	public String toString() {
		return this.getSelector1() + "$" + this.getDeclaration1() + 
				" -> " +
				this.getSelector2() + "$" + this.getDeclaration2() +
				" (" + this.getLabelsString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((selector1 == null) ? 0 : selector1.hashCode());
		result = prime * result
				+ ((selector2 == null) ? 0 : selector2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		CrossSelectorValueOverridingDependency other = (CrossSelectorValueOverridingDependency)obj;
		if (selector1 == null) {
			if (other.selector1 != null)
				return false;
		} else if (!selector1.selectorEquals(other.selector1))
			return false;
		if (selector2 == null) {
			if (other.selector2 != null)
				return false;
		} else if (!selector2.selectorEquals(other.selector2))
			return false;
		return true;
	}
}
