package ca.concordia.cssanalyser.io;

public class CSVColumns {
	
	private static String SEPARATOR = "|";
	
	private final String[] columns;
	
	public CSVColumns(String... columns) {
		this.columns = columns;
	}
	
	public void setSeparator(String separator) {
		SEPARATOR = separator;
	}

	public String getHeader(boolean addLineSeparator) {
		StringBuilder toReturn = new StringBuilder();
		for (int i = 0; i < columns.length; i++) {
			toReturn.append(columns[i]);
			if (i < columns.length - 1) {
				toReturn.append(SEPARATOR);
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
		for (int i = 0; i < columns.length; i++) {
			toReturn.append("%s");
			if (i < columns.length - 1) {
				toReturn.append(SEPARATOR);
			} else { 
				if (addLineSeparator) {
					toReturn.append(System.lineSeparator());
				}
			}
		}
		return toReturn.toString();
	}
	
}
