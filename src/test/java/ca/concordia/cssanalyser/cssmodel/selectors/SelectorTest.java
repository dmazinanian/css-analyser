package ca.concordia.cssanalyser.cssmodel.selectors;

import static org.junit.Assert.*;

import org.junit.Test;

import static ca.concordia.cssanalyser.cssmodel.selectors.SelectorTestSuite.*;

public class SelectorTest {

	@Test
	public void testEquals() {		
		assertFalse(simpleOutOfMedia.equals(simpleInMedia));
		assertFalse(simpleInMedia.equals(simpleInTheSameMedia));
		assertFalse(simpleInTheSameMedia.equals(simpleInDifferentMedia));
		
		assertFalse(descendantOutOfMedia.equals(descendantInMedia));
		assertFalse(descendantInMedia.equals(descendantInTheSameMedia));
		assertFalse(descendantInTheSameMedia.equals(descendantInDifferentMedia));
		
		assertFalse(siblingOutOfMedia.equals(siblingInMedia));
		assertFalse(siblingInMedia.equals(siblingInTheSameMedia));
		assertFalse(siblingInTheSameMedia.equals(siblingInDifferentMedia));
		
		assertFalse(groupingOutOfMedia.equals(groupingInMedia));
		assertFalse(groupingInMedia.equals(groupingInTheSameMedia));
		assertFalse(groupingInTheSameMedia.equals(groupingInDifferentMedia));
	}
	
	@Test
	public void testSelectorEquals() {
		assertFalse(simpleOutOfMedia.selectorEquals(simpleInMedia));
		assertTrue(simpleInMedia.selectorEquals(simpleInTheSameMedia));
		assertFalse(simpleInTheSameMedia.selectorEquals(simpleInDifferentMedia));
		
		assertFalse(descendantOutOfMedia.selectorEquals(descendantInMedia));
		assertTrue(descendantInMedia.selectorEquals(descendantInTheSameMedia));
		assertFalse(descendantInTheSameMedia.selectorEquals(descendantInDifferentMedia));
		
		assertFalse(siblingOutOfMedia.selectorEquals(siblingInMedia));
		assertTrue(siblingInMedia.selectorEquals(siblingInTheSameMedia));
		assertFalse(siblingInTheSameMedia.selectorEquals(siblingInDifferentMedia));
		
		assertFalse(groupingOutOfMedia.selectorEquals(groupingInMedia));
		assertTrue(groupingInMedia.selectorEquals(groupingInTheSameMedia));
		assertFalse(groupingInTheSameMedia.selectorEquals(groupingInDifferentMedia));
	}

}
