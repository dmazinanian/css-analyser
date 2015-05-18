package ca.concordia.cssanalyser.migration.topreprocessors.less;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.LessStyleSheetAdapter;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.GeneralProblem;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

/**
 * 
 * @author Davood Mazinanian
 *
 */
public class LessHelper {
	
	/**
	 * Compiles a given less style sheet to a {@link StyleSheet} object
	 * @param lessStyleSheetToCompile
	 * @return
	 * @throws Less4jException
	 */
	public static StyleSheet compileLESSStyleSheet(com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheetToCompile) throws Less4jException {
		
		/*
		 * This is weird, however, it seems that when we make changes to
		 * the AST of the Less style sheet, it cannot compile because, for instance,
		 * it cannot find Mixin declarations. 
		 * For the moment, the naive approach is to parse it again.
		 */
		LessPrinter lessPrinter = new LessPrinter();	
		Configuration options = new Configuration();
		String lessCSSFileAsString = lessPrinter.getString(lessStyleSheetToCompile);
		LessSource source = new StringSource(lessCSSFileAsString);
		
		/*
		 * Got from Less4j code
		 */
		ANTLRParser.ParseResult result = null;
		try {
			result = new ANTLRParser().parseStyleSheet(source.getContent(), source);
		} catch (FileNotFound ex) {
			throw new Less4jException(new GeneralProblem("The file " + source + " does not exists."), new CompilationResult(null));
		} catch (CannotReadFile ex) {
			throw new Less4jException(new GeneralProblem("Cannot read the file " + source + "."), new CompilationResult(null));
		}

		if (result.hasErrors()) {
			CompilationResult compilationResult = new CompilationResult("Errors during parsing phase, partial result is not available.");
			throw new Less4jException(result.getErrors(), compilationResult);
		}

		ProblemsHandler problemsHandler = new ProblemsHandler();
		ASTBuilder astBuilder = new ASTBuilder(problemsHandler);
		LessToCssCompiler compiler = new LessToCssCompiler(problemsHandler, options);
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
		ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source, options);
		
		// Adapt to our style sheet
		LessStyleSheetAdapter adapter = new LessStyleSheetAdapter(cssStyleSheet);
		StyleSheet resultingStyleSheet = adapter.getAdaptedStyleSheet();
		
		return resultingStyleSheet;
	}

	public static ASTCssNode getLessNodeFromLessString(String nodeString) throws ParseException {
		String fakeSelectorString = ".fake { " + nodeString + "}" ;
		com.github.sommeri.less4j.core.ast.StyleSheet root = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(fakeSelectorString));
		ASTCssNode resultingNode = root.getChilds().get(0).getChilds().get(1).getChilds().get(1); // :)
		return resultingNode;
	}
	
	
}
