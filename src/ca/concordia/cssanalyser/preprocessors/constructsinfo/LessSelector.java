package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessSelector extends LessConstruct {

	private final ASTCssNode node;
	private final boolean hasNesting;
	private String type;
	private String parentName;
	private int parentLine;
	private String parentType;
	private int numberOfBaseSelectors;
	private int numberOfNestableSelectors;
	private int numberOfDeclarations;
	private List<String> parents;

	public LessSelector(ASTCssNode node, List<String> parents, boolean hasNesting, String type, StyleSheet styleSheet) {
		super(styleSheet);
		this.node = node;
		this.parents = parents;
		this.hasNesting = hasNesting;
		this.type = type;
	}
	
	public ASTCssNode getNode() {
		return this.node;
	}

	public int getSourceLine() {
		return this.node.getSourceLine();
	}
	
	public int getSourceColumn() {
		return this.node.getSourceColumn();
	}

	public String getName() {
		return LessASTQueryHandler.getNodeName(node);
	}

	public int getNumberOfBaseSelectors() {
		return this.numberOfBaseSelectors;
	}

	public int getNumberOfNestableSelectors() {
		return this.numberOfNestableSelectors;
	}

	public boolean hasNesting() {
		return this.hasNesting;
	}

	public String getParentName() {
		return this.parentName;
	}

	public int getParentLine() {
		return this.parentLine;
	}

	public String getParentType() {
		return this.parentType;
	}

	public int getNumberOfDeclarations() {
		return this.numberOfDeclarations;
	}

	public String getType() {
		return this.type;
	}

	public int getLevel() {
		return this.parents.size();
	}

	public void setBodyInfo(int numberOfBaseSelectors, int numberOfNestableSelectors, int numberOfDeclarations) {
		this.numberOfBaseSelectors = numberOfBaseSelectors;
		this.numberOfNestableSelectors = numberOfNestableSelectors;
		this.numberOfDeclarations = numberOfDeclarations;
		
	}

	public void setParentInfo(String parentName, int parentLine, String parentType) {
		this.parentName = parentName;
		this.parentLine = parentLine;
		this.parentType = parentType;
		
	}

	public String getFullyQualifiedName() {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> iterator = parents.iterator(); iterator.hasNext();) {
			String parentableLessConstruct = iterator.next();
			builder.append(parentableLessConstruct);
			if (iterator.hasNext())
				builder.append(" ");
		}
		if (parents.size() > 0)
			builder.append(" ");
		builder.append(getName());
		return builder.toString();
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getStyleSheetPath() == null) ? 0 : getStyleSheetPath().hashCode());
		result = prime * result + getFullyQualifiedName().hashCode();
		result = prime * result + this.node.getSourceLine();
		result = prime * result + this.node.getSourceColumn();
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
		LessSelector other = (LessSelector) obj;

		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null) {
				return false;
			}
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath())) {
			return false;
		}
		if (!this.getFullyQualifiedName().equals(other.getFullyQualifiedName()))
			return false;
		if (this.node.getSourceLine() != other.node.getSourceLine())
			return false;
		if (this.node.getSourceColumn() != other.node.getSourceColumn())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getFullyQualifiedName();
	}

}
