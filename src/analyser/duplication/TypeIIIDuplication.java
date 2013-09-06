/**
 * 
 */
package analyser.duplication;

import java.util.Set;

import CSSModel.declaration.Declaration;
import CSSModel.declaration.ShorthandDeclaration;

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
public class TypeIIIDuplication implements Duplication {

	private final ShorthandDeclaration shorthandDeclaration;
	private final Set<Declaration> individualDeclarations;

	public TypeIIIDuplication(ShorthandDeclaration shorthand,
			Set<Declaration> individual) {
		shorthandDeclaration = shorthand;
		individualDeclarations = individual;
	}

	@Override
	public DuplicationType getType() {
		return DuplicationType.TYPE_III;
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
