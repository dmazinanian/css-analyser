package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
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
		
		if (files == null || files.length == 0)
			return new ArrayList<File>();
		else
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
				//try {
				//	deleteDirectory(folder);
				//} catch (IOException ex) {
				//	LOGGER.warn("Folder " + folderPath + " could not be deleted.");
				//}
			}

		} else {

			folder.mkdir();
			LOGGER.info("Created folder " + folderPath);
		}

		

	}

	public static void writeFile(BufferedWriter fw, String line) throws IOException {
		fw.append(line + "\r\n");
	}

	public static BufferedWriter openFile(String path) throws IOException {
		return openFile(path, false);
	}
	public static BufferedWriter openFile(String path, boolean append) throws IOException {
		File f = new File(path);
		BufferedWriter fw = new BufferedWriter(new FileWriter(f, append));
		return fw;
	}

	public static void closeFile(BufferedWriter fw) throws IOException {
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
