package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.fixturesutil.FixturesUtil;

@RunWith(Suite.class)
@SuiteClasses({
	DeclarationTest.class,
	ShorthandDeclarationTest.class
})
public class DeclarationTestSuite {
	
	private static List<Declaration> declarations;
	
	static Declaration vendorWebkit,
						vendorMoz,
						vendorMs,
						vendorO,
						vendorGeneral;
	
	@BeforeClass
	public static void setUpOnce() {

		StyleSheet styleSheet = FixturesUtil.getTestCSSFile(FixturesUtil.DECLARATIONS_TEST_FILE_PATH);
		
		List<Selector> selectorsList = FixturesUtil.getSelectorsList(styleSheet);
		
		Selector selector = selectorsList.get(0);
		
		declarations = FixturesUtil.getDeclarations(selector);
		
		vendorWebkit = declarations.get(0);
		vendorMoz = declarations.get(1);
		vendorMs = declarations.get(2);
		vendorO = declarations.get(3);
		vendorGeneral = declarations.get(4);
		
	}

}
