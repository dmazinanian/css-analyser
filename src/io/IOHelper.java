package io;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class IOHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IOHelper.class);

	public static List<File> searchForFiles(String folderPath,
			String withExtension) {

		if (!withExtension.startsWith("."))
			withExtension = "." + withExtension;

		final String ext = withExtension;

		File f = new File(folderPath);
		File[] files = f.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(ext);
			}
		});

		return Arrays.asList(files);
	}

	/**
	 * 
	 * @param folderPath
	 * @param replaceExisting
	 */
	public static void createFolder(String folderPath, boolean replaceExisting) {

		File folder = new File(folderPath);

		if (folder.exists()) {
			
			if (replaceExisting) {
				LOGGER.warn("Folder " + folderPath + " already exists. Contents would be overriden.");
				try {
					deleteDirectory(folder);
				} catch (IOException ex) {
					LOGGER.warn("Folder " + folderPath + " could not be deleted.");
				}
			}

		}

		folder.mkdir();
		LOGGER.info("Created folder " + folderPath);

		

	}

	public static void writeFile(FileWriter fw, String line) throws IOException {
		fw.append(line + "\r\n");
	}

	public static FileWriter openFile(String path) throws IOException {
		File f = new File(path);
		FileWriter fw = new FileWriter(f);
		return fw;
	}

	public static void closeFile(FileWriter fw) throws IOException {
		fw.close();
	}
	
	public static String readFileToString(String path) throws IOException {
		return org.apache.commons.io.FileUtils.readFileToString(new File(path));
	}

	/**
	 * Returns true if the file or folder in the given path exists
	 * @param path
	 * @return
	 */
	public static boolean exists(String path) {
		return (new File(path)).exists();
	}

	public static void deleteDirectory(File folder) throws IOException {
		org.apache.commons.io.FileUtils.deleteDirectory(folder);
	}
}
