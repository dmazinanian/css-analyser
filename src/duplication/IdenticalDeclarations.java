package duplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CSSModel.Declaration;
import CSSModel.Selector;

/**
 * This class represents the occurrences of same declarations
 * for different selectors.
 * @author Davood Mazinanian
 *
 */
public class IdenticalDeclarations extends Duplication {

	/* We keep a list of declarations which are the same 
	 * across different selectors
	 */
	private final List<List<Declaration>> forDeclarations;
	
	/*
	 * Although in the Declaration class we have a reference 
	 * to the parent Selector, but we also keep references 
	 * to the selectors as well.
	 */
	private final Set<Selector> forSelectors;
	
	public IdenticalDeclarations() {
		super(DuplicationType.IDENTICAL_PROPERTY_AND_VALUE);
		forDeclarations = new ArrayList<>();
		forSelectors = new HashSet<>();
	}

	/**
	 * Returns the number of distinct declarations
	 * @return Number of distinct declarations
	 */
	public int getNumberOfDeclarations() {
		return forDeclarations.size();
	}
	
	/**
	 * Number of the distinct selectors which
	 * these duplications belong to.
	 * @return Number of the distinct selectors.
	 */
	public int getNumberOfSelectors() {
		return forSelectors.size();
	}
	
	/**
	 * Add a new but distinct selector to the set
	 * of selectors.
	 * @param selector Selector to be added to the set
	 * @deprecated We add the selector automatically when
	 * adding a new declaration.
	 */
	public void addSelector(Selector selector) {
		forSelectors.add(selector);
	}
	
	/**
	 * Adds a new declaration to the list of declarations.
	 * When a declaration is added, if it is equal to one
	 * of the existing declarations, they would be added to
	 * the same list. If not, another list for containing this
	 * declaration (and all other future declarations which are
	 * the same with this declaration) would be created.
	 * @param declaration Declaration to be added.
	 * @return Returns true if the addition is successful.
	 */
	public boolean addDeclaration(Declaration declaration) {
		boolean added = false;
		for (List<Declaration> declarations : forDeclarations) {
			if (declarations.get(0)!= null && 
					declarations.get(0).equals(declaration)) {
				// If we can find the declaration in one of the lists, add it to that list.
				// Consider looking at the Declaration.equals() method!
				declarations.add(declaration);
				added = true;
			}
		}
		if (!added) {
			/* So we could not find the list containing a similar
			 * declaration to the new declaration (or the
			 * addition was unsuccessful). So create a list, add the new
			 * declaration to it, and add the list to the list of lists! 
			 */
			ArrayList<Declaration> newList = new ArrayList<>();
			newList.add(declaration);
			forDeclarations.add(newList);
			added = true;
		}
		// Lets add the parent selector as well.
		if (added)
			forSelectors.add(declaration.getSelector());
		
		return added;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (Selector selector : forSelectors) {
			s += selector;
			if (selector.getLineNumber() >= 0)
				s += String.format(" (%s : %s)", selector.getLineNumber(), selector.getColumnNumber());
			s += ", ";
		}
		s = s.substring(0, s.length() - 2); // Remove the last comma and space
		String string = "For selectors " + s + ", the following declarations are the same: \n";
		for (List<Declaration> list : forDeclarations) {
			if (list == null || list.get(0) == null) 
				continue;
			string += "\t" + list.get(0) + " in following places: \n";
			for (Declaration declaration : list)
				string += "\t\t" + declaration.getLineNumber() + " : " + declaration.getColumnNumber() + " \n"; 
		}
		return string;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj.getClass() != getClass())
			return false;
		
		if (!super.equals(obj))
			return false;
		
		IdenticalDeclarations otherDuplication = (IdenticalDeclarations)obj;
		
		if (forSelectors.size() != otherDuplication.forSelectors.size() || 
				!forSelectors.containsAll(otherDuplication.forSelectors))
			return false;
		
		if (forDeclarations.size() != otherDuplication.forDeclarations.size())
			return false;
		
		for (List<Declaration> list : forDeclarations) {
			if (list == null || list.get(0) == null) 
				continue;
			Declaration representative = list.get(0);
			boolean notFound = true;
			for (List<Declaration> list2 : otherDuplication.forDeclarations) {
				if (list2 == null)
					continue;
				int i = list2.indexOf(representative);
				if (i > 0) { // found corresponding list
					notFound = false;
					if (list2.size() != list.size() ||
							!list.containsAll(list2))
						return false;
				}	
			}
			if (notFound)
				return false;
		}
		
		return true;
	}
	
	/**
	 * TODO: The implementation is buggy and is not
	 * in conformance with the equals method. 
	 */
	@Override
	public int hashCode() {
		int result = super.hashCode();
		for (Selector selector : forSelectors)
			result += selector.hashCode();
		for (List<Declaration> list : forDeclarations) {
			for (Declaration declaration : list)
				result = 31 * result + declaration.hashCode();
		}
		return result;
	}

	/**
	 * Searches all lists for the given declaration
	 * @param declaration Declaration to find
	 * @return True if the declaration exists in a list
	 */
	public boolean hasDeclaration(Declaration declaration) {
		for (List<Declaration> list : forDeclarations)
			if (list.contains(declaration))
				return true;
		return false;
	}

	public void addAllDeclarations(List<Declaration> currentEqualDeclarations) {
		for (Declaration declaration : currentEqualDeclarations)
			addDeclaration(declaration);
	}

	public boolean hasAllSelectorsForADuplication(List<Declaration> currentEqualDeclarations) {
		List<Selector> parentSelectors = new ArrayList<>();
		for (Declaration declaration : currentEqualDeclarations)
			parentSelectors.add(declaration.getSelector());
		return (parentSelectors.size() == forSelectors.size() && 
				forSelectors.containsAll(parentSelectors));
	}

}
