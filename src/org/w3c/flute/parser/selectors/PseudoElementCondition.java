package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.Condition;

public class PseudoElementCondition implements Condition {

	final String name;
	
	public PseudoElementCondition(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public short getConditionType() {
		return SAC_PSEUDO_CLASS_CONDITION;
	}

}
