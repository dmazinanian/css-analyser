package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.Locator;
import org.w3c.css.sac.SelectorList;

public class NegativeConditionImpl implements Condition {
	
	Locator loc;
	SelectorList selectorList;

	public NegativeConditionImpl(SelectorList selectorList, Locator loc) {
		this.selectorList = selectorList;
		this.loc = loc;
	}

	public SelectorList getSelectorList() {
		return selectorList;
	}
	
	public Locator getLocator() {
		return loc;
	}

	@Override
	public short getConditionType() {
		return SAC_NEGATIVE_CONDITION;
	}
	
}
