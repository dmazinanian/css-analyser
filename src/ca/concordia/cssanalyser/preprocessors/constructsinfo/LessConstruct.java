package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import com.github.sommeri.less4j.core.ast.StyleSheet;

public abstract class LessConstruct {
	
	private final StyleSheet styleSheet;
	
	public LessConstruct(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}
	
	public String getStyleSheetPath() {
		String path = null;
		if (this.styleSheet != null)
			path = this.styleSheet.getSource().toString();
		return path;
	}
	
}
