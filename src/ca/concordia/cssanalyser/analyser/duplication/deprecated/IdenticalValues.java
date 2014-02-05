package ca.concordia.cssanalyser.analyser.duplication.deprecated;
/*
import java.util.ArrayList;
import java.util.List;

import duplication.DuplicationInstance;
import duplication.DuplicationInstanceType;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.DeclarationValue;

public class IdenticalValues extends DuplicationInstance {

	private final List<Declaration> declarations;

	public IdenticalValues() {
		super(DuplicationInstanceType.IDENTICAL_VALUE);
		declarations = new ArrayList<>();
	}

	/**
	 * Returns that, for which value we are collecting
	 * the duplications
	 * @return The value for which we are collecting the duplications
	 * /
	public List<DeclarationValue> getForWhichValue() {
		if (declarations.get(0) != null)
			return declarations.get(0).getValues();
		return null;
	}
	
	/**
	 * Adds a declaration (so its line number and corresponding selector)
	 * to the list of declarations for current duplication.
	 * @param declaration
	 *  /
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	@Override
	public String toString() {
		String string = "DuplicationInstance for value: \n" + getForWhichValue()
				+ " in the following lines: \n";
		for (Declaration declaration : declarations)
			string += "\t" + declaration.getLineNumber() + " : " + declaration.getColumnNumber() + "\n";
		return string;
	}

	/**
	 * Returns the number of declarations
	 * @return Returns the number of declarations
	 * /
	public int getNumberOfDeclarations() {
		return declarations.size();
	}
	
	// TODO Consider implementing equals() and hashCode()
}
*/