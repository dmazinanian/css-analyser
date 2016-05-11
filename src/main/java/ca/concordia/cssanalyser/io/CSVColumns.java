package ca.concordia.cssanalyser.io;

import java.util.ArrayList;
import java.util.List;

public class CSVColumns {
	
	private String separator = "|";
	
	private final List<String> columns;
	
	public CSVColumns(String... columns) {
		this.columns = new ArrayList<>();
		for (String column : columns) {
			this.columns.add(column);
		}
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public String getSeparator() {
		return this.separator;
	}
	
	public void addColumn(String columnName) {
		this.columns.add(columnName);
	}

	public String getHeader(boolean addLineSeparator) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < columns.size(); i++) {
			toReturn.append(columns.get(i));
			if (i < columns.size() - 1) {
				toReturn.append(this.separator);
			} else { 
				if (addLineSeparator) {
					toReturn.append(System.lineSeparator());
				}
			}
		}
		return toReturn.toString();
	}
	
	public String getRowFormat(boolean addLineSeparator) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < columns.size(); i++) {
			toReturn.append("%s");
			if (i < columns.size() - 1) {
				toReturn.append(separator);
			} else { 
				if (addLineSeparator) {
					toReturn.append(System.lineSeparator());
				}
			}
		}
		return toReturn.toString();
	}
	
}
