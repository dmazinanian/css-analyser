package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessInterpolationInfo extends LessConstruct {

	private final ASTCssNode node;

	public LessInterpolationInfo(ASTCssNode node, StyleSheet styleSheet) {
		super(styleSheet);
		if (node instanceof InterpolableName || node instanceof CssString || node instanceof EscapedValue)
			this.node = node;
		else
			this.node = null;
	}

	public String getInterpolableNameAsString() {
		
		if (node != null) {
			if (node instanceof InterpolableName)
				return ((InterpolableName)this.getNode()).getName();
			else if (node instanceof CssString)
				return ((CssString)this.getNode()).getValue();
			else if (node instanceof EscapedValue)
				return ((EscapedValue)this.getNode()).getValue();
		}
		
		return "";
	}

	public ASTCssNode getNode() {
		return node;
	}
	
	public int getLine() {
		return node.getSourceLine();
	}
	
	public int getColumn() {
		return node.getSourceColumn();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getColumn();
		result = prime * result + getLine();
		result = prime * result + ((getInterpolableNameAsString() == null) ? 0 : getInterpolableNameAsString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LessInterpolationInfo other = (LessInterpolationInfo) obj;
		if (getColumn() != other.getColumn()) {
			return false;
		}
		if (getLine() != other.getLine()) {
			return false;
		}
		if (getInterpolableNameAsString() == null) {
			if (other.getInterpolableNameAsString() != null) {
				return false;
			}
		} else if (!getInterpolableNameAsString().equals(other.getInterpolableNameAsString())) {
			return false;
		}
		return true;
	}
	
	

}
