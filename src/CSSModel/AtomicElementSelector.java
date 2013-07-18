package CSSModel;

import java.util.ArrayList;
import java.util.List;

public class AtomicElementSelector extends AtomicSelector {
	private String selectedElementName = "";
	private List<String> selectedClasses;
	private String selectedIDName = "";
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

	public void addClassName(String className) {
		selectedClasses.add(className);
	}

	public void setIDName(String idName) {
		selectedIDName = idName;
	}

	public void addCondition(SelectorCondition condition) {
		conditions.add(condition);
	}

	public void addPseudoClass(PseudoClass pseudoClass) {
		pseudoClasses.add(pseudoClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AtomicElementSelector))
			return false;
		AtomicElementSelector otherObject = (AtomicElementSelector) obj;

		// First check for element name, ID and Class which have to be the same
		return selectedElementName.equals(otherObject.selectedElementName)
				&& selectedIDName.equals(otherObject.selectedIDName)
				&& selectedClasses.size() == otherObject.selectedClasses.size() && 
				(selectedClasses.size() != 0 ? selectedClasses.containsAll(otherObject.selectedClasses) : true)
				&& (conditions.size() == otherObject.conditions.size()
				&& conditions.containsAll(otherObject.conditions))
				&& pseudoClasses.equals(otherObject.pseudoClasses) &&
				pseudoElements.equals(otherObject.pseudoElements);
	}

	@Override
	public int hashCode() {
		int result = 17;
		if (selectedIDName != null)
			result = 31 * result + selectedIDName.hashCode();
		if (selectedElementName != null)
			result = 31 * result + selectedElementName.hashCode();
		for (String c : selectedClasses)
			result += c.hashCode();
		for (SelectorCondition condition : conditions)
			result = 31 * result + (condition == null ? 0 : condition.hashCode());
		for (PseudoClass pseudoClass : pseudoClasses)
			result = 31 * result + (pseudoClass == null ? 0 : pseudoClass.hashCode());
		for (PseudoElement pElement : pseudoElements)
			result = 31 * result + (pElement == null ? 0 : pElement.hashCode());
		return result;
	}

	@Override
	public String toString() {
		String result = selectedElementName != null ? selectedElementName : "";
		if (selectedIDName != "")
			result += "#" + selectedIDName;
		if (selectedClasses.size() > 0)
			for (String c : selectedClasses)
				result += "." + c;
		for (SelectorCondition condition : conditions)
			result += "[" + condition + "]";
		for (PseudoClass pseudoClass : pseudoClasses)
			result += ":" + pseudoClass;
		for (PseudoElement pelement : pseudoElements)
			result += "::" + pelement;
		return result;
	}

	public void addPseudoElement(PseudoElement pseudoElement) {
		pseudoElements.add(pseudoElement);
	}

}
