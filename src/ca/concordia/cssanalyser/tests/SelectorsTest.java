package ca.concordia.cssanalyser.tests;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.parser.CSSParser;




/**
 * 
 */

/**
 * @author Davood Mazinanian
 *
 */
public class SelectorsTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testSelectorEquality() {
		String css = "p{} p{} a[href*='https'][title]{} a[title][href*='https']{} p,div{} div,p{}";
		CSSParser parser = new CSSParser();
		StyleSheet styleSheet = parser.parseCSSString(css);
		BaseSelector selector1 = (BaseSelector)styleSheet.getAllSelectors().iterator().next();
		BaseSelector selector2 = (BaseSelector)styleSheet.getAllSelectors().iterator().next();
		assertEquals(false, selector1.equals(selector2));
		assertEquals(true, selector1.selectorEquals(selector2));
		BaseSelector selector3 = (BaseSelector)styleSheet.getAllSelectors().iterator().next();
		BaseSelector selector4 = (BaseSelector)styleSheet.getAllSelectors().iterator().next();	
		assertEquals(true, selector3.selectorEquals(selector4));
		GroupingSelector gr1 = (GroupingSelector)styleSheet.getAllSelectors().iterator().next();
		GroupingSelector gr2 = (GroupingSelector)styleSheet.getAllSelectors().iterator().next();
		assertEquals(true, gr1.selectorEquals(gr2));
		assertEquals(false, gr1.equals(gr2));
	}

}
