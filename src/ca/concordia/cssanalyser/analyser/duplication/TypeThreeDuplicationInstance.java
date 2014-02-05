/**
 * 
 */
package ca.concordia.cssanalyser.analyser.duplication;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * By definition, a type III duplication in a CSS files is: <br />
 * A pair of selectors (A, B) where a set of x declarations in A is equivalent
 * with a set of y declarations in B (x ≠ y). For example, shorthand property
 * border can be used to replace the individual properties border-width,
 * border-style, and border-color
 * 
 * @author Davood Mazinanian
 * 
 */
public class TypeThreeDuplicationInstance implements DuplicationInstance {

	private final ShorthandDeclaration shorthandDeclaration;
	private final Set<Declaration> individualDeclarations;

	public TypeThreeDuplicationInstance(ShorthandDeclaration shorthand,
			Set<Declaration> individual) {
		shorthandDeclaration = shorthand;
		individualDeclarations = individual;
	}

	@Override
	public DuplicationInstanceType getType() {
		return DuplicationInstanceType.TYPE_III;
	}

	public Set<Selector> getSelectors() {
		Set<Selector> toReturn = new HashSet<>();
		toReturn.add(shorthandDeclaration.getSelector());
		toReturn.add(individualDeclarations.iterator().next().getSelector());
		return toReturn;
	}
	
	@Override
	public String toString() {

		StringBuilder individualString = new StringBuilder();
		for (Declaration dec : individualDeclarations)
			individualString.append(String.format("%s (%s, %s); ", dec,
					dec.getLineNumber(), dec.getColumnNumber()));

		return String.format("%s (%s, %s) = %s", shorthandDeclaration,
				shorthandDeclaration.getLineNumber(),
				shorthandDeclaration.getColumnNumber(),
				individualString.substring(0, individualString.length() - 2));

	}

}
