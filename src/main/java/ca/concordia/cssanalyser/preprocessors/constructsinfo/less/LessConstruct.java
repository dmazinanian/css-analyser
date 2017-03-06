package ca.concordia.cssanalyser.preprocessors.constructsinfo.less;

import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.preprocessors.constructsinfo.PreprocessorConstruct;

public abstract class LessConstruct implements PreprocessorConstruct {
	
	private final StyleSheet styleSheet;
	
	public LessConstruct(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}
	
	@Override
	public String getStyleSheetPath() {
		String path = null;
		if (this.styleSheet != null)
			path = this.styleSheet.getSource().toString();
		return path;
	}
	
}
