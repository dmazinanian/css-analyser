package ca.concordia.cssanalyser.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.parser.CSSParser;


public class DeclarationsTest {

	 

	@Test
	public void test() {
		String css = "p{border: 0px; border: 0px; background-position: 0px top; background-position: top 0px;" +
				" }";
		CSSParser parser = new CSSParser();
		StyleSheet styleSheet = parser.parseCSSString(css);
		Declaration d1 = styleSheet.getAllDeclarations().iterator().next();
		Declaration d2 = styleSheet.getAllDeclarations().iterator().next();
		assertEquals(true, d1.equals(d2));
		assertEquals(true, d1.hashCode() == d2.hashCode());
		
		
		Declaration d3 = styleSheet.getAllDeclarations().iterator().next();
		Declaration d4 = styleSheet.getAllDeclarations().iterator().next();
		assertEquals(true, d3.equals(d4));
		assertEquals(true, d4.hashCode() == d4.hashCode());
	}

}
