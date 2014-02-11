package ca.concordia.cssanalyser.refactoring.dependencies;

import java.util.HashSet;
import java.util.Set;


public class CSSValueOverridingDependencyList extends CSSDependencyList<CSSValueOverridingDependency> {

//	/**
//	 * Return a dependency which is from a given declaration to another given declaration
//	 * @param declaration1
//	 * @param declaration2
//	 * @return
//	 */
//	public CSSValueOverridingDependency getDependency(BaseSelector selector1, Declaration declaration1, 
//													BaseSelector selector2, Declaration declaration2) {
//		for (CSSValueOverridingDependency dependency : dependencies) {
//			CSSValueOverridingDependency vDependency = (CSSValueOverridingDependency) dependency;
//			if (vDependency.getDeclaration1().declarationEquals(declaration1) &&
//					vDependency.getDeclaration2().declarationEquals(declaration2))
//				return dependency;
//		}
//		return null;
//	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CSSValueOverridingDependencyList))
			return false;
		CSSValueOverridingDependencyList other = (CSSValueOverridingDependencyList)obj;
		if (size() != other.size())
			return false;
		Set<Integer> checked = new HashSet<>();
		for (int i = 0; i < size(); i++) {
			CSSDependency<String> dependency = get(i);
			boolean found = false;
			for (int j = 0; j < other.size(); j++) {
				if (checked.contains(j))
					continue;
				CSSDependency<String> dependency2 = other.get(j);
				if (dependency.equals(dependency2)) {
					found = true;
					checked.add(j);
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
