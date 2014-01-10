package ca.concordia.cssanalyser.cssdiff.differences;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;

/**
 * Value of the same declaration is changed from one value to another.
 * @author Davood Mazinanian
 *
 */
public class ChangedDeclarationValueDifference extends Difference {

	private Declaration declaration1;
	private Declaration declaration2;
	
	public ChangedDeclarationValueDifference(StyleSheet styleSheet1, StyleSheet styleSheet2, Declaration declaration1, Declaration declaration2) {
		super(styleSheet1, styleSheet2);
		this.declaration1 = declaration1;
		this.declaration2 = declaration2;
	}
	
	public Declaration getDeclaration1() {
		return declaration1;
	}
	
	public Declaration getDeclaration2() {
		return declaration2;
	}
	
	@Override
	public String toString() {
		String value1 = "", value2 = "";
		
		for (DeclarationValue v : declaration1.getRealValues()) 
			value1 += v.getValue() + " ";
		
		for (DeclarationValue v : declaration2.getRealValues()) 
			value2 += v.getValue() + " ";
		
		return "Value of " + declaration1 + " in selector " + declaration1.getSelector() + " is changed from " +  value1.trim() + " to " + value2.trim();
	}
}
