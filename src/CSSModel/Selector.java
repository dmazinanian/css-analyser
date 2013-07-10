package CSSModel;

import java.util.LinkedList;
import java.util.List;

public abstract class Selector {
	
	protected int lineNumber;
	protected int columnNumber;
	protected Media parentMedia;
	protected List<Declaration> cssRules;
	protected int specificityOfSelector;
	
	public int getSpecificity() {
		return specificityOfSelector;
	}

	public void setSpecificity(int specificity) {
		specificityOfSelector = specificity;
	}

	public Selector() {
		this(-1, -1);
	}

	public Selector(int fileLineNumber, int fileColNumber) {
		lineNumber = fileColNumber;
		columnNumber = fileColNumber;
		cssRules = new LinkedList<>();
	}

	public void addCSSRule(Declaration rule) {
		cssRules.add(rule);
	}

	public List<Declaration> getAllDeclarations() {
		return cssRules;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int linNumber) {
		lineNumber = linNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int fileColumnNumber) {
		columnNumber = fileColumnNumber;
	}

	public Media getMedia() {
		return parentMedia;
	}

	public void setMedia(Media media) {
		parentMedia = media;
	}

	public void setMedia(String name) {
		setMedia(new AtomicMedia(name));
	}
}
