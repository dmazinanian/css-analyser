package CSSModel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main class storing all CSS data
 * in the memory
 * @author Davood Mazinanian
 */
public class StyleSheet {
	
	private final List<Selector> listOfSelectors;
	
	public StyleSheet() {
		listOfSelectors = new ArrayList<>();
	}
	
	/**
	 * Adds a new selector (whether atomic or grouped) to 
	 * the selectors list of this style sheet.
	 * @param selector
	 */
	public void addSelector(Selector selector) {
		listOfSelectors.add(selector);
	}
	
	/**
	 * Returns all the selectors, whether atomic or grouped
	 * in the style sheet.
	 * @return List<Selector>
	 */
	public List<Selector> getAllSelectors() {
		return listOfSelectors;
	}

	/**
	 * This method returns all the atomic selectors in the 
	 * style sheet, in addition to the all atomic selectors inside the 
	 * grouped selectors. It preserves the order of atomic selectors.
	 * @return List<AtomicSelector>
	 */
	public List<AtomicSelector> getAllAtomicSelectors() {
		List<AtomicSelector> allAtomicSelectors = new ArrayList<>();

		for (Selector selector : listOfSelectors) { // Look inside all selectors
			if (selector instanceof AtomicSelector) {
				// Just add
				allAtomicSelectors.add((AtomicSelector) selector);
			} else if (selector instanceof GroupedSelectors) {
				// Loop through all grouped selectors
				for (AtomicSelector atomicSelector : (GroupedSelectors) selector) {
					allAtomicSelectors.add(atomicSelector);
				}
			}
		}
		return allAtomicSelectors;
	}

	/**
	 * This method returns all the declarations inside a 
	 * style sheet, preserving their order in which they have
	 * been defined.
	 * @return List<Declaration>
	 */
	public List<Declaration> getAllDeclarations() {
		List<Declaration> allDeclarations = new ArrayList<>();
		for (Selector selector : listOfSelectors) 
			for (Declaration declaration : selector.getAllDeclarations())
				allDeclarations.add(declaration);
		return allDeclarations;
	}
}
