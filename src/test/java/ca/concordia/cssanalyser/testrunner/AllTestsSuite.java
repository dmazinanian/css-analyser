package ca.concordia.cssanalyser.testrunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationTestSuite;
import ca.concordia.cssanalyser.cssmodel.media.MediaTestSuite;
import ca.concordia.cssanalyser.cssmodel.selectors.SelectorTestSuite;
import ca.concordia.cssanalyser.parser.less.LessCSSParserTestSuite;
import ca.concordia.cssanalyser.preprocessors.constructsinfo.ConstructsInfoSuite;
import ca.concordia.cssanalyser.preprocessors.util.less.ImportInlinerTest;

@RunWith(Suite.class)
@SuiteClasses({
	SelectorTestSuite.class,
	DeclarationTestSuite.class,
	MediaTestSuite.class,
	ImportInlinerTest.class,
	ConstructsInfoSuite.class,
	LessCSSParserTestSuite.class
})
public class AllTestsSuite {

}
