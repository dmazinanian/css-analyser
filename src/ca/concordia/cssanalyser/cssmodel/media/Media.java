package ca.concordia.cssanalyser.cssmodel.media;

//import java.util.Iterator;

public abstract class Media {
	//public Iterator<Selector> getAllSelectorsForThisMedia();
	private final int definedInLine;
	private final int definedInColumn;
	
	public Media() {
		definedInLine = -1;
		definedInColumn = -1;
	}
	
	public Media(int line, int coloumn) {
		definedInLine = line;
		definedInColumn = coloumn;
	}

	public int getLine() {
		return definedInLine;
	}

	public int getColumn() {
		return definedInColumn;
	}
	
	
}
