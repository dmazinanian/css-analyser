package ca.concordia.cssanalyser.preprocessors.constructsinfo.less;

import java.io.File;

import org.apache.commons.lang.NotImplementedException;

import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.StyleSheet;

import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.parser.less.LessCSSParser;
import ca.concordia.cssanalyser.parser.less.ModifiedLessFileSource;
import ca.concordia.cssanalyser.preprocessors.util.less.ImportInliner;

public class LessImport extends LessConstruct {
	
	private final Import originalImportNode;
	private final String url;
	private final String importedFileAbsolutePath;
	private final int line;
	private final int column;
	
	public LessImport(Import importNode, StyleSheet styleSheet) throws Exception {
		super(styleSheet);
		this.originalImportNode = importNode;
		this.url = ImportInliner.getURLFromImportStatement(originalImportNode);
		this.line = importNode.getSourceLine();
		this.column = importNode.getSourceColumn();
		if (!this.url.startsWith("http")) {
			File importedFile = new File(this.url);
			if (importedFile.isAbsolute()) {
				if (importedFile.exists()) {
					this.importedFileAbsolutePath = this.url;
				} else {
					this.importedFileAbsolutePath = "";
				}
			} else {
				String absolutePath = (new File(styleSheet.getSource().toString())).getParentFile().getAbsolutePath()
						+ File.separator 
						+ this.url;
				importedFile = new File(absolutePath);
				if (importedFile.exists() && !importedFile.isDirectory()) {
					this.importedFileAbsolutePath = absolutePath;
				} else {
					if (!this.url.endsWith(".less")) {
						absolutePath += ".less";
						if ((new File(absolutePath)).exists()) {
							this.importedFileAbsolutePath = absolutePath;
						} else {
							this.importedFileAbsolutePath = "";
						}
					} else {
						this.importedFileAbsolutePath = "";
					}
				}	
			}
		} else {
			this.importedFileAbsolutePath = "";
		}
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}

	public StyleSheet getImportedStyleSheet() throws ParseException {
		if (this.url.startsWith("http:") || this.url.startsWith("https:")) {
			throw new NotImplementedException("We don't support remote URLs for the moment");
		}
		if (!"".equals(this.getImportedFileAbsolutePath())) {
			return LessCSSParser.getLessStyleSheet(new ModifiedLessFileSource(new File(this.getImportedFileAbsolutePath())));
		} else {
			throw new ParseException("Target URL not found");
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + line;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LessImport other = (LessImport) obj;
		if (column != other.column)
			return false;
		if (line != other.line)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public boolean couldFindURL() {
		return (!"".equals(this.importedFileAbsolutePath)) && (new File(importedFileAbsolutePath).exists());
	}

	public String getImportedFileAbsolutePath() {
		return importedFileAbsolutePath;
	}
	
}
