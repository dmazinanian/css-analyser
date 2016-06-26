package ca.concordia.cssanalyser.preprocessors.constructsinfo;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.fixturesutil.FixturesUtil;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;

@RunWith(Suite.class)
@SuiteClasses({
	LessASTQueryHandlerTest.class
})
public class ConstructsInfoSuite {
	
	private static LessASTQueryHandler queryHandler;
	
	@BeforeClass
	public static void setUpOnce() {
		try {
		File inputFile = new File(FixturesUtil.EMPIRICAL_STUDY_TEST_FILE_PATH);
		ModifiedLessFileSource modifiedLessFileSource = new ModifiedLessFileSource(inputFile);
		StyleSheet lessStyleSheet = LessCSSParser.getLessStyleSheet(modifiedLessFileSource);
		queryHandler = new LessASTQueryHandler(lessStyleSheet);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			System.exit(-1);
		}
	}
	
	static LessASTQueryHandler getQueryHandler() {
		return queryHandler;
	}
}
