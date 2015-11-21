package ca.concordia.cssanalyser.parser;

import ca.concordia.cssanalyser.parser.flute.FluteCSSParser;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;

public class CSSParserFactory {
	
	public enum CSSParserType {
		LESS,
		FLUTE
	}
	
	public static CSSParser getCSSParser(CSSParserType type) {
		switch (type) {
		case FLUTE:
			return new FluteCSSParser();
		case LESS:
			return new LessCSSParser();
		default:
			throw new IllegalArgumentException();
		
		}
	}
	
}
