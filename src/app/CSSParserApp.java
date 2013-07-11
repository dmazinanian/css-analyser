package app;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import duplication.Duplication;
import duplication.DuplicationFinder;
import duplication.DuplicationsList;

import CSSModel.StyleSheet;

import parser.CSSParser;

public class CSSParserApp {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// System.out.println(System.getProperty("user.dir"));
		
		//String folderPath = "css/other";
		//analysefiles(folderPath);
		
		CSSParser parser = new CSSParser("css/other/facebook-2.css");

		StyleSheet styleSheet = parser.parseAndCreateStyleSheetObject();

		DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);
		duplicationFinder.apriori();
		
	}

	private static void analysefiles(String folderPath) throws IOException {
		File f = new File(folderPath);
		File[] files = f.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".css");
		    }
		});

		for (File file : files) {
			
			String filePath = file.getAbsolutePath();
			
			System.out.println("Now doing " + filePath);
			CSSParser parser = new CSSParser(filePath);

			StyleSheet styleSheet = parser.parseAndCreateStyleSheetObject();

			DuplicationFinder duplicationFinder = new DuplicationFinder(styleSheet);

			String folderName = filePath + ".analyse";
			
			createFolder(folderName);
				
			FileWriter fw = openFile(folderName + "/identical_selectors.txt");
			DuplicationsList identicalSelectorsDuplication = duplicationFinder.findIdenticalSelectors();
			for (Duplication duplication : identicalSelectorsDuplication)
				writeFile(fw, duplication.toString());
			closeFile(fw);

			fw = openFile(folderName + "/identical_declarations.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalDeclarations()) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);

			fw = openFile(folderName + "/identical_values.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalValues()) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			
			fw = openFile(folderName + "/identical_effects.txt");
			for (Duplication duplication : duplicationFinder.findIdenticalEffects(identicalSelectorsDuplication)) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			
			fw = openFile(folderName + "/overriden_values.txt");
			for (Duplication duplication : duplicationFinder.findOverridenValues(identicalSelectorsDuplication)) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			System.out.println("Done");
		}
	}

	private static void createFolder(String folderPath) {
		File folder = new File(folderPath);
		
		if (!folder.exists())
			folder.mkdir();
	}

	private static void writeFile(FileWriter fw, String line)
			throws IOException {
		fw.append(line + "\r\n");
	}

	private static FileWriter openFile(String path) throws IOException {
		FileWriter fw = new FileWriter(new File(path));
		return fw;
	}

	private static void closeFile(FileWriter fw) throws IOException {
		fw.flush();
		fw.close();
	}
}
