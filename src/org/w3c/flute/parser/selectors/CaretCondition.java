package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.Condition;

public class CaretCondition implements AttributeCondition {

	String localName;
	String value;

	/**
	 * Creates a new AttributeConditionImpl
	 */
	public CaretCondition(String localName, String value) {
		this.localName = localName;
		this.value = value;
	}

	/**
	 * An integer indicating the type of <code>Condition</code>.
	 */
	public short getConditionType() {
		return Condition.SAC_ATTRIBUTE_CONDITION;
	}

	/**
	 * Returns the <a
	 * href="http://www.w3.org/TR/REC-xml-names/#dt-NSName">namespace URI</a> of
	 * this attribute condition.
	 * <p>
	 * <code>NULL</code> if :
	 * <ul>
	 * <li>this attribute condition can match any namespace.
	 * <li>this attribute is an id attribute.
	 * </ul>
	 */
	public String getNamespaceURI() {
		return null;
	}

	/**
	 * Returns the <a
	 * href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">local part</a> of
	 * the <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified
	 * name</a> of this attribute.
	 * <p>
	 * <code>NULL</code> if :
	 * <ul>
	 * <li>
	 * <p>
	 * this attribute condition can match any attribute.
	 * <li>
	 * <p>
	 * this attribute is a class attribute.
	 * <li>
	 * <p>
	 * this attribute is an id attribute.
	 * <li>
	 * <p>
	 * this attribute is a pseudo-class attribute.
	 * </ul>
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Returns <code>true</code> if the attribute must have an explicit value in
	 * the original document, <code>false</code> otherwise.
	 */
	public boolean getSpecified() {
		return false;
	}

	/**
	 * Returns the value of the attribute. If this attribute is a class or a
	 * pseudo class attribute, you'll get the class name (or psedo class name)
	 * without the '.' or ':'.
	 */
	public String getValue() {
		return value;
	}

}
