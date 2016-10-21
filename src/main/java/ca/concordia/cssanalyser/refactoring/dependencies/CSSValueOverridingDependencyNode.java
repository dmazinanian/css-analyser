package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class CSSValueOverridingDependencyNode implements CSSDependencyNode {
	private final Declaration declaration;
	private final Selector selector;
	public CSSValueOverridingDependencyNode(Declaration declaration, Selector selector) {
		this.declaration = declaration;
		this.selector = selector;
	}

	public Declaration getDeclaration() {
		return this.declaration;
	}

	public Selector getSelector() {
		return this.selector;
	}

	@Override
	public boolean nodeEquals(CSSDependencyNode otherCSSDependencyNode) {
		if (!(otherCSSDependencyNode instanceof CSSValueOverridingDependencyNode))
			return false;

		CSSValueOverridingDependencyNode otherValueOverridingDependencyNode = (CSSValueOverridingDependencyNode)otherCSSDependencyNode;

		return this.selector.selectorEquals(otherValueOverridingDependencyNode.getSelector()); // &&
				//(this.declaration.declarationEquals(otherValueOverridingDependencyNode.declaration) ||
				//this.declaration.declarationIsEquivalent(otherValueOverridingDependencyNode.declaration));

	}

    public int nodeHashCode() {
		return this.selector.selectorHashCode();
				//(this.declaration.declarationEquals(otherValueOverridingDependencyNode.declaration) ||
				//this.declaration.declarationIsEquivalent(otherValueOverridingDependencyNode.declaration));

	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((declaration == null) ? 0 : declaration.hashCode());
		result = prime * result
				+ ((selector == null) ? 0 : selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSSValueOverridingDependencyNode other = (CSSValueOverridingDependencyNode) obj;
		if (declaration == null) {
			if (other.declaration != null)
				return false;
		} else if (!declaration.equals(other.declaration))
			return false;
		if (selector == null) {
			if (other.selector != null)
				return false;
		} else if (!selector.equals(other.selector))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return selector.toString() + "<" + selector.getMediaQueryLists()  + ">" + "$" + declaration.toString();
	}


}
