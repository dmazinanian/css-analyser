package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class InsideSelectorValueOverridingDependency extends ValueOverridingDependency {

	private final Selector selector;
	
	public InsideSelectorValueOverridingDependency(BaseSelector selector,
			Declaration declaration1, Declaration declaration2,
			String property) {
		super(declaration1, declaration2, property);
		this.selector = selector;
	}
	
	public Selector getSelector() {
		return selector;
	}
	
	@Override
	public String toString() {
		return this.getSelector() + "$" + this.getDeclaration1() + 
				" -> " +
				this.getSelector() + "$" + this.getDeclaration2() +
				" (" + this.getLabelsString() + ")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((selector == null) ? 0 : selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (obj.getClass() != this.getClass())
			return false;
		InsideSelectorValueOverridingDependency other = (InsideSelectorValueOverridingDependency)obj;
		if (selector == null) {
			if (other.selector != null)
				return false;
		} else if (!selector.selectorEquals(other.selector))
			return false;
		return true;
	}
	
	

}
