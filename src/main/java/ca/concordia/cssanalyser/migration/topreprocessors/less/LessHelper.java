package ca.concordia.cssanalyser.migration.topreprocessors.less;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorHelper;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.LessStyleSheetAdapter;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.FileSource;
import com.github.sommeri.less4j.LessSource.URLSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

import java.io.File;
import java.util.function.Function;

/**
 * 
 * @author Davood Mazinanian
 *
 */
public class LessHelper {

	/**
	 * Compiles a given less style sheet using Less4j to a {@link StyleSheet} object
	 * @param lessStyleSheetToCompile The less stylesheet object to compile.
	 * The method will first try to read from the source
	 * (using {@link com.github.sommeri.less4j.core.ast.StyleSheet#getSource()}
	 * of the style sheet being compiled.
	 * If not found, it tries to use the AST of the given file to compile;
	 * i.e., it will call compileLESSStyleSheet(lessStyleSheetToCompile, false).
	 * @return Pure CSS represented using the {@link StyleSheet} model
	 * @see #compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet, boolean)
	 * @throws Less4jException
	 */
	public static StyleSheet compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheetToCompile) throws Exception {
		return compileLESSStyleSheet(lessStyleSheetToCompile, false);
	}

	/**
	 * Compiles a given less style sheet using Less4j to a {@link StyleSheet} object
	 * @param lessStyleSheetToCompile The less stylesheet object to compile.
	 * @param forceReadingFromAST Use the AST of the given style sheet instead of trying to
	 *                            compile it using its source code.
	 * @return Pure CSS represented using the {@link StyleSheet} model
	 * @see #compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet, boolean)
	 * @throws Less4jException
	 */
	public static StyleSheet compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheetToCompile,
												   boolean forceReadingFromAST) throws Exception {

		Function<String, StyleSheet> compileFunction = filePath -> {
			try {
				return compileLessOnNonWindowsUsingLessJS(filePath);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		};
		return compileLESSStyleSheet(lessStyleSheetToCompile, forceReadingFromAST, compileFunction);
	}

	/**
	 * Compiles a given less style sheet to a {@link StyleSheet} object
	 * @param lessStyleSheetToCompile The less stylesheet object to compile.
	 * @param forceReadingFromAST Force the method to read from the AST.
	 * otherwise, the method will first try to read from the source
	 * (using {@link com.github.sommeri.less4j.core.ast.StyleSheet#getSource()}
	 * of the style sheet being compiled.
	 * @param compilingFunction A method that accepts the path to the less file
	 *                          and returns a {@link StyleSheet} object.
	 *                          For convenience, one can use one of these:
	 *                          <ul>
	 *                          	<li>LessHelper::compileStyleSheetUsingLess4J</li>
	 * 								<li>LessHelper::compileStyleSheetOnWindowsUsingLessJS</li>
	 * 								<li>LessHelper::compileLessOnNonWindowsUsingLessJS</li>
	 * 							</ul>
	 * @return Pure CSS represented in our model
	 * @throws Less4jException
	 */
	public static StyleSheet compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheetToCompile,
												   boolean forceReadingFromAST,
												   Function<String, StyleSheet> compilingFunction) throws Exception {

		String filePath = "";

		if (!forceReadingFromAST) {
			LessSource source = lessStyleSheetToCompile.getSource();
			if (source instanceof FileSource) {
				FileSource fileSource = (FileSource) source;
				if (fileSource.getURI() != null) {
					filePath = fileSource.getURI().toString();
				} else {
					if (source instanceof ModifiedLessFileSource) {
						ModifiedLessFileSource modifiedLessFileSource = (ModifiedLessFileSource) source;
						filePath = modifiedLessFileSource.getInputFile().getAbsolutePath();
					}
				}
			} else if (source instanceof URLSource) {
				URLSource urlSource = (URLSource) source;
				filePath = urlSource.getURI().toString();
			}
		}

		if (forceReadingFromAST || "".equals(filePath)) {
			File tempFile = File.createTempFile("lessToCompile", "less");
			LessPrinter lessPrinter = new LessPrinter();
			String lessCSSFileAsString = lessPrinter.getString(lessStyleSheetToCompile);
			IOHelper.writeStringToFile(lessCSSFileAsString, tempFile.getAbsolutePath());
			filePath = tempFile.getAbsolutePath();
		}

		StyleSheet resultingStyleSheet = compilingFunction.apply(filePath);

		resultingStyleSheet.setPath(filePath + ".css");

		return resultingStyleSheet;

	}

	/**
	 * Compiles the given less file using <code>less4j</code>
	 * and returns the compiled less file as a {@link StyleSheet} object.
	 * @param filePath Path to the less file to be compiled.
	 * @return A {@link StyleSheet} object resulting from compiling the less file.
	 * @throws ParseException
	 */
	public static StyleSheet compileStyleSheetUsingLess4J(String filePath) throws ParseException {
		/*
		 * This is weird, however, it seems that when we make changes to
		 * the AST of the Less style sheet, it cannot compile because, for instance,
		 * it cannot find Mixin declarations. 
		 * For the moment, the naive approach is to parse it again.
		 */
		Configuration options = new Configuration();
		LessSource source = new FileSource(new File(filePath));
		
		/*
		 * Got from Less4j code
		 */
		ANTLRParser.ParseResult result = null;
		try {
			result = new ANTLRParser().parseStyleSheet(source.getContent(), source);
		} catch (FileNotFound | CannotReadFile ex) {
			throw new ParseException(ex);
		}

		if (result.hasErrors()) {
			CompilationResult compilationResult = new CompilationResult("Errors during parsing phase, partial result is not available.");
			throw new ParseException(new Less4jException(result.getErrors(), compilationResult));
		}

		ProblemsHandler problemsHandler = new ProblemsHandler();
		ASTBuilder astBuilder = new ASTBuilder(problemsHandler);
		LessToCssCompiler compiler = new LessToCssCompiler(problemsHandler, options);
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = astBuilder.parseStyleSheet(result.getTree());
		ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source, options);
		
		// Adapt to our style sheet
		LessStyleSheetAdapter adapter = new LessStyleSheetAdapter(cssStyleSheet);
		StyleSheet resultingStyleSheet = adapter.getAdaptedStyleSheet();
		return resultingStyleSheet;
	}

	/**
	 * Calls <code>cmd.exe /c lessc {filePath}</code> on a windows-based machine
	 * and returns the compiled less file as a {@link StyleSheet} object.
	 * Requires the less.js compiler be installed and the PATH environment variable is set correctly.
	 * @param filePath Path to the less file to be compiled.
	 * @return A {@link StyleSheet} object resulting from compiling the less file.
	 * @throws ParseException
	 */
	public static StyleSheet compileStyleSheetOnWindowsUsingLessJS(String filePath) throws ParseException {
		String[] command = new String[] { "cmd.exe", "/c", "lessc", filePath };
		return PreprocessorHelper.runProcessToCompilePreprocessorCode(command);
	}

	/**
	 * Calls <code>sh -c lessc {filePath}</code> on a non-windows machine
	 * and returns the compiled less file as a {@link StyleSheet} object.
	 * Requires the less.js compiler be installed and the PATH environment variable is set correctly.
	 * @param filePath Path to the less file to be compiled.
	 * @return A {@link StyleSheet} object resulting from compiling the less file.
	 * @throws ParseException
	 */
	public static StyleSheet compileLessOnNonWindowsUsingLessJS(String filePath) throws ParseException {
		String[] command = new String[]{"sh", "-c", "lessc " + filePath};
		return PreprocessorHelper.runProcessToCompilePreprocessorCode(command);
	}

	public static ASTCssNode getLessNodeFromLessString(String nodeString) throws ParseException {
		String fakeSelectorString = ".fake { " + nodeString + "}" ;
		com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(fakeSelectorString));
		ASTCssNode resultingNode = root.getChilds().get(0).getChilds().get(1).getChilds().get(1); // :)
		return resultingNode;
	}
	
	
}
