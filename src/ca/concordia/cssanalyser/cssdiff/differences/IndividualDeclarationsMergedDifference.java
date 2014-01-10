package ca.concordia.cssanalyser.cssdiff.differences;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;

public class IndividualDeclarationsMergedDifference extends Difference {
	
	private ShorthandDeclaration shorthandDeclaration;
	private Set<Declaration> individualDeclarations; 
	
	public IndividualDeclarationsMergedDifference(StyleSheet styleSheet1,
			StyleSheet styleSheet2, ShorthandDeclaration shorthandDeclaration) {
		super(styleSheet1, styleSheet2);
		this.shorthandDeclaration = shorthandDeclaration;
		this.individualDeclarations = new HashSet<>();
	}
	
	public ShorthandDeclaration getShorthandDeclaration() {
		return shorthandDeclaration;
	}
	
	public Set<Declaration> getIndividualDeclarations() {
		return individualDeclarations;
	}
	
	public void addIndividualDeclaration(Declaration d) {
		individualDeclarations.add(d);
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("Declaration");
		if (individualDeclarations.size() > 1)
			toReturn.append("s");
		toReturn.append(" ");			
		for (Declaration d : individualDeclarations)
			toReturn.append(d.toString() + " - ");
		if (toReturn.toString().endsWith(" - "))
			toReturn.delete(toReturn.length() - 3, toReturn.length());
		toReturn.append(" of selector " + individualDeclarations.iterator().next().getSelector().toString());
		if (individualDeclarations.size() > 1)
			toReturn.append(" were ");
		else 
			toReturn.append(" was ");
		toReturn.append(" merged into " + shorthandDeclaration.toString() + " in the second stylesheet.");
		return toReturn.toString(); 
	}

}
