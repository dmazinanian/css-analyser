package app;

import java.io.File;
import java.io.FileWriter;
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
		String[] filePaths = new String[] { 
				"css/netvibes/netvibes.css",
				"css/netvibes/1.css",
				"css/netvibes/2.css",
		};
		analysefiles(filePaths);
		System.out.println("Done");
	}

	private static void analysefiles(String[] files) throws IOException {
		for (String filePath : files) {
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

			/*fw = openFile(folderName + "/declarations.intersections.txt");
			int max = 0;
			List<Duplication> maxd = new ArrayList<>();
			for (Duplication duplication: duplicationFinder.findIdenticalPropertyAndValuesIntersection()) {
				if (duplication.getSize() > max) {
					maxd.clear();
					maxd.add(duplication);
					max = duplication.getSize();
				}
				else if (duplication.getSize() == max) {
					maxd.add(duplication);
				}
			}
			closeFile(fw);
			System.out.println(max);
			for (Duplication d : maxd)
				System.out.println(d);*/

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
