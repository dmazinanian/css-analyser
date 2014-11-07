package ca.concordia.cssanalyser.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.app.FileLogger;


public final class IOHelper {
	
	private static final Logger LOGGER = FileLogger.getLogger(IOHelper.class);

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

	public static void writeLinesToFile(Iterable<?> lines, String path) {
		writeLinesToFile(lines, path, false);
	}
	/**
	 * Writes the lines in the object of Iterable<> to the path
	 * @param lines
	 * @param path
	 * @param append
	 */
	public static void writeLinesToFile(Iterable<?> lines, String path, boolean append) {
		
		try {
			
			BufferedWriter fw = openFile(path, append);
	
			for (Object row : lines) {
				writeFile(fw, row.toString());
			}
			
			closeFile(fw);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void writeStringToFile(String string, String path) {
		writeStringToFile(string, path, false);
	}
	
	public static void writeStringToFile(String string, String path, boolean append) {
		File f = new File(path);
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(f, append));
			fw.append(string);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static boolean deleteFile(String path) {
		File f = new File(path);
		if (f.exists())
			return f.delete();
		return false;
	}

	public static boolean isFolder(String path) {
		File file = new File(path);
		
		if (file.isDirectory())
			return true;
		
		return false;
	}

	public static String getContainingFolder(String path) {
		File f = new File(path);
		return f.getParent();
	}
	
	
	
}
