package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.Condition;

public class EndsWithCondition implements AttributeCondition {

	String localName;
	String value;
	String namespaceURI;
	boolean specified;

	public EndsWithCondition(String localName, String value) {
		this.localName = localName;
		this.value = value;
	}

	@Override
	public short getConditionType() {
		return Condition.SAC_ATTRIBUTE_CONDITION;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public boolean getSpecified() {
		return specified;
	}

	@Override
	public String getValue() {
		return value;
	}

}
