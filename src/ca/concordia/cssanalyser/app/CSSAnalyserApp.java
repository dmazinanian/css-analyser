package ca.concordia.cssanalyser.app;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.concordia.cssanalyser.analyser.CSSAnalyser;
import ca.concordia.cssanalyser.crawler.Crawler;
import ca.concordia.cssanalyser.io.IOHelper;




public class CSSAnalyserApp {

	private enum Mode {
		FOLDER, CRAWL, NODOM, DIFF
	}

	public static void main(String[] args) throws IOException {
		
		String url = "";
		int minsup = 2;
		String outputFolder = "./";
		String inputFolder = "";
		Mode mode = Mode.CRAWL;
		String urlFile = "";
		String css1Path = "", css2Path= "";
		String inputFoldersFile = "";
		
		if (args.length == 0) {
			System.out.println("No input file or URL provided.");
			return;
		} else {
			for (String s : args) {
				if (s.startsWith("--")) {
					// Parameters
					String parameter = s.substring(2, s.indexOf(":")).toLowerCase();
					String value = s.substring(s.indexOf(":") + 1);
					switch(parameter) {
					case "url":
						url = value;
						break;
					case "outfolder":
						outputFolder = value.replace("\\", "/");
						if (!outputFolder.endsWith("/"))
							outputFolder = outputFolder + "/";
						break;
					case "minsup":
						minsup = Integer.valueOf(value);
						break;
					case "mode":
						mode = Mode.valueOf(value.toUpperCase());
						break;
					case "infolder":
						inputFolder = value.replace("\\", "/");
						if (!inputFolder.endsWith("/"))
							inputFolder = inputFolder + "/";
						break;
					case "foldersfile":
						inputFoldersFile = value;
						break;
					case "urlfile":
						urlFile = value;
						break;
					case "css1":
						css1Path = value;
						break;
					case "css2":
						css2Path = value;
						break;
					}
				}
			}
		}
		
		switch (mode) {
		case CRAWL:
			if ("".equals(outputFolder)) {
				System.out.println("Please provide an output folder with --outfolder:out/folder.");
				return;
			} else if ("".equals(url) && "".equals(urlFile)) {
				System.out.println("Please provide a url using --url or the file containing urls using --urlfile");
				return;
			}
			
			List<String> urls = new ArrayList<>();
			
			if (!"".equals(urlFile)) {
				String file = IOHelper.readFileToString(urlFile);
				String[] lines = file.split("\n|\r|\r\n");
				for (String line : lines) {
					if (!"".equals(line.trim()) && !line.startsWith("--")) {
						if (!line.startsWith("http://")) {
							line = "http://" + line;
						}
						urls.add(line);
					}
				}
			} else {
				urls.add(url);
			}
			
			for (String currentUrl : urls) {
			
				String outputFolderPath = outputFolder + currentUrl.replaceFirst("http[s]?://", "").replaceFirst("file://", "").replace("/", "_").replace(":", "_") + "/";
				// Make sure to configure ca.concordia.cssanalyser.crawler in Crawler class
				Crawler crawler = new Crawler(currentUrl, outputFolderPath);
				crawler.start();

				//System.out.println(System.getProperty("user.dir"));

				// Get all ca.concordia.cssanalyser.dom states in outputFolder/crawljax/doms		
				List<File> allStatesFiles = IOHelper.searchForFiles(outputFolderPath + "crawljax/doms", "html");	
				for (File domStateHtml : allStatesFiles) {

					String stateName = domStateHtml.getName();
					// Remove .html
					String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(minsup);

				}
				
			}
			
			break;
		case FOLDER:

			List<String> folders = new ArrayList<>();
			
			if (!"".equals(inputFolder))
				folders.add(inputFolder);
			else if (!"".equals(inputFoldersFile)) {
				String file = IOHelper.readFileToString(inputFoldersFile);
				String[] lines = file.split("\n|\r|\r\n");
				for (String line : lines) {
					if (!"".equals(line.trim())) {
						line = line.replace("\\",  "/");
						if (!line.endsWith("/"))
							line = line + "/";
						folders.add(line);
					}
				}
			} else {
				System.out.println("Please provide an input folder with --infolder:in/folder or set of folders using --foldersfile.");
				return;
			}
			
			for (String folder : folders) {
			
				List<File> allStatesFiles = IOHelper.searchForFiles(folder + "crawljax/doms", "html");	
				for (File domStateHtml : allStatesFiles) {

					String stateName = domStateHtml.getName();
					// Remove .html
					String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), folder + "css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(minsup);

				}
			}
			
			break;
		case NODOM:
			if ("".equals(inputFolder)) {
				System.out.println("Please provide an input folder with --infolder:in/folder.");
				return;
			}
			
			CSSAnalyser cssAnalyser = new CSSAnalyser(inputFolder);
			cssAnalyser.analyse(minsup);

			break;
		case DIFF:
//			CSSParser parser = new CSSParser();
//			try {
//				StyleSheet css1 =  parser.parseExternalCSS(css1Path);
//				StyleSheet css2 =  parser.parseExternalCSS(css2Path);
//				DifferenceList dl = Diff.diff(css1, css2);
//				System.out.println(dl);
//			} catch (Exception ex) {
//				System.out.println(ex);
//			}
			break;
		}			
		
	}
	
}
