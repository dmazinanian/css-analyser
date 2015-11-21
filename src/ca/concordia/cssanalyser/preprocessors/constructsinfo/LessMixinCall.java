package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessMixinCall extends LessConstruct {

	private final int numberOfMultiValuedArguments;
	private final MixinReference reference;
	private LessMixinDeclaration mixinDeclaration;

	public LessMixinCall(MixinReference reference, int numberOfMultiValuedArguments, StyleSheet styleSheet) {
		super(styleSheet);
		this.reference = reference;
		this.numberOfMultiValuedArguments = numberOfMultiValuedArguments;
	}

	public int getNumberOfMultiValuedArguments() {
		return numberOfMultiValuedArguments;
	}

	public MixinReference getReference() {
		return reference;
	}
	
	public String getName() {
		return reference.getFinalNameAsString();
	}
	
	public int getNumberOfParameters() {
		return reference.getNumberOfDeclaredParameters();
	}

	public ASTCssNode getParentStructure() {
		return getReference().getParent().getParent();
	}

	public String getMixinCallHashString() {
		return String.format("%s(%s)", this.reference.getFinalNameAsString(), this.reference.getNumberOfDeclaredParameters());
	}
	
	@Override
	public String toString() {
		return getMixinCallHashString();
	}

	public void setMixinDeclaration(LessMixinDeclaration mixinDeclaration) {
		this.mixinDeclaration = mixinDeclaration; 
	}
	
	public LessMixinDeclaration getMixinDeclaration() {
		return this.mixinDeclaration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMixinCallHashString() == null) ? 0 : getMixinCallHashString().hashCode());
		result = prime * result + numberOfMultiValuedArguments;
		result = prime * result + ((getStyleSheetPath()  == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + reference.getSourceLine();
		result = prime * result + reference.getSourceColumn();
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
		LessMixinCall other = (LessMixinCall) obj;
		if (getMixinCallHashString() == null) {
			if (other.getMixinCallHashString() != null)
				return false;
		} else if (!getMixinCallHashString().equals(other.getMixinCallHashString()))
			return false;
		if (numberOfMultiValuedArguments != other.numberOfMultiValuedArguments)
			return false;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
			return false;
		if (this.reference == null) {
			if (other.reference != null)
				return false;
		} 
		if (this.reference != null) {
			if (this.reference.getSourceLine() != other.reference.getSourceLine())
				return false;
			if (this.reference.getSourceColumn() != other.reference.getSourceColumn())
				return false;
		}
		return true;
	}
	
	
}
