package duplication;

import java.util.ArrayList;
import java.util.List;

import CSSModel.declaration.Declaration;
import CSSModel.selectors.Selector;

/**
 * This class represents the cases that you have the same selector and same
 * declarations for them. For these kinds of duplications, we may want to remove
 * all the repeating declarations and just keep one.
 * 
 * @author Davood Mazinanian
 */
public class IdenticalEffects extends Duplication {

	// The first selector of this list would be representative
	private final Selector mainSelector;
	private final List<List<Declaration>> forDeclarations;

	public IdenticalEffects(Selector selector) {
		super(DuplicationType.IDENTICAL_EFFECT);
		mainSelector = selector;
		forDeclarations = new ArrayList<>();
	}

	public Selector getSelector() {
		return mainSelector;
	}

	public boolean addDeclaration(Declaration declaration) {
		for (List<Declaration> list : forDeclarations) {
			if (list.get(0).equals(declaration))
				return list.add(declaration);
		}
		ArrayList<Declaration> newList = new ArrayList<>();
		newList.add(declaration);
		return forDeclarations.add(newList);
	}

	@Override
	public String toString() {
		String string = "For selector " + mainSelector
				+ " the following declarations are defined more than once: \n";
		for (List<Declaration> list : forDeclarations) {
			string += "\t" + list.get(0) + " in the following lines: \n";
			for (Declaration declaration : list)
				string += "\t\t" + declaration.getLineNumber() + " : " + declaration.getColumnNumber() + " \n";
		}
		return string;
	}

	public int getNumberOfDeclarations() {
		return forDeclarations.size();
	}

	public void addAllDeclarations(List<Declaration> currentEqualDeclarations) {
		for (Declaration declaration : currentEqualDeclarations)
			addDeclaration(declaration);
	}

	// TODO: equals() and hashCode()
}
