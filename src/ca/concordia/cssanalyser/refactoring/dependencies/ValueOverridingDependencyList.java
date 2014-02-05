package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;


public class ValueOverridingDependencyList extends DependencyList<String> {

	public ValueOverridingDependency getDependency(Declaration declaration1, Declaration declaration2) {
		for (CSSDependency<String> dependency : dependencies) {
			ValueOverridingDependency vDependency = (ValueOverridingDependency) dependency;
			if (vDependency.getDeclaration1().declarationEquals(declaration1) &&
					vDependency.getDeclaration2().declarationEquals(declaration2))
				return (ValueOverridingDependency) dependency;
		}
		return null;
	}
	
}
