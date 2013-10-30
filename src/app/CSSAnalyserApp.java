package app;

import io.IOHelper;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crawljax.Crawler;


import analyser.CSSAnalyser;

public class CSSAnalyserApp {

	private enum Mode {
		FOLDER, CRAWL, NODOM
	}

	public static void main(String[] args) throws IOException {
		
		String url = "";
		int minsup = 2;
		String outputFolder = ".";
		String inputFolder = "";
		Mode mode = Mode.CRAWL;
		String urlFile = "";
		
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
						outputFolder = value;
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
						inputFolder = value;
						break;
					case "urlfile":
						urlFile = value;
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
			
				String outputFolderPath = outputFolder + currentUrl.replace("http://", "").replace("/", "_");
				// Make sure to configure crawljax in Crawler class
				Crawler crawler = new Crawler(currentUrl, outputFolderPath);
				crawler.start();

				//System.out.println(System.getProperty("user.dir"));

				// Get all dom states in outputFolder/crawljax/doms		
				List<File> allStatesFiles = IOHelper.searchForFiles(outputFolderPath + "/crawljax/doms", "html");	
				for (File domStateHtml : allStatesFiles) {

					String stateName = domStateHtml.getName();
					// Remove .html
					String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);

					CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), outputFolderPath + "/css/" + correspondingCSSFolderName);
					cssAnalyser.analyse(minsup);

				}
				
			}
			
			break;
		case FOLDER:
			if ("".equals(inputFolder)) {
				System.out.println("Please provide an input folder with --infolder:in/folder.");
				return;
			}
			
			List<File> allStatesFiles = IOHelper.searchForFiles(inputFolder, "html");	
			for (File domStateHtml : allStatesFiles) {
				
				String stateName = domStateHtml.getName();
				// Remove .html
				String correspondingCSSFolderName = stateName.substring(0, stateName.length() - 5);
				
				CSSAnalyser cssAnalyser = new CSSAnalyser(domStateHtml.getAbsolutePath(), inputFolder + "/css/" + correspondingCSSFolderName);
				cssAnalyser.analyse(minsup);
				
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
		}
		
			
		
	}
	
}
