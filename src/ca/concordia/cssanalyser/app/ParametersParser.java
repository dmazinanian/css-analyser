package ca.concordia.cssanalyser.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.cssanalyser.io.IOHelper;

/**
 * Parses input arguments.
 * Methods in this class return default values if necessary.
 * @author Davood Mazinanian
 *
 */
class ParametersParser {
	
	private static Logger LOGGER = FileLogger.getLogger(ParametersParser.class);
	
	private Map<String, String> params;

	public ParametersParser(String[] args) {
		parseParameters(args);
	}
	
	private void parseParameters(String[] args) {
		
		params = new HashMap<>();
		
		if (args.length == 0) {
			return;
		} else {
			for (String s : args) {
				if (s.startsWith("--")) {
					// Parameters
					String parameter = s.substring(2, s.indexOf(":")).toLowerCase();
					String value = s.substring(s.indexOf(":") + 1);
					switch(parameter) {
					case "url":
					case "minsup":
					case "mode":
					case "to":
						params.put(parameter, value);
						break;
					case "outfolder":
					case "infolder":
					case "foldersfile":
					case "urlsfile":
					case "css1":
					case "css2":
					case "file":
						params.put(parameter, formatPath(value));
						break;
					}
				}
			}
		}
	}

	/**
	 * Replace backslashes with forward slashes in a path, add an ending
	 * forward slash to the path if necessary.
	 * @param path
	 * @return
	 */
	private static String formatPath(String path) {
		String toReturn;
		toReturn = path.replace("\\", "/");
		if (!toReturn.endsWith("/"))
			toReturn = toReturn + "/";
		return toReturn;
	}
	
	public ProgramMode getProgramMode() {
		try {
			return ProgramMode.valueOf(params.get("mode").toUpperCase());
		} 
		catch (NullPointerException npe) {
			LOGGER.info("No mode parameters is provided. CRAWL mode is selected by default.");
			params.put("mode", "CRAWL");
			return ProgramMode.CRAWL;
		}
		catch (IllegalArgumentException iae) {
			LOGGER.error("Invalid mode parameter is provided. please see documentation for the allowed parameters.");
			return null;
		}
	}
	
	public String getOutputFolderPath() {
		if (params.containsKey("outfolder"))
			return params.get("outfolder");
		else {
			String currentDirectory = formatPath(System.getProperty("user.dir"));
			LOGGER.warn(String.format("No output folder was provided, %s is selected by default.", currentDirectory));
			params.put("outfolder", currentDirectory);
			return currentDirectory;
		}
	}
	
	public String getUrl() {
		return params.get("url");
	}
	
	/**
	 * Returns minimum support count for FP-Growth.
	 * The minimum support count is 2.
	 * @return
	 */
	public int getFPGrowthMinsup() {
		String minsup = params.get("minsup");
		try {
			if (minsup != null)
				return Integer.valueOf(minsup);
			else 
				LOGGER.warn("Nothing was given for minsup, 2 is selected by default.");
		} catch (NumberFormatException nfe) {
			LOGGER.warn("Invalid number was given for minsup, 2 is selected by default.");
		}	
		params.put("minsup", "2");
		return 2;
	}
	
	public String getInputFolderPath() {
		return params.get("infolder");
	}
	
	/**
	 * Returns the file path, provided by user, which includes
	 * a list of folder path's in which program analyze crawled data.
	 * @return
	 */
	public String getListOfFoldersPathsToBeAnayzedFile() {
		return params.get("foldersfile");
	}
	
	public String getListOfURLsToAnalyzeFilePath() {
		return params.get("urlsfile");
	}
	
	public String getCSS1FilePath() {
		return params.get("css1");
	}
	
	public String getCSS2FilePath() {
		return params.get("css2");
	}

	/**
	 * Returns a list of paths to the URLs,
	 * which are provided in the url list file using --urlsfile:path/to/file
	 * @return
	 */
	public Collection<? extends String> getURLs() {
		
		List<String> urls = new ArrayList<>();
		
		try {
			
			String file = IOHelper.readFileToString(getListOfURLsToAnalyzeFilePath());
			String[] lines = file.split("\n|\r|\r\n");
			for (String line : lines) {
				if (!"".equals(line.trim()) && !line.startsWith("--")) {
					if (!"".equals(line.trim()) && !line.startsWith("http://")) {
						line = "http://" + line;
					}
					urls.add(line);
				}
			}
			
		} catch (IOException ioe) {
			LOGGER.error("Error reading file " + getListOfURLsToAnalyzeFilePath());
		}
		
		return urls;
	}

	/**
	 * Get list of folders from the given file using --fildersfile:path/to/file
	 * @return
	 */
	public Collection<? extends String> getFoldersListToBeAnalyzed() {
		
		List<String> folderPaths = new ArrayList<>();
		
		try {
			String folderPathsFile = getListOfFoldersPathsToBeAnayzedFile();
			String file = IOHelper.readFileToString(folderPathsFile);
			String[] lines = file.split("\n|\r|\r\n");
			String folderPathsFileParentPath = new File(folderPathsFile).getParentFile().getCanonicalPath();
			for (String line : lines) {
				if (!"".equals(line.trim())) {
					if (line.startsWith("--"))
						continue;
					String path = formatPath(line);
					if (new File(folderPathsFileParentPath + "/" + path).exists())
						folderPaths.add(folderPathsFileParentPath + "/" + path);
					else if (new File(path).exists())
						folderPaths.add(path);
					else
						LOGGER.warn("\"" + path + "\" is not a valid relative or abolute path.");
						
				}
			}
		} catch (IOException ioe) {
			LOGGER.error("IO Exception in reading file " + getListOfFoldersPathsToBeAnayzedFile());
		}
		//System.out.println(folderPaths.size());
		return folderPaths;
	}

	public String getFilePath() {
		return params.get("file");
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String key : params.keySet()) {
			builder.append("--" + key + ":\"" + params.get(key) + "\" ");
		}
		return builder.toString().trim();
	}
}
