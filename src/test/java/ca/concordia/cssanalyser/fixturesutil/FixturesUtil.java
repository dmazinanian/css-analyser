package ca.concordia.cssanalyser.fixturesutil;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;

public class FixturesUtil {
	
	private static final String RESOURCES_PATH = "src/test/resources/";
	
	public static final String SELECTORS_TEST_FILE_PATH = RESOURCES_PATH + "css-source/selectors-test.css";
	public static final String DECLARATIONS_TEST_FILE_PATH = RESOURCES_PATH + "css-source/declarations-test.css";
	public static final String MEDIA_TEST_FILE_PATH = RESOURCES_PATH + "css-source/media-test.css";
	public static final String IMPORT_INLINER_TEST_INPUT = RESOURCES_PATH + "less-source/import-inliner-test.less"; 
	public static final String IMPORT_INLINER_TEST_EXPECTED = RESOURCES_PATH + "less-source/import-inliner-test-expected.less";
	public static final String EMPIRICAL_STUDY_TEST_FILE_PATH = RESOURCES_PATH + "less-source/empirical-study-test.less";
	
	public static StyleSheet getTestCSSFile(String path) {
		StyleSheet styleSheet = null;
		CSSParser cssParser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
		try {
			styleSheet = cssParser.parseExternalCSS(path);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("Error parsing " + path);
		}

		return styleSheet;
	}
	
	public static StyleSheet getStyleSheetFromString(String text) {
		StyleSheet styleSheet = null;
		CSSParser cssParser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
		try {
			styleSheet = cssParser.parseCSSString(text);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("Error parsing " + text);
		}
		return styleSheet;
	}

	public static List<Selector> getSelectorsList(StyleSheet styleSheet) {
		List<Selector> selectors = new ArrayList<>();
		for (Selector selector : styleSheet.getAllSelectors())
			selectors.add(selector);
		return selectors;
	}

	public static List<Declaration> getDeclarations(Selector selector) {
		List<Declaration> declarations = new ArrayList<>();
		for (Declaration declaration : selector.getDeclarations()) {
			declarations.add(declaration);
		}
		return declarations;
	}
}
