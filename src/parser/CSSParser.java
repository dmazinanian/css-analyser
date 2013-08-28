package parser;

import java.io.InputStream;
import java.net.URL;

import CSSModel.StyleSheet;

import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.helpers.ParserFactory;


public class CSSParser {

	private CSSDocumentHandler docHandler;

	private Parser parser;

	private String path;

	public CSSParser(String path) {

		System.setProperty("org.w3c.css.sac.parser", "org.w3c.flute.parser.Parser");

		this.path = path;
	}

	public StyleSheet parseAndCreateStyleSheetObject() {

		StyleSheet styleSheet = new StyleSheet(path);

		try {

			InputSource source = new InputSource();
			URL uri = new URL("file", null, -1, path);
			InputStream stream = uri.openStream();

			source.setByteStream(stream);
			source.setURI(uri.toString());
			ParserFactory factory = new ParserFactory();
			
			parser = factory.makeParser();

			docHandler = new CSSDocumentHandler(styleSheet);

			parser.setDocumentHandler(docHandler);

			parser.parseStyleSheet(source);
			
			stream.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return styleSheet;
	}
}
