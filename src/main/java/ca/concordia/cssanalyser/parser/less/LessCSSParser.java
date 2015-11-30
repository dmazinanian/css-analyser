package ca.concordia.cssanalyser.parser.less;

import java.io.File;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.ParseException;

public class LessCSSParser implements CSSParser {

	@Override
	public StyleSheet parseCSSString(String css) throws ParseException {
		
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = getLessStyleSheet(new LessSource.StringSource(css));
		
		try {
			
			LessStyleSheetAdapter adapter = new LessStyleSheetAdapter(lessStyleSheet);
			return adapter.getAdaptedStyleSheet();
			
		} catch (RuntimeException ex) {
			throw new ParseException(ex);
		}	
	}

	@Override
	public StyleSheet parseExternalCSS(String path) throws ParseException {
		
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet = getLessStyleSheet(new LessSource.FileSource(new File(path)));
		
		try {
			
			LessStyleSheetAdapter adapter = new LessStyleSheetAdapter(lessStyleSheet);
			return adapter.getAdaptedStyleSheet();
			
		} catch (RuntimeException ex) {
			throw new ParseException(ex);
		}	

	}

	public static com.github.sommeri.less4j.core.ast.StyleSheet getLessStyleSheet(LessSource source) throws ParseException {
		
		ANTLRParser parser = new ANTLRParser();
		
		ANTLRParser.ParseResult result;
		ProblemsHandler problemsHandler = new ProblemsHandler();
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet;
		ASTBuilder astBuilder = new ASTBuilder(problemsHandler);
		try {
			
			result = parser.parseStyleSheet(source.getContent(), source);

			lessStyleSheet = astBuilder.parse(result.getTree());			

		} catch (FileNotFound | CannotReadFile ex) {
			throw new ParseException(ex);
		}
		return lessStyleSheet;
	}
	
	public static com.github.sommeri.less4j.core.ast.StyleSheet getLessParserFromStyleSheet(StyleSheet styleSheet) throws ParseException {

		if (styleSheet.getFilePath() == null) {
			return getLessStyleSheet(new LessSource.StringSource(styleSheet.toString()));
		} else {
			return getLessStyleSheet(new LessSource.FileSource(new File(styleSheet.getFilePath())));
		}
		
	}

}
