package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessEmbeddedScriptInfo extends LessConstruct {

	private final FunctionExpression functionExpression;

	public LessEmbeddedScriptInfo(FunctionExpression functionExpression, StyleSheet styleSheet) {
		super(styleSheet);
		this.functionExpression = functionExpression;
	}
	
	public String getParameterAsString() {
		return this.functionExpression.getParameter().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getParameterAsString().hashCode());
		result = prime * result + ((getStyleSheetPath() == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + functionExpression.getSourceLine();
		result = prime * result + functionExpression.getSourceColumn();
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
		LessEmbeddedScriptInfo other = (LessEmbeddedScriptInfo) obj;

		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null) {
				return false;
			}
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath())) {
			return false;
		}
		if (!getParameterAsString().equals(other.getParameterAsString()))
			return false;
		if (this.functionExpression.getSourceLine() != other.functionExpression.getSourceLine())
			return false;
		if (this.functionExpression.getSourceColumn() != other.functionExpression.getSourceColumn())
			return false;
		return true;
	}
	
	

}
