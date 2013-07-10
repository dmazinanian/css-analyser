package duplication;

import java.util.HashSet;
import java.util.Set;

import CSSModel.Declaration;
import CSSModel.Selector;

public class Intersection implements Comparable<Intersection> {

	private final Set<Selector> selectors;
	private final Set<Declaration> declarations;

	public Intersection() {
		selectors = new HashSet<>();
		declarations = new HashSet<>();
	}

	public void addSelector(Selector selector) {
		selectors.add(selector);
	}

	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public Set<Selector> getSelectors() {
		return selectors;
	}

	public Set<Declaration> getDeclarations() {
		return declarations;
	}

	@Override
	public int compareTo(Intersection otherIntersection) {
		return Integer.compare(declarations.size(),
				otherIntersection.declarations.size());
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (!(obj instanceof Intersection))
			return false;

		Intersection otherIntersection = (Intersection) obj;

		if (selectors.size() != otherIntersection.selectors.size())
			return false;

		if (declarations.size() != otherIntersection.declarations.size())
			return false;

		return (selectors.containsAll(otherIntersection.selectors) && declarations
				.containsAll(otherIntersection.declarations));
	}

	@Override
	public int hashCode() {
		int result = 17;
		for (Selector selector : selectors)
			result = result * 31 + selector.hashCode();
		for (Declaration declaration : declarations)
			result = result * 31 + declaration.hashCode();
		return result;
	}

	@Override
	public String toString() {
		String string = "Declarations intersection of ";
		for (Selector selector : selectors)
			string += selector;
		string += ": {\n ";
		for (Declaration declaration : declarations)
			string += declaration + "\n ";
		string += "}";
		return string;
	}
}
