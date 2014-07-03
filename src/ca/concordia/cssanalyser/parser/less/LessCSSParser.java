package ca.concordia.cssanalyser.parser.less;

import java.io.File;

import org.apache.commons.lang.NotImplementedException;

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
	public StyleSheet parseCSSString(String css) {
		throw new NotImplementedException();
	}

	@Override
	public StyleSheet parseExternalCSS(String path) throws ParseException {
		LessSource source = new LessSource.FileSource(new File(path));
		ANTLRParser parser = new ANTLRParser();
		
		ANTLRParser.ParseResult result;
		ProblemsHandler problemsHandler = new ProblemsHandler();
		com.github.sommeri.less4j.core.ast.StyleSheet lessStyleSheet;
		ASTBuilder astBuilder = new ASTBuilder(problemsHandler);
		try {
			
			result = parser.parseStyleSheet(source.getContent(), source);
			lessStyleSheet = astBuilder.parse(result.getTree());
			
			LessStyleSheetAdapter adapter = new LessStyleSheetAdapter();
			return adapter.adapt(lessStyleSheet);
			

		} catch (FileNotFound | CannotReadFile ex) {
			throw new ParseException(ex);
		}

	}

}
