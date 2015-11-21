package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.StyleSheet;

public class LessNesting extends LessConstruct {

	private final int sourceLine;
	private final int sourceColumn;
	private final String parentName;
	private final String selectorName;
	private final int level;

	public LessNesting(int sourceLine, int sourceColumn, String parentName, String selectorName, int level, StyleSheet styleSheet) {
		super(styleSheet);
		this.sourceLine = sourceLine;
		this.sourceColumn = sourceColumn;
		this.parentName = parentName;
		this.selectorName = selectorName;
		this.level = level;
	}

	public int getLevel() {
		return this.level;
	}

	public String getSelectorName() {
		return this.selectorName;
	}

	public String getParentName() {
		return this.parentName;
	}

	public int getSourceLine() {
		return this.sourceLine;
	}

	public int getSourceColumn() {
		return sourceColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + ((parentName == null) ? 0 : parentName.hashCode());
		result = prime * result + ((selectorName == null) ? 0 : selectorName.hashCode());
		result = prime * result + sourceColumn;
		result = prime * result + sourceLine;
		result = prime * result + ((getStyleSheetPath() == null) ? 0 : getStyleSheetPath().hashCode());
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
		LessNesting other = (LessNesting) obj;
		if (level != other.level)
			return false;
		if (parentName == null) {
			if (other.parentName != null)
				return false;
		} else if (!parentName.equals(other.parentName))
			return false;
		if (selectorName == null) {
			if (other.selectorName != null)
				return false;
		} else if (!selectorName.equals(other.selectorName))
			return false;
		if (sourceColumn != other.sourceColumn)
			return false;
		if (sourceLine != other.sourceLine)
			return false;
		if (getStyleSheetPath() == null) {
			if (other.getStyleSheetPath() != null)
				return false;
		} else if (!getStyleSheetPath().equals(other.getStyleSheetPath()))
			return false;
		return true;
	}
	
	

}
