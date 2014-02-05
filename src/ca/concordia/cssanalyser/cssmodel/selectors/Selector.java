package ca.concordia.cssanalyser.cssmodel.selectors;

import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.media.Media;
import ca.concordia.cssanalyser.cssmodel.media.SingleMedia;


public abstract class Selector {
	
	protected int lineNumber;
	protected int columnNumber;
	protected Media parentMedia;
	protected Set<Declaration> declarations;
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
		lineNumber = fileLineNumber;
		columnNumber = fileColNumber;
		declarations = new LinkedHashSet<>();
		
	}
	public void addDeclaration(Declaration declaration) {
		declaration.setSelector(this);
		declarations.add(declaration);
	}

	public Set<Declaration> getDeclarations() {
		return declarations;
	}
	
//	public Collection<Declaration> getAllDeclarationsHS() {
//		return declarationsHashSet;
//	}

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
		setMedia(new SingleMedia(name));
	}
	
	/**
	 * The equals() method for different selectors have different meanings
	 * but in all of them selectors should be exactly the same and are appeared
	 * in the same location in the file. This method provides a way to compare
	 * two selectors to see weather they are equal, regardless of their
	 * definition location
	 * @param otherSelector
	 * @return
	 */
	public abstract boolean selectorEquals(Selector otherSelector);
	
	public abstract Selector clone();
	
	public abstract String getXPath() throws UnsupportedSelectorToXPathException;
	
	@SuppressWarnings("serial")
	public static class UnsupportedSelectorToXPathException extends Exception {
		private Selector selector;
		public UnsupportedSelectorToXPathException(Selector selector) {
			this.selector = selector;
		}
		public Selector getSelector() {
			return selector;
		}
	}

	/**
	 * Removes a declaration from list of declarations
	 * @param declaration
	 */
	public void removeDeclaration(Declaration declaration) {
		declaration.setSelector(null);
		this.declarations.remove(declaration);
	}

	/**
	 * Copies current BaseSelector without any declaration
	 * @return
	 */
	public Selector copyEmptySelector() {
		Selector newEmptySelector = this.clone();
		newEmptySelector.getDeclarations().clear();
		return newEmptySelector;
	}	
}
