package ca.concordia.cssanalyser.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;

import ca.concordia.cssanalyser.io.IOHelper;

/**
 * Parses input arguments.
 * Methods in this class return default values if necessary.
 * @author Davood Mazinanian
 *
 */
class ParametersParser {
	
	private static Logger LOGGER = FileLogger.getLogger(ParametersParser.class);
	
	@Option(name="--mode", usage="Mode")
	private ProgramMode mode;
	
	@Option(name="--url", usage="Url for analysis")
    private String url;
	
	@Option(name="--min-sup", usage="Minimum support count for duplicated declarations")
	private int minsup;
	
	@Option(name="--max-declarations", usage="Maximum number of declarations extracted in each mixin")
	private int maxDeclarations = -1;
	
	@Option(name="--max-calls", usage="Maximum numbef of times a mixin is called")
	private int maxCalls = -1;
	
	@Option(name="--max-parameters", usage="Maximum number of the involved parameters")
	private int maxParameters = -1;

	@Option(name="--check-safety", usage="When enabled, will check for the safety of the applied migration opportunities")
	private boolean shouldCheckPresentationPreservation = false;

	@Option(name="--compare-calls", usage="When enabled, each opportunity should have the same (or more) calls than the real mixin. " +
			"Otherwise, the mixin and opportunities calls are not compared.")
	private boolean shouldCompareRealMixinsAndOpportunitiesCalls = false;

	@Option(name="--out-folder", usage="Folder for the output stuff")
	private String outFolder;

	@Option(name="--in-folder", usage="Input folder")
	private String inFolder;

	@Option(name="--folders-file", usage="File containing the paths to the folders")
	private String foldersFile;

	@Option(name="--urls-file", usage="File containing the list of URLs for analysis")
	private String urlsFile;
	
	@Option(name="--input-file", usage="File containing the list of URLs for analysis")
	private String inputFile;

	private final CmdLineParser parser;

	public ParametersParser(String[] args) {
		parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			LOGGER.error(e.getMessage());
			parser.printUsage(System.out);
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
		if (mode == null) {
			LOGGER.info("No mode parameters is provided. CRAWL mode is selected by default.");
			mode = ProgramMode.CRAWL;
		}
		return mode;
	}
	
	public String getOutputFolderPath() {
		if (outFolder == null) {
			outFolder = formatPath(System.getProperty("user.dir"));
			LOGGER.warn(String.format("No output folder was provided, %s is selected by default.", outFolder));
		}
		return outFolder;
	}
	
	public String getInputFolderPath() {
		return inFolder;
	}
	
	public String getUrl() {
		return url;
	}
	
	/**
	 * Returns minimum support count for FP-Growth.
	 * The minimum (and default) value is 2.
	 * @return
	 */
	public int getFPGrowthMinsup() {
		if (minsup < 2) {
			minsup = 2;
			LOGGER.warn("Invalid number (or nothing) was given for minimum support count (--min-sup), 2 is selected by default.");
		}	
		return minsup;
	}

	/**
	 * Returns the file path, provided by user, which includes
	 * a list of folder path's in which program analyze crawled data.
	 * @return
	 */
	public String getListOfFoldersPathsToBeAnayzedFile() {
		return foldersFile;
	}
	
	public String getListOfURLsToAnalyzeFilePath() {
		return urlsFile;
	}
	
	public int getMaxDeclarations() {
		return maxDeclarations;
	}
	
	public int getMaxParameters() {
		return maxParameters;
	}
	
	public int getMaxCalls() {
		return maxCalls;
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
		return folderPaths;
	}
	
	@Override
	public String toString() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		parser.printUsage(outputStream);
		return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
	}

	public String getFilePath() {
		return inputFile;
	}

    public boolean shouldCheckPresentationPreservation() {
	    return shouldCheckPresentationPreservation;
    }

	public boolean shouldCompareRealMixinsAndOpportunitiesCalls() {
		return this.shouldCompareRealMixinsAndOpportunitiesCalls;
	}
}
