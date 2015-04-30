package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.CSSModelObject;
import ca.concordia.cssanalyser.cssmodel.CSSOrigin;
import ca.concordia.cssanalyser.cssmodel.CSSSource;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * The representation of a single CSS declaration which consists of a
 * a property (as a String).
 * Values will be there in the subclasses, depending on the number of values (as {@link DeclarationValue}s. 
 * 
 * @author Davood Mazinanian
 *
 */
public abstract class Declaration extends CSSModelObject implements Cloneable {

	protected final String property;
	protected Selector parentSelector;
	protected boolean isImportant;
	protected CSSOrigin origin = CSSOrigin.AUTHOR;
	protected CSSSource source = CSSSource.EXTERNAL;
	
	/**
	 * Declarations can be the building blocks of a shorthand declaration.
	 * (for instance, margin-left for a margin).
	 * In this case, this reference will keep track of that.
	 */
	protected ShorthandDeclaration parentShorthand;
	
	/**
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param offset
	 * @param length
	 * @param important
	 */
	public Declaration(String propertyName, Selector belongsTo, boolean important, LocationInfo location) {
		property = propertyName.toLowerCase().trim();
		parentSelector = belongsTo;
		isImportant = important;
		locationInfo = location;
	}
	
	/**
	 * For properties which have vendor prefixes
	 * (like -moz-, -webkit-, etc.)
	 * return the property without prefix
	 * @return
	 */
	public static String getNonVendorProperty(String property) {
		String torReturn = property;
		Set<String> prefixes = new HashSet<>();
		prefixes.add("-webkit-");
		prefixes.add("-moz-");
		prefixes.add("-ms-");
		prefixes.add("-o-");
		
		for (String prefix : prefixes)
			if (torReturn.startsWith(prefix)) {
				torReturn = torReturn.substring(prefix.length());
				break;
			}
		return torReturn;
	}
	
	public static String getNonHackedProperty(String property) {
		String torReturn = property;
		Set<String> prefixes = new HashSet<>();
		prefixes.add("*");
		prefixes.add("_");
		
		for (String prefix : prefixes)
			if (torReturn.startsWith(prefix)) {
				torReturn = torReturn.substring(prefix.length());
				break;
			}
		return torReturn;
	}
	
	/**
	 * Returns true if the declaration is declared with !important
	 * @return
	 */
	public boolean isImportant() {
		return isImportant;
	}
	
	/**
	 * Sets the !important value
	 * @param isImportant
	 */
	public void isImportant(boolean isImportant) {
		this.isImportant = isImportant;
	}

	/**
	 * Returns the selector to which this declaration belongs
	 * @return
	 */
	public Selector getSelector() {
		return parentSelector;
	}
	
	/**
	 * Returns the selector to which this declaration belongs
	 * @return
	 */
	public void setSelector(Selector selector) {
		this.parentSelector = selector;
		if (!this.parentSelector.containsDeclaration(this))
			this.parentSelector.addDeclaration(this);
	}

	/**
	 * Returns the name of the property of this declaration
	 * @return
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * Compares two declarations based only on their values to see if they are Equal.
	 * @param otherDeclaration
	 * @return
	 */
	protected abstract boolean valuesEqual(Declaration otherDeclaration);

	/**
	 * Compares two declarations based only on their values to see if they are Equal.
	 * @param otherDeclaration
	 * @return
	 */
	protected abstract boolean valuesEquivalent(Declaration otherDeclaration);

	/**
	 * Return true if the given declarations is equivalent
	 * with this declaration
	 * @param otherDeclaration
	 * @return
	 */
	public boolean declarationIsEquivalent(Declaration otherDeclaration) {
		return compareDeclarations(otherDeclaration, false, true);
	}
	
	/**
	 * Return true if the given declarations is equivalent
	 * with this declaration, ignoring the vendor-prefix properties
	 * @param otherDeclaration
	 * @return
	 */
	public boolean declarationIsEquivalent(Declaration otherDeclaration, boolean nonVendorPrefixesEquivalent) {
		return compareDeclarations(otherDeclaration, nonVendorPrefixesEquivalent, true);		
	}
	
	/**
	 * Return true if the given declarations is equal
	 * with this declaration
	 * @param otherDeclaration
	 * @return
	 */
	public boolean declarationEquals(Declaration otherDeclaration) {
		return compareDeclarations(otherDeclaration, false, false);
	}
	
	/**
	 * Compares two declarations based on the given parameters.
	 * @param otherDeclaration
	 * @param skipVendor Skips the vendor-specific prefix
	 * @param equivalent 
	 * @return
	 */
	private boolean compareDeclarations(Declaration otherDeclaration, boolean skipVendor, boolean equivalent) {
		String p1 = property;
		String p2 = otherDeclaration.property;
		if (skipVendor) {
			p1 = getNonVendorProperty(p1);
			p2 = getNonVendorProperty(p2);
		}
		if (otherDeclaration.isImportant() != isImportant)
			return false;
		if (!p1.equals(p2))
			return false;
		
		if (parentSelector.getMediaQueryLists() == null) {
			if (otherDeclaration.parentSelector.getMediaQueryLists() != null) {
				if (otherDeclaration.parentSelector.getMediaQueryLists().size() != 0)
					return false;
			}
		} else {				
			if (!parentSelector.getMediaQueryLists().equals(otherDeclaration.parentSelector.getMediaQueryLists()))
				return false;
		}
		// Template method design pattern
		if (equivalent)
			return valuesEquivalent(otherDeclaration);
		else
			return valuesEqual(otherDeclaration);
	}
	
	public LocationInfo getLocationInfo() {
		return this.locationInfo;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isImportant ? 1231 : 1237);
		result = prime * locationInfo.hashCode();
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result
				+ ((parentSelector == null) ? 0 : parentSelector.hashCode());
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		Declaration other = (Declaration) obj;
		if (isImportant != other.isImportant)
			return false;
		if (origin != other.origin)
			return false;
		if (parentSelector == null) {
			if (other.parentSelector != null)
				return false;
		} else if (!parentSelector.equals(other.parentSelector))
			return false;
		if (locationInfo == null) {
			if (other.locationInfo != null)
				return false;
		} else if (!locationInfo.equals(other.locationInfo))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (source != other.source)
			return false;
		return true;
	}

	public abstract Declaration clone();		
	
	public abstract String toString();
	
	/**
	 * Returns a map which maps every style property to a list of declaration values.
	 * In the case of single-valued declarations, it returns a map with one mapping: property -> the only value.
	 * In the case of multi-valued declarations, it maps every style property to a list.
	 * Each member of the list represents one layer (in multi-layered, comma-separated values).
	 * This list will have one item, if the property is not comma-separated.
	 * Each of the items of this list will be a collection of values, corresponding to the
	 * given property name.
	 * for instance, for property <code>font: bold 10pt Tahoma, Arial</code>,
	 * calling this method like <code>getPropertyToValuesMap("font-familty")</code> will
	 * return a list with one item, which is a collection containing "Tahoma" and "Arial".
	 * @return
	 */
	protected abstract Map<String, ?> getPropertyToValuesMap();

	public abstract Collection<String> getStyleProperties();
	
	public int getDeclarationNumber() {
		return this.parentSelector.getDeclarationNumber(this);
	}

	public abstract Iterable<DeclarationValue> getDeclarationValues();
	
	public abstract int getNumberOfValueLayers();

	public abstract Collection<DeclarationValue> getDeclarationValuesForStyleProperty(String styleProperty, int forLayer);
	
	public Collection<DeclarationValue> getDeclarationValuesForStyleProperty(PropertyAndLayer propertyAndLayer) {
		return getDeclarationValuesForStyleProperty(propertyAndLayer.getPropertyName(), propertyAndLayer.getPropertyLayer());
	}
	
	public abstract Set<PropertyAndLayer> getAllSetPropertyAndLayers();
	
	public boolean isVirtualDeclarationOfAShorthand() {
		return parentShorthand != null;
	}

	public ShorthandDeclaration getParentShorthand() {
		return parentShorthand;
	}

	public void setParentShorthand(ShorthandDeclaration parentShorthand) {
		this.parentShorthand = parentShorthand;
	}
	
	
		
}
