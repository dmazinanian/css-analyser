package ca.concordia.cssanalyser.migration.topreprocessors.sass;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorHelper;
import ca.concordia.cssanalyser.parser.ParseException;

public class SassHelper {

	public static StyleSheet compileStyleSheetOnWindowsUsingSass(String filePath) throws ParseException {
		String[] command = new String[] { "cmd.exe", "/c", "sass", filePath };
		return PreprocessorHelper.runProcessToCompilePreprocessorCode(command);
	}
	
}
