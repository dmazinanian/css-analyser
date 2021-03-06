package ca.concordia.cssanalyser.cssmodel.selectors.conditions;

/**
 * Selector conditions which are come inside the brackets like div[align]
 * 
 * @see SelectorConditionType
 * @author Davood Mazinanian
 * 
 */
public class SelectorCondition {

	private String conditionName;
	private String conditionValue;
	private SelectorConditionType conditionType;

	/**
	 * This constructor is used when we have a selector like selector[attribute]
	 * 
	 * @param nameOfAttribute
	 */
	public SelectorCondition(String nameOfAttribute) {
		conditionName = nameOfAttribute;
		conditionValue = "";
		conditionType = SelectorConditionType.HAS_ATTRIBUTE;
	}

	/**
	 * Constructor of the SelectorCondition
	 * @param attributeName Name of the attribute
	 * @param value Value of the condition
	 * @param type Type of the condition, based on {@link SelectorConditionType <code>SelectorConditionType</code>}
	 * @see SelectorConditionType
	 */
	public SelectorCondition(String attributeName, String value, SelectorConditionType type) {
		conditionName = attributeName;
		conditionValue = value;
		conditionType = type;
	}

	/**
	 * In the selector[attr operand value] format, returns attr
	 * @return
	 */
	public String getAttributeName() {
		return conditionName;
	}

	/**
	 * Sets the name of the attr,  in the selector[attr operand value] format
	 * @param name
	 */
	public void setAttributeName(String name) {
		conditionName = name;
	}
	
	/**
	 * In the selector[attr operand value] format, returns value
	 * @return
	 */
	public String getValue() {
		return conditionValue;
	}

	/**
	 * In the selector[attr operand value] format, returns type of the operand
	 * based on {@link SelectorConditionType <code>SelectorConditionType</code>}
	 * @see SelectorConditionType 
	 * @param value
	 */
	public void setValue(String value) {
		conditionValue = value;
	}

	/**
	 * In the selector[attr operand value] format, sets the type of the operand
	 * based on {@link SelectorConditionType <code>SelectorConditionType</code>}
	 * @see SelectorConditionType
	 * @param type Type of the condition
	 */
	public void setConditionType(SelectorConditionType type) {
		conditionType = type;
	}
	
	/**
	 * In the selector[attr operand value] format, returns type of the operand
	 * based on {@link SelectorConditionType <code>SelectorConditionType</code>}
	 * @see SelectorConditionType
	 * @return
	 */
	public SelectorConditionType getConditionType() {
		return conditionType;
	}

	@Override
	public String toString() {
		String result = "";
		switch (conditionType) {
		case HAS_ATTRIBUTE:
			result = conditionName;
			break;
		case VALUE_EQUALS_EXACTLY:
		case VALUE_CONTAINS_WORD_SPACE_SEPARATED:
		case VALUE_START_WITH_DASH_SEPARATED:
		case VALUE_ENDS_WITH:
		case VALUE_STARTS_WITH:
		case VALUE_CONTAINS:
			result = String.format("%s %s %s", conditionName,
					conditionType.toString(), conditionValue);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conditionName == null) ? 0 : conditionName.hashCode());
		result = prime * result
				+ ((conditionType == null) ? 0 : conditionType.hashCode());
		result = prime * result
				+ ((conditionValue == null) ? 0 : conditionValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectorCondition other = (SelectorCondition) obj;
		if (conditionName == null) {
			if (other.conditionName != null)
				return false;
		} else if (!conditionName.equals(other.conditionName))
			return false;
		if (conditionType != other.conditionType)
			return false;
		if (conditionValue == null) {
			if (other.conditionValue != null)
				return false;
		} else if (!conditionValue.equals(other.conditionValue))
			return false;
		return true;
	}

	public SelectorCondition clone() {
		return new SelectorCondition(conditionName, conditionValue, getConditionType());
	}
	
}
