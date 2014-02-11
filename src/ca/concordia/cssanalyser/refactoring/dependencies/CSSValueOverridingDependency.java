package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public abstract class CSSValueOverridingDependency extends CSSDependency<String> {

	public CSSValueOverridingDependency(Declaration declaration1, Selector selector1, Declaration declaration2, Selector selector2, String property) {
		super(new CSSValueOverridingDependencyNode(declaration1, selector1), new CSSValueOverridingDependencyNode(declaration2, selector2));
		this.addDependencyLabel(property);
	}

	public Declaration getDeclaration1() {
		return getStartingNode().getDeclaration();
	}

	public Declaration getDeclaration2() {
		return getEndingNode().getDeclaration();
	}
	
	public Selector getSelector1() {
		return getStartingNode().getSelector();
	}
	public Selector getSelector2() {
		return getEndingNode().getSelector();
	}
	
	@Override
	public CSSValueOverridingDependencyNode getStartingNode() {
		return (CSSValueOverridingDependencyNode)super.getStartingNode();
	}
	
	@Override
	public CSSValueOverridingDependencyNode getEndingNode() {
		return (CSSValueOverridingDependencyNode)super.getEndingNode();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getStartingNode() == null) ? 0 : getStartingNode().hashCode());
		result = prime * result + ((getLabelsString() == null) ? 0 : getLabelsString().hashCode());
		result = prime * result + ((getEndingNode() == null) ? 0 : getEndingNode().hashCode());
		return result;
	}

		
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CSSValueOverridingDependency))
			return false;
		CSSValueOverridingDependency other = (CSSValueOverridingDependency)obj;
		if (getStartingNode() == null) {
			if (other.getStartingNode() != null)
				return false;
		} else if (!getStartingNode().nodeEquals(other.getStartingNode()))
			return false;
		if (getEndingNode() == null) {
			if (other.getEndingNode() != null)
				return false;
		} else if (!getEndingNode().nodeEquals(other.getEndingNode()))
			return false;
		if (this.getDependencyLabels() == null) {
			if (other.getDependencyLabels() != null)
				return false;
		} else if (!this.getDependencyLabels().equals(other.getDependencyLabels()))
			return false;
		return true;
	}
	
	/**
	 * Returns true if current dependency has the given selector 
	 * participating in this dependency
	 * @param selector
	 * @return
	 */
	public boolean containsSelector(Selector selector) {
		return getSelector1().selectorEquals(selector) || getSelector2().selectorEquals(selector);
	}
	
	@Override
	public int getSpecialHashCode() {
		return getSpecialHashCode(this.getSelector1(), this.getDeclaration1(), this.getSelector2(), this.getDeclaration2());
	}

	public static int getSpecialHashCode(Selector s1, Declaration d1, Selector s2, Declaration d2) {
		return (s1 + "$" + d1 + " -> " + s2 + "$" + d2).hashCode();
	}
	
	@Override
	public String toString() {
		return getStartingNode() + "->" + getEndingNode() + "(" + getLabelsString() + ")";
	}
}
