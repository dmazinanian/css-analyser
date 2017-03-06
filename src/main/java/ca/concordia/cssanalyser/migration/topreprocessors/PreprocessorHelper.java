package ca.concordia.cssanalyser.migration.topreprocessors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;

public class PreprocessorHelper {

	public static StyleSheet runProcessToCompilePreprocessorCode(String[] command) throws ParseException {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			process.waitFor();  // wait for process to complete

			StyleSheet resultingStyleSheet = CSSParserFactory.getCSSParser(CSSParserType.LESS).parseCSSString(builder.toString());
			return resultingStyleSheet;

		} catch(IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
