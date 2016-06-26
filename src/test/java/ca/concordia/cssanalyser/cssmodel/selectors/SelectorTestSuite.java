package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.fixturesutil.FixturesUtil;

@RunWith(Suite.class)
@SuiteClasses({
	SelectorTest.class
})
public class SelectorTestSuite {

	private static List<Selector> selectors;
	
	private static final String SIMPLE = ".simple";
	private static final String DESCENDANT = ".simple1 .simple2";
	private static final String CHILD = ".simple1 > .simple2";
	private static final String SIBLING = ".simple1 ~ .simple2";
	private static final String ADJACENT = ".simple1 + .simple2";
	private static final String GROUPING = ".base1, .base2";
	
	static Selector simpleOutOfMedia,
			simpleInMedia,
			simpleInTheSameMedia,
			simpleInDifferentMedia;
	
	static Selector descendantOutOfMedia,
			descendantInMedia,
			descendantInTheSameMedia,
			descendantInDifferentMedia;
	
	static Selector childOutOfMedia,
			childInMedia,
			childInTheSameMedia,
			childInDifferentMedia;
	
	static Selector siblingOutOfMedia,
			siblingInMedia,
			siblingInTheSameMedia,
			siblingInDifferentMedia;
	
	static Selector adjacentOutOfMedia,
			adjacentInMedia,
			adjacentInTheSameMedia,
			adjacentInDifferentMedia;
	
	static Selector groupingOutOfMedia,
			groupingInMedia,
			groupingInTheSameMedia,
			groupingInDifferentMedia;
	
	@BeforeClass
	public static void setUpOnce() {

		StyleSheet styleSheet = FixturesUtil.getTestCSSFile(FixturesUtil.SELECTORS_TEST_FILE_PATH);
		
		selectors = FixturesUtil.getSelectorsList(styleSheet);
		
		simpleOutOfMedia 			= getSelector(0, SIMPLE);
		descendantOutOfMedia		= getSelector(1, DESCENDANT);
		childOutOfMedia				= getSelector(2, CHILD);
		siblingOutOfMedia			= getSelector(3, SIBLING);
		adjacentOutOfMedia			= getSelector(4, ADJACENT);
		groupingOutOfMedia 			= getSelector(5, GROUPING);
		
		simpleInMedia 				= getSelector(6,  SIMPLE);
		descendantInMedia			= getSelector(7,  DESCENDANT);
		childInMedia				= getSelector(8,  CHILD);
		siblingInMedia				= getSelector(9,  SIBLING);
		adjacentInMedia				= getSelector(10, ADJACENT);
		groupingInMedia 			= getSelector(11, GROUPING);
		
		simpleInTheSameMedia 		= getSelector(12, SIMPLE);
		descendantInTheSameMedia	= getSelector(13, DESCENDANT);
		childInTheSameMedia			= getSelector(14, CHILD);
		siblingInTheSameMedia		= getSelector(15, SIBLING);
		adjacentInTheSameMedia		= getSelector(16, ADJACENT);
		groupingInTheSameMedia		= getSelector(17, GROUPING);
		
		simpleInDifferentMedia 		= getSelector(18, SIMPLE);
		descendantInDifferentMedia	= getSelector(19, DESCENDANT);
		childInDifferentMedia		= getSelector(20, CHILD);
		siblingInDifferentMedia		= getSelector(21, SIBLING);
		adjacentInDifferentMedia	= getSelector(22, ADJACENT);
		groupingInDifferentMedia	= getSelector(23, GROUPING);

	}

	private static Selector getSelector(int locationInList, String name) {
		
		Selector selector = selectors.get(locationInList);
		if (!name.equals(selector.toString()))
			throw new RuntimeException(String.format("Selector name is %s, expected %s", selector.toString(), name));
		
		return selector;
		
	}
}
