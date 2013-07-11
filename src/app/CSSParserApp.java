package app;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import duplication.Duplication;
import duplication.DuplicationFinder;
import duplication.DuplicationsList;
import duplication.ItemSetList;

import CSSModel.StyleSheet;

import parser.CSSParser;

public class CSSParserApp {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		//System.out.println(System.getProperty("user.dir"));
		
		String folderPath = "css/other/outlook.com";
		analysefiles(folderPath);
		
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
			System.out.println("\nFinding identical selectors in " + filePath);
			DuplicationsList identicalSelectorsDuplication = duplicationFinder.findIdenticalSelectors();
			for (Duplication duplication : identicalSelectorsDuplication)
				writeFile(fw, duplication.toString());
			closeFile(fw);

			fw = openFile(folderName + "/identical_declarations.txt");
			System.out.println("\nFinding identical declarations in " + filePath);
			for (Duplication duplication : duplicationFinder.findIdenticalDeclarations()) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);

			fw = openFile(folderName + "/identical_values.txt");
			System.out.println("\nFinding identical values in " + filePath);
			for (Duplication duplication : duplicationFinder.findIdenticalValues()) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			
			fw = openFile(folderName + "/identical_effects.txt");
			System.out.println("\nFinding identical effects (identical selector and declarations together) in " + filePath);
			for (Duplication duplication : duplicationFinder.findIdenticalEffects(identicalSelectorsDuplication)) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			
			fw = openFile(folderName + "/overriden_values.txt");
			System.out.println("\nFinding identical values in " + filePath);
			for (Duplication duplication : duplicationFinder.findOverridenValues(identicalSelectorsDuplication)) {
				writeFile(fw, duplication.toString());
			}
			closeFile(fw);
			
			final int MIN_SUPPORT_COUNT = 4;
			
			fw = openFile(folderName + "/apriori.txt");
			System.out.println("\nFinding grouped identical declarations in " + filePath);
			for (ItemSetList itemsetList : duplicationFinder.apriori(MIN_SUPPORT_COUNT)) {
				writeFile(fw, itemsetList.toString());
			}
			closeFile(fw);
			
			
			System.out.println("\nDone\n\n");
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
