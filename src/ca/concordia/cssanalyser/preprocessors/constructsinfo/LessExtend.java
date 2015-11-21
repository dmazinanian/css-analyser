package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessExtend extends LessConstruct {

	private final Extend extend;

	public LessExtend(Extend extend, StyleSheet styleSheet) {
		super(styleSheet);
		this.extend = extend;
	}
	
	public ASTCssNode getParentConstruct() {
		ASTCssNode parent = this.extend.getParent();
		if (parent instanceof GeneralBody) {
			return parent.getParent();
		} else {
			throw new IllegalArgumentException("Parent is not GeneralBody");
		}
	}
	
	public String getParentConstructName() {
		ASTCssNode parentConstruct = getParentConstruct();
		if (parentConstruct != null)
			return LessASTQueryHandler.getNodeName(parentConstruct);
		return "";
	}
	
	public Extend getExtend() {
		return extend;
	}
	
	public String getTargetSelectorName() {
		return LessASTQueryHandler.getNodeName(extend.getTarget());
	}
	
	public Selector getTargetSelector() {
		return extend.getTarget();
	}

	public boolean isAll() {
		return extend.isAll();
	}

	public int getSourceLine() {
		return extend.getSourceLine();
	}
	
	public int getSourceColumn() {
		return extend.getSourceColumn();
	}

	public String getSpecialHashString() {
		return String.format("%s>%s",  
				LessASTQueryHandler.getNodeName(this.extend.getParent().getParent()),
				LessASTQueryHandler.getNodeName(this.extend));
	}

	public String getExtendAndParentPairString() {
		return "<" + getTargetSelectorName() + "," + getParentConstructName() + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getStyleSheetPath() != null ? getStyleSheetPath().hashCode() : 0);
		result = prime * result + getSpecialHashString().hashCode();
		result = prime * result + extend.getSourceLine();
		result = prime * result + extend.getSourceColumn();
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
		LessExtend other = (LessExtend) obj;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else {
			if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
				return false;
		}
		if (!getSpecialHashString().equals(other.getSpecialHashString()))
			return false;
		if (this.extend.getSourceLine() != other.extend.getSourceLine())
			return false;
		if (this.extend.getSourceColumn() != other.extend.getSourceColumn())
			return false;
		return false;
	}
	
}
