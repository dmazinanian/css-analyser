package ca.concordia.cssanalyser.migration.topreprocessors.sass;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.StringContext;

import java.io.File;
import java.net.URI;

public class SassHelper {

    public static StyleSheet compileSassFile(String inputFilePath) throws Exception {

        File inputFile = new File(inputFilePath);
        URI inputFileURL = inputFile.toURI();
        URI outputFileURL = inputFile.getParentFile().toPath().resolve(inputFile.getName() + ".css").toUri();
        String cssFileContents = IOHelper.readFileToString(inputFilePath);

        Compiler compiler = new Compiler();
        Options options = new Options();
        options.setIsIndentedSyntaxSrc(inputFilePath.endsWith(".sass"));

        StringContext context = new StringContext(cssFileContents, inputFileURL, outputFileURL, options);
        Output output = compiler.compile(context);
        String cssString = output.getCss();
        return CSSParserFactory.getCSSParser(CSSParserFactory.CSSParserType.LESS).parseCSSString(cssString);

    }

}
