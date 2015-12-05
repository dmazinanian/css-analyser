package ca.concordia.cssanalyser.parser.less;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.LessSource.FileSource;

public class ModifiedLessFileSource extends FileSource {

	public ModifiedLessFileSource(File inputFile) {
		super(inputFile);
	}

	@Override
	public String getContent() throws FileNotFound, CannotReadFile {
		try {
			Reader input;
			input = new FileReader(getInputFile());
			try {
				String content = IOUtils.toString(input); // .replace("\r\n", "\n");
				setLastModified(getInputFile().lastModified());
				return content;
			} finally {
				input.close();
			}
		} catch (FileNotFoundException ex) {
			throw new FileNotFound();
		} catch (IOException ex) {
			throw new CannotReadFile();
		}
	}

}
