package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;

public abstract class ValueOverridingDependency extends CSSDependency<String> {

	public ValueOverridingDependency(Declaration declaration1, Declaration declaration2, String property) {
		super(new ValueOverridingDependencyNode(declaration1), new ValueOverridingDependencyNode(declaration2));
		this.addDependencyLabel(property);
	}

	public Declaration getDeclaration1() {
		return ((ValueOverridingDependencyNode)this.getStartingNode()).getDeclaration();
	}

	public Declaration getDeclaration2() {
		return ((ValueOverridingDependencyNode)this.getEndingNode()).getDeclaration();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.getStartingNode() == null) ? 0 : this.getStartingNode().hashCode());
		result = prime * result
				+ ((this.getEndingNode() == null) ? 0 : this.getEndingNode().hashCode());
		result = prime * result
				+ ((this.getDependencyLabels() == null) ? 0 : this.getDependencyLabels().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ValueOverridingDependency))
			return false;
		ValueOverridingDependency other = (ValueOverridingDependency)obj;
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
}
