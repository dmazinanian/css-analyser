package ca.concordia.cssanalyser.parser.flute;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.helpers.ParserFactory;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.ParseException;


public class FluteCSSParser implements CSSParser {

	private CSSDocumentHandler docHandler;

	private Parser parser;
	
	private Logger LOGGER = FileLogger.getLogger(FluteCSSParser.class);
	
	static {
		System.setProperty("org.w3c.css.sac.parser", "org.w3c.flute.parser.Parser");
	}

	public FluteCSSParser() {

	}
	
	public StyleSheet parseCSSString(String css) throws ParseException {
		try {
			return parseStreamCSS(new ByteArrayInputStream(css.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParseException(e);
		}
	}
	

	public StyleSheet parseExternalCSS(String path) throws ParseException {
		try {
			LOGGER.info("Parsing " + path);
			URL uri = new URL("file", null, -1, path);
			StyleSheet styleSheet = parseStreamCSS(uri.openStream());
			styleSheet.setPath(path);
			LOGGER.info("Parsed " + path);
			return styleSheet;
		}
		catch (Exception e) {
			LOGGER.warn(e.toString());
			throw new ParseException(e);
		}
	}
	
	private StyleSheet parseStreamCSS(InputStream inputStream) throws Exception {
				
		StyleSheet styleSheet = new StyleSheet();

		InputSource source = new InputSource();

		source.setByteStream(inputStream);
		//source.setURI(uri.toString());
		ParserFactory factory = new ParserFactory();

		parser = factory.makeParser();

		docHandler = new CSSDocumentHandler(styleSheet);

		parser.setDocumentHandler(docHandler);

		parser.parseStyleSheet(source);
			
		inputStream.close();

		return styleSheet;
	}
}
