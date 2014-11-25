/**
 * 
 */
package ca.concordia.cssanalyser.cssmodel;

/**
 * Stores the location info for a CSS element
 * @author Davood Mazinanian
 *
 */
public class LocationInfo {
	private int lineNumber;
	private int columnNumber;
	private int offset;
	private int lenghth;
	
	public LocationInfo() {
		lineNumber = columnNumber = offset = lenghth = -1;
	}
	
	public LocationInfo(int fileLineNumber, int fileColNumber) {
		this.lineNumber = fileLineNumber;
		this.columnNumber = fileColNumber;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLenghth() {
		return lenghth;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnNumber;
		result = prime * result + lenghth;
		result = prime * result + lineNumber;
		result = prime * result + offset;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationInfo other = (LocationInfo) obj;
		if (columnNumber != other.columnNumber)
			return false;
		if (lenghth != other.lenghth)
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return String.format("Line: %6s, Column: %6s, Offset: %8s, Length: %8s", lineNumber, columnNumber, offset, lenghth);
	}
	
}
