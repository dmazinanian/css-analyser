package ca.concordia.cssanalyser.cssdiff.differences;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;

public class RemovedDeclarationDifference extends Difference {

private Declaration declaration;
	
	public RemovedDeclarationDifference(StyleSheet styleSheet1, StyleSheet styleSheet2, Declaration removedDeclaration) {
		super(styleSheet1, styleSheet2);
		this.declaration = removedDeclaration;
	}
	
	
	public Declaration getDeclaration() {
		return this.declaration;
	}
	
	@Override
	public String toString() {
		return "Declaration " + declaration.toString() + " has been removed from " +
				"selector " + declaration.getSelector() + " from the first stylesheet.";
	}
	
}
