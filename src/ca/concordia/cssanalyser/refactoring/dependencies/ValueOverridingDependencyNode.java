package ca.concordia.cssanalyser.refactoring.dependencies;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;

public class ValueOverridingDependencyNode implements CSSDependencyNode {
	private final Declaration declaration;
	public ValueOverridingDependencyNode(Declaration declaration) {
		this.declaration = declaration;
	}
	
	public Declaration getDeclaration() {
		return this.declaration;
	}
		
	@Override                                                                                                                    
	public boolean nodeEquals(CSSDependencyNode otherCSSDependencyNode) {                                                        
		if (!(otherCSSDependencyNode instanceof ValueOverridingDependencyNode))                                                  
			return false;                                                                                                        
		                                                                                                                         
		ValueOverridingDependencyNode otherValueOverridingDependencyNode = (ValueOverridingDependencyNode)otherCSSDependencyNode;
		                                                                                                                         
		return this.declaration.declarationIsEquivalent(otherValueOverridingDependencyNode.declaration);                    
				                                                                                                                 
	}                         
	
	@Override
	public int hashCode() {
		return this.declaration.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.declaration.equals(obj);
	}
	
//	@Override
//	public boolean nodeEquals(CSSDependencyNode otherCSSDependencyNode) {
//		if (!(otherCSSDependencyNode instanceof ValueOverridingDependencyNode))
//			return false;
//		
//		ValueOverridingDependencyNode otherValueOverridingDependencyNode = (ValueOverridingDependencyNode)otherCSSDependencyNode;
//		
//		return this.fromDeclaration.declarationEquals(otherValueOverridingDependencyNode.fromDeclaration) &&
//				this.toDeclaration.declarationEquals(otherValueOverridingDependencyNode.toDeclaration);
//				
//	}
	
	
}
