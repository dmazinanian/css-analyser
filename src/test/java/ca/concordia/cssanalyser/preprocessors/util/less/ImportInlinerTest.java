package ca.concordia.cssanalyser.preprocessors.util.less;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ca.concordia.cssanalyser.fixturesutil.FixturesUtil;
import ca.concordia.cssanalyser.io.IOHelper;

public class ImportInlinerTest {

	@Test
	public void testreplaceImports() {
		try {
			String result = ImportInliner.replaceImports(new File(FixturesUtil.IMPORT_INLINER_TEST_INPUT), false);
			String expected = IOHelper.readFileToString(FixturesUtil.IMPORT_INLINER_TEST_EXPECTED);
			assertEquals(expected, result);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
