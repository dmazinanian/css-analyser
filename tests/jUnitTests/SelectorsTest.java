package jUnitTests;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import parser.CSSParser;

import CSSModel.StyleSheet;
import CSSModel.selectors.AtomicElementSelector;
import CSSModel.selectors.GroupedSelectors;


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
		AtomicElementSelector selector1 = (AtomicElementSelector)styleSheet.getAllSelectors().iterator().next();
		AtomicElementSelector selector2 = (AtomicElementSelector)styleSheet.getAllSelectors().iterator().next();
		assertEquals(false, selector1.equals(selector2));
		assertEquals(true, selector1.selectorEquals(selector2));
		AtomicElementSelector selector3 = (AtomicElementSelector)styleSheet.getAllSelectors().iterator().next();
		AtomicElementSelector selector4 = (AtomicElementSelector)styleSheet.getAllSelectors().iterator().next();	
		assertEquals(true, selector3.selectorEquals(selector4));
		GroupedSelectors gr1 = (GroupedSelectors)styleSheet.getAllSelectors().iterator().next();
		GroupedSelectors gr2 = (GroupedSelectors)styleSheet.getAllSelectors().iterator().next();
		assertEquals(true, gr1.selectorEquals(gr2));
		assertEquals(false, gr1.equals(gr2));
	}

}
