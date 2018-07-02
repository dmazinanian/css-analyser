package ca.concordia.cssanalyser.migration.topreprocessors.sass;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.importer.Import;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

public class SassHelper {

    public static StyleSheet compileSassFile(String filePath) throws Exception {

        String cssFileContents = IOHelper.readFileToString(filePath);

        Compiler compiler = new Compiler();
        Options options = new Options();
        options.setImporters(Collections.singleton((path, previousImport) -> resolveImport(path, previousImport, filePath)));

        Output output = compiler.compileString(cssFileContents, options);
        String cssString = output.getCss();
        return CSSParserFactory.getCSSParser(CSSParserFactory.CSSParserType.LESS).parseCSSString(cssString);

    }

    /**
     * Implements a {@link io.bit3.jsass.importer.Importer}
     *
     * @param importPath
     * @param previousImport
     * @param rootSassFilePath
     * @return
     */
    private static Collection<Import> resolveImport(String importPath, Import previousImport, String rootSassFilePath) {
        Path previousImportPath;
        String previousImportPathString = previousImport.getAbsoluteUri().getPath();
        if ("stdin".equals(previousImportPathString)) {
            previousImportPath = Paths.get(rootSassFilePath).getParent();
        } else {
            previousImportPath = Paths.get(previousImportPathString).getParent();
        }

        try {
            Path path = previousImportPath.resolve(importPath);
            File resource = resolveSassFile(path);

            if (null == resource) {
                throw new FileNotFoundException(importPath);
            }

            final URI uri = new URI(resource.getAbsolutePath());
            final String source = IOHelper.readFileToString(resource.getPath());

            final Import scssImport = new Import(uri, uri, source);
            return Collections.singleton(scssImport);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File resolveSassFile(Path path) {
        final Path dir = path.getParent();
        final String basename = path.getFileName().toString();

        for (String prefix: new String[]{"_", ""}) {
            for (String suffix: new String[]{".scss", ".css", ""}) {
                Path target = dir.resolve(prefix + basename + suffix);
                File resource = new File(target.toString());
                if (resource.exists() && !resource.isDirectory()) {
                    return resource;
                }
            }
        }

        return null;
    }

}
