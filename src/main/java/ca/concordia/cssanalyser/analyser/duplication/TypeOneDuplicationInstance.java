package ca.concordia.cssanalyser.analyser.duplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * This class represents the occurrences of same declarations
 * for different selectors.
 * 
 * @author Davood Mazinanian
 *
 */
public class TypeOneDuplicationInstance implements DuplicationInstance {

	/* We keep a list of declarations which are the same 
	 * across different selectors
	 * Because we are going to keep the duplicated declarations
	 * which happen for the <same selectors> in one object,
	 * we have used List<List<Declaration>>
	 */
	protected final List<List<Declaration>> forDeclarations;
	
	/*
	 * Although in the Declaration class we have a reference 
	 * to the parent Selector, but we also keep references 
	 * to the selectors as well.
	 */
	protected final Set<Selector> forSelectors;
	
	protected static DuplicationInstanceType duplicationType;
	
	public TypeOneDuplicationInstance() {
		duplicationType = DuplicationInstanceType.TYPE_I;
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
	
	public Set<Selector> getSelectors() {
		return forSelectors;
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
	
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Iterator<Selector> iterator = forSelectors.iterator(); iterator.hasNext();) {
			Selector selector = iterator.next();
			s.append(selector);
			if (selector.getLocationInfo().getLineNumber() >= 0)
				s.append(String.format("(%s)", selector.getLocationInfo()));
			if (iterator.hasNext())
				s.append(", ");
		}
		StringBuilder string = new StringBuilder();
		string.append("For selectors ").append(s).append(", the following declarations are the same:");
		string.append(System.lineSeparator());
		for (List<Declaration> list : forDeclarations) {
			if (list == null || list.get(0) == null) 
				continue;
			string.append("\t[").append(list.get(0)).append("] in the following places:");
			string.append(System.lineSeparator());
			for (Declaration declaration : list) {
				string.append("\t\t").append(declaration.getLocationInfo());
				string.append(System.lineSeparator());
			}
		}
		return string.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj.getClass() != getClass())
			return false;
		
		if (!super.equals(obj))
			return false;
		
		TypeOneDuplicationInstance otherDuplication = (TypeOneDuplicationInstance)obj;
		
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
	 * in conformity with the equals method. 
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

	/**
	 * Adds a list of declarations
	 * @param declarations
	 */
	public void addAllDeclarations(List<Declaration> declarations) {
		for (Declaration declaration : declarations)
			forSelectors.add(declaration.getSelector());
		forDeclarations.add(declarations);
	}

	/**
	 * Determines whether all selectors which are the containers of the given
	 * declarations exist in the given selectors
	 * @param declarations
	 * @return
	 */
	public boolean hasAllSelectorsForADuplication(List<Declaration> declarations) {
		List<Selector> parentSelectors = new ArrayList<>();
		for (Declaration declaration : declarations)
			parentSelectors.add(declaration.getSelector());
		return (parentSelectors.size() == forSelectors.size() && 
				forSelectors.containsAll(parentSelectors));
	}

	@Override
	public DuplicationInstanceType getType() {
		return duplicationType;
	}

}
