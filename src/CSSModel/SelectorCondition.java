package CSSModel;

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

	public SelectorCondition(String nameOfCondition, String valueOfCondition,
			SelectorConditionType typeOfCondition) {
		conditionName = nameOfCondition;
		conditionValue = valueOfCondition;
		conditionType = typeOfCondition;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setName(String name) {
		conditionName = name;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String value) {
		conditionValue = value;
	}

	public void setConditionType(SelectorConditionType type) {
		conditionType = type;
	}

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
		case VALUE_CONTAINS_WORD:
		case VALUE_STARTING_WITH:
		case VALUE_ENDS_WITH:
			result = String.format("%s %s %s", conditionName,
					conditionType.toString(), conditionValue);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SelectorCondition))
			return false;
		SelectorCondition otherObject = (SelectorCondition) obj;
		return (conditionName.equals(otherObject.conditionName)
				&& conditionValue.equals(otherObject.conditionValue) && conditionType
					.equals(otherObject.conditionType));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
