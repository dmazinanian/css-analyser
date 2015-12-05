package ca.concordia.cssanalyser.preprocessors.util.less;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessPrinter;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;

public class ImportInliner {
	
	private static final Logger LOGGER = FileLogger.getLogger(ImportInliner.class);
	
	public static void inlineImportsAll(String inputPath, boolean onlyReplaceLess) {
		List<File> files = IOHelper.searchForFiles(inputPath, ".less");
		inlineImportsAll(files, onlyReplaceLess);
	}

	public static void inlineImportsAll(List<File> filesList, boolean onlyImportLessFiles) {
		for (File lessFile : filesList) {
			LOGGER.info("Inlining imports in {}", lessFile.getAbsolutePath());
			replaceImports(lessFile.getAbsolutePath(), onlyImportLessFiles);
		}
	}
	
	public static void replaceImports(String lessFilePath, boolean onlyImportLessFiles) {
		try {
			String replacedImports = replaceImports(new File(lessFilePath), onlyImportLessFiles);
			if (!"".equals(replacedImports))
				IOHelper.writeStringToFile(replacedImports, getOutputFilePath(lessFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static String replaceImports(File lessFile, boolean onlyImportLessFiles) throws IOException {
		try {
			StyleSheet lessStyleSheet = LessCSSParser.getLessStyleSheet(new ModifiedLessFileSource(lessFile));
			List<Import> allImports = getAllImports(lessStyleSheet);
			String toReturn = IOHelper.readFileToString(lessFile.getAbsolutePath());
			for(Import importNode : allImports) {
				try {

					String url = getURLFromImportStatement(importNode);	

					if (!"".equals(url)) {
						if (url.startsWith("\"") && url.endsWith("\"")) {
							url = url.substring(1, url.length() - 1);
						}
						if (!onlyImportLessFiles && url.endsWith(".css"))
							continue;
						if (url.contains("@{")) {
							LOGGER.warn("In {} (line {}) URL expression has to be evaluated because it needs string interpolation",
											lessFile.getAbsolutePath(),
											importNode.getSourceLine());
						} else {
							// Inline!
							File importedFile = new File(url);
							if (!importedFile.isAbsolute()) {
								importedFile = new File(url);
								url = lessFile.getParentFile().getAbsolutePath() + File.separator + url;
							}
							
							if (!importedFile.exists()) {
								if (url.toLowerCase().lastIndexOf(".less") != url.length() - 5) {
									importedFile = new File(url + ".less");
								}
								if (!onlyImportLessFiles && !importedFile.exists()) {
									if (url.toLowerCase().lastIndexOf(".css") != url.length() - 4) {
										importedFile = new File(url + ".css");
									}
								}
							}

							if (importedFile.exists()) {
								String importedFileText = replaceImports(importedFile, onlyImportLessFiles);
								try {
									StyleSheet replacedImportsImportedStyleSheet = LessCSSParser.getLessStyleSheet(new LessSource.StringSource(importedFileText));
									lessStyleSheet.addMemberAfter(replacedImportsImportedStyleSheet, importNode);
								} catch (RuntimeException rte) {
									LOGGER.warn(lessFile.getAbsolutePath());
									rte.printStackTrace();
								}
								lessStyleSheet.removeMember(importNode);
								toReturn = (new LessPrinter()).getString(lessStyleSheet);
							} else {
								LOGGER.warn("In {} (line {}), imported file {} does not exist",
												lessFile.getAbsolutePath(),
												importNode.getSourceLine(),
												url);
							}
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return toReturn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getURLFromImportStatement(Import importNode) throws Exception {
		Expression urlExpression = importNode.getUrlExpression();
		String url = "";
		if (urlExpression instanceof CssString) {
			CssString cssString = (CssString) urlExpression;
			url = cssString.getValue();
		} else if (urlExpression instanceof FunctionExpression) {
				FunctionExpression functionExpression = (FunctionExpression) urlExpression;
				if ("url".equals(functionExpression.getName())) {
					url = (new LessPrinter()).getStringForNode(functionExpression.getParameter());
					if (url.startsWith("'")) // Remove quotes
						url = url.substring(1, url.length() - 1);
				} else {
					throw new Exception(
							String.format(
								"Cannot handle function %s when importing at line %s: ",
								functionExpression.getSourceLine(),
								functionExpression.getName()
							));
			}
		} else {
			throw new Exception(
					String.format("In line %s, the URL expression is of type %s",
							urlExpression.getSourceLine(),
							urlExpression.getClass().getName()));
		}
		return url;
	}
	
	public static List<Import> getAllImports(ASTCssNode child) {
		List<Import> toReturn = new ArrayList<>();
		if (child instanceof Import)
			toReturn.add((Import)child);
		else {
			for (ASTCssNode c : child.getChilds())
				toReturn.addAll(getAllImports(c));
		}
		return toReturn;
	}
	
	private static String getOutputFilePath(String inputFilePath) {
		File inputFile = new File(inputFilePath);
		int dotPosition = inputFile.getName().lastIndexOf(".");
		String outputFileName = inputFile.getName();
		String extension = "";
		if (dotPosition >= 0) {
			extension = inputFile.getName().substring(dotPosition + 1);
			outputFileName = outputFileName.replace(extension, "");
		}
		outputFileName += "importsInlined." + extension;
		return inputFile.getParentFile().getAbsolutePath() + File.separator + outputFileName;
	}
	
}
