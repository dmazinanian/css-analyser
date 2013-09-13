package CSSModel.selectors;

import java.util.ArrayList;
import java.util.List;

import CSSModel.selectors.conditions.SelectorCondition;

/**
 * An atomic element selector, is a selector 
 * in the format element#id.class1.class2....:PseudoClass1...:PseudoClassK::PseudoElement
 * 
 * @author Davood Mazinanian
 *
 */
public class AtomicElementSelector extends AtomicSelector {
	
	private String selectedElementName = "";
	private List<String> selectedClasses;
	private String selectedID = "";
	private final List<SelectorCondition> conditions;
	private final List<PseudoClass> pseudoClasses;
	private final List<PseudoElement> pseudoElements;

	public AtomicElementSelector() {
		this(null);
	}

	public AtomicElementSelector(GroupedSelectors parent) {
		this(parent, -1, -1);
	}

	public AtomicElementSelector(int fileLineNumber, int fileColNumber) {
		this(null, fileColNumber, fileLineNumber);
	}

	/**
	 * 
	 * @param 	parent Parent GroupedSelectors object. In a selector 
	 * 			like "p, div", the "p, div" is a parent GroupedSelectors
	 * 			object and "p" and "div" would be the atomic element selectors
	 * @param fileLineNumber
	 * 			Line number of the source of container stylesheet.
	 * @param fileColumnNumber
	 * 			Column number of the source of container stylesheet.
	 * 
	 */
	public AtomicElementSelector(GroupedSelectors parent, int fileLineNumber,
			int fileColumnNumber) {
		super(parent, fileLineNumber, fileColumnNumber);
		conditions = new ArrayList<>();
		pseudoClasses = new ArrayList<>();
		selectedClasses = new ArrayList<>();
		pseudoElements = new ArrayList<>();
	}

	public void setSelectedElementName(String elementName) {
		selectedElementName = elementName;
	}

	public String getSelectedElementName() {
		return selectedElementName;
	}
	
	public void addClassName(String className) {
		selectedClasses.add(className);
	}
	
	public List<String> getClassNames() {
		return selectedClasses;
	}

	public void setElementID(String idName) {
		selectedID = idName;
	}
	
	/**
	 * Returns the ID of the current selector. For example,
	 * selector "#test" would have "test" as its ID name
	 * @return
	 */
	public String getElementID() {
		return selectedID;
	}

	/**
	 * Adds different conditions to current selector.
	 * @param condition
	 * @see CSSModel.selectors.conditions.SelectorCondition
	 * @see CSSModel.selectors.conditions.SelectorConditionType
	 */
	public void addCondition(SelectorCondition condition) {
		conditions.add(condition);
	}
	
	public List<SelectorCondition> getConditions() {
		return conditions;
	}

	public void addPseudoClass(PseudoClass pseudoClass) {
		pseudoClasses.add(pseudoClass);
	}
	
	/**
	 * Returns all PseudoClasses of current selector
	 * @return
	 */
	public List<PseudoClass> getPseudoClasses() {
		return pseudoClasses;
	}

	/**
	 * Adds a PseudoElement to current selector
	 * @param pseudoElement
	 */
	public void addPseudoElement(PseudoElement pseudoElement) {
		pseudoElements.add(pseudoElement);
	}
	
	/**
	 * Returns the pseudo elements of current selector (like ::selector)
	 * @return
	 */
	public List<PseudoElement> getPseudoElements() {
		return pseudoElements;
	}
	
	@Override
	public boolean selectorEquals(Selector otherSelector) {
		if (!checkGeneralEquality(otherSelector))
			return false;
		
		AtomicElementSelector otherAtomicSelector = (AtomicElementSelector)otherSelector;
		
		return selectedElementName.equalsIgnoreCase(otherAtomicSelector.selectedElementName)
				&& selectedID.equalsIgnoreCase(otherAtomicSelector.selectedID)
				&& selectedClasses.size() == otherAtomicSelector.selectedClasses.size() && 
				(selectedClasses.size() != 0 ? selectedClasses.containsAll(otherAtomicSelector.selectedClasses) : true)
				&& (conditions.size() == otherAtomicSelector.conditions.size()
				&& conditions.containsAll(otherAtomicSelector.conditions))
				&& pseudoClasses.equals(otherAtomicSelector.pseudoClasses) &&
				pseudoElements.equals(otherAtomicSelector.pseudoElements);
	}
	
	/**
	 * Two atomic element selectors are equal
	 * if they are in the same line anc column in the file, 
	 */
	@Override
	public boolean equals(Object obj) {

		if (!checkGeneralEquality(obj))
		return false;
		
		AtomicElementSelector otherAtomicSelector = (AtomicElementSelector) obj;

		return (lineNumber == otherAtomicSelector.lineNumber &&
				columnNumber == otherAtomicSelector.columnNumber &&
				/*parentMedia == otherAtomicSelector.parentMedia &&*/
				selectorEquals(otherAtomicSelector));
	}

	private boolean checkGeneralEquality(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != AtomicElementSelector.class)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + lineNumber;
		result = 31 * result + columnNumber;
		if (selectedID != null)
			result = 31 * result + selectedID.hashCode();
		if (selectedElementName != null)
			result = 31 * result + selectedElementName.hashCode();
		for (String c : selectedClasses)
			result += c.hashCode();
		for (SelectorCondition condition : conditions)
			result += (condition == null ? 0 : condition.hashCode());
		for (PseudoClass pseudoClass : pseudoClasses)
			result = 31 * result + (pseudoClass == null ? 0 : pseudoClass.hashCode());
		for (PseudoElement pElement : pseudoElements)
			result = 31 * result + (pElement == null ? 0 : pElement.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(selectedElementName != null ? selectedElementName : "");
		if (selectedID != "")
			result.append("#" + selectedID);
		if (selectedClasses.size() > 0)
			for (String c : selectedClasses)
				result.append("." + c);
		for (SelectorCondition condition : conditions)
			result.append("[" + condition + "]");
		for (PseudoClass pseudoClass : pseudoClasses)
			result.append(":" + pseudoClass);
		for (PseudoElement pelement : pseudoElements)
			result.append("::" + pelement);
		return result.toString();
	}

}
