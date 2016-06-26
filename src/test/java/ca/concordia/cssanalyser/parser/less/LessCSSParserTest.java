package ca.concordia.cssanalyser.parser.less;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.parser.ParseException;

public class LessCSSParserTest {

	@Test
	public void parseCSSStringTest() {
		String cssString = ".important { border: solid 1px red !important; }";
		LessCSSParser lessCSSParser = new LessCSSParser();
		try {
			StyleSheet styleSheet = lessCSSParser.parseCSSString(cssString);
			Selector selector = styleSheet.getAllSelectors().iterator().next();
			Declaration declaration = selector.getDeclarations().iterator().next();
			assertTrue(declaration.isImportant());
			assertEquals("border", declaration.getProperty());
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}

}
