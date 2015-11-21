package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.AttributeCondition;

public class FunctionPseudoClassCondition implements AttributeCondition {
	String value;
	String localName;
	boolean specified;
	String namespaceURI;
	
	public FunctionPseudoClassCondition(String localName, String value) {
		this.value = value;
		this.localName = localName;
	}

	@Override
	public short getConditionType() {
		return SAC_PSEUDO_CLASS_CONDITION;
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
