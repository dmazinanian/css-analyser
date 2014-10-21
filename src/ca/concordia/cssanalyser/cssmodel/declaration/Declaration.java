package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.csshelper.NamedColorsHelper;
import ca.concordia.cssanalyser.cssmodel.CSSOrigin;
import ca.concordia.cssanalyser.cssmodel.CSSSource;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationEquivalentValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * The representation of a single CSS declaration which consists of a
 * a property (as a String) and a list of values (as {@link DeclarationValue}s. 
 * 
 * @author Davood Mazinanian
 *
 */
public class Declaration implements Cloneable {

	protected final String property;
	protected final List<DeclarationValue> declarationValues;
	protected Selector parentSelector;
	protected final int offset;
	protected final int length;
	protected final boolean isImportant;
	protected int numberOfMissingValues;
	protected final boolean isCommaSeparatedListOfValues;
	protected CSSOrigin origin = CSSOrigin.AUTHOR;
	protected CSSSource source = CSSSource.EXTERNAL;

	
	/**
	 * Creates a new instance of Declaration and 
	 * add missing values (initial values) for properties in which user can eliminate some values.
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param offset
	 * @param length
	 * @param important
	 */
	public Declaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int offset, int length, boolean important) {
		this(propertyName, values, belongsTo, offset, length, important, true); 
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param offset
	 * @param length
	 * @param important
	 * @param addMissingValues
	 */
	public Declaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int offset, int length, boolean important, boolean addMissingValues) {
		property = propertyName.trim();
		declarationValues = values;
		parentSelector = belongsTo;
		this.offset = offset;
		this.length = length;
		isImportant = important;
		isCommaSeparatedListOfValues = isCommaSeparated(property);
		
		if (addMissingValues)
			addMissingValues();
	}
	
	/**
	 * Gets a property name (as String) and 
	 * determines whether the property can have a list of 
	 * comma-separated values (like CSS3 background, font, etc.)
	 * @param property
	 * @return
	 */
	public static boolean isCommaSeparated(String property) {
		switch (property) {
		case "font-family":
		case "font": // ?
		case "background":
		case "background-clip":
		case "background-origin":
		case "background-position":
		case "background-image":
		case "background-repeat":
		case "background-attachment":
		case "box-shadow":
		case "transition":
		case "transition-delay":
		case "transition-duration":
		case "transition-property":
		case "transition-timing-function":
		case "overflow-style":
		case "animation":
		case "src": // for @font-face
			return true;
		}
		return false;
	}

	/**
	 * This method adds missing values to the different properties
	 * which can have more than one value (like background-position).
	 */
	protected void addMissingValues() {
		
		// Only multi-valued and non-shorthand declarations are handled here.
		// Shorthands are handled in ShorthandDeclaration class
		
		if (declarationValues == null || declarationValues.size() == 0)
			return;

		switch (getNonVendorProperty(property)) {
		
		case "background-position": 
			//http://www.w3.org/TR/css3-background/#the-background-position
			switch (declarationValues.size()) {
			case 1:
				if ("inherit".equals(declarationValues.get(0).getValue())) {
					
				} else {
					if (declarationValues.get(0).isKeyword())
						addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
					else if (declarationValues.get(0).getValue().endsWith("%"))
						addMissingValue(new DeclarationEquivalentValue("50%", "50%", ValueType.LENGTH), 1);
				}
				break;
			// TODO add four-valued background-position
			}
			break;
			
		case "background-size":
			//http://www.w3.org/TR/css3-background/#the-background-size
			if (declarationValues.size() == 1) {
				String val = declarationValues.get(0).getValue();
				if (!("cover".equals(val) || "contain".equals(val) || "inherit".equals(val))) {
					addMissingValue(new DeclarationValue("auto", ValueType.LENGTH), 1);
				}
			}
			break;
			
		case "border-top-left-radius":
		case "border-top-right-radius":
		case "border-bottom-right-radius":
		case "border-bottom-left-radius":
			// http://www.w3.org/TR/css3-background/
			if (declarationValues.size() == 1) {
				String val = declarationValues.get(0).getValue();
				addMissingValue(new DeclarationValue(val, ValueType.LENGTH), 1);
			}
			break;
		
		
				
		case "transform-origin":
			// http://www.w3.org/TR/css3-transforms
			if (declarationValues.size() == 1) {
				addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
				addMissingValue(new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH), 2);
			} else if (declarationValues.size() == 2) {
				addMissingValue(new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH), 2);
			}
			break;
		 
		case "perspective-origin":
			// http://www.w3.org/TR/css3-transforms/
			if (declarationValues.size() == 1)
				addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
			break;
		case "border-spacing":
			if (declarationValues.size() == 1)
				addMissingValue(declarationValues.get(0).clone(), 1);
			break;
			
		case "text-shadow":
		case "box-shadow":
			/* 
			 * They are comma separated
			 * http://www.w3.org/TR/css3-background/#box-shadow
			 * http://www.w3.org/TR/2013/CR-css-text-decor-3-20130801/#text-shadow-property
			 * They are the same, except for "inset" keyword and
			 * spread value (fourth numeric value) which are not allowed for text-shadow 
			 */ 
			List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
			DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
			allValues.add(sentinel);
			
			int currentLayerStartIndex = 0;
			int totalAddedMissingValues = 0;
			
			for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
				DeclarationValue currentValue = allValues.get(currentValueIndex);
				
				if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
					DeclarationValue inset = null,
									 //hOffset = null,
							 		 vOffset = null,
							 		 blurRadius = null,
							 		 spreadDistance = null,
							 		 color = null;
					int numberOfLengths = 0;
					for (int currentLayerValueIndex = currentLayerStartIndex; currentLayerValueIndex < currentValueIndex; currentLayerValueIndex++) {
						DeclarationValue currentLayerCurrentValue = allValues.get(currentLayerValueIndex);
						switch (currentLayerCurrentValue.getType()) {
						case COLOR:
							color = currentLayerCurrentValue;
							break;
						case IDENT:
							if ("inset".equals(currentLayerCurrentValue.getValue()))
								inset = currentLayerCurrentValue;
							else if ("none".equals(currentLayerCurrentValue.getValue()))
								return;
							break;
						case LENGTH:
							numberOfLengths++;
							switch (numberOfLengths) {
							case 1:
								//hOffset = currentLayerCurrentValue;
								break;
							case 2:
								vOffset = currentLayerCurrentValue;
								break;
							case 3:
								blurRadius = currentLayerCurrentValue;
								break;
							case 4:
								spreadDistance = currentLayerCurrentValue;
							}
						default:
						}
					}
					
					int missingValueOffset = totalAddedMissingValues;
					
					int vOffsetPosition = 1, blurPosition = 2, distancePosition = 3, colorPosition = 3;
					if ("box-shadow".equals(getNonVendorProperty(property))) {
						colorPosition++;
						if (inset != null) {
							vOffsetPosition++;
							colorPosition++;
							blurPosition++;
							distancePosition++;
						}
					}
					
					if (vOffset == null) {
						vOffset = new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH);
						addMissingValue(vOffset, vOffsetPosition);
						totalAddedMissingValues++;
					}
					
					if (blurRadius == null) {
						blurRadius = new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH);
						addMissingValue(blurRadius, blurPosition);
						totalAddedMissingValues++;
					}
					
					if ("box-shadow".equals(property) && spreadDistance == null) {
						spreadDistance = new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH);
						addMissingValue(spreadDistance, distancePosition);
						totalAddedMissingValues++;
					}
					
					if (color == null) {
						color = new DeclarationValue("currentColor", ValueType.COLOR);
						addMissingValue(color, currentLayerStartIndex + missingValueOffset + colorPosition);
						totalAddedMissingValues++;
					}
					
					currentLayerStartIndex = currentValueIndex + 1;
					
					//TODO: Add individual
				}
				
				
			}

			break;
			
		
		// TODO: Not supported yet.
		case "animation":
			break;
			
		}
		
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
	
	/**
	 * Returns the number of missing values this declaration have
	 * @return
	 */
	public int getNumberOfMissingValues() {
		return numberOfMissingValues;
	}

	/**
	 * Returns true if the declaration is declared with !important
	 * @return
	 */
	public boolean isImportant() {
		return isImportant;
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
	}

	/**
	 * Returns the name of the property of this declaration
	 * @return
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Returns a list of values for current declaration
	 * @return
	 */
	public List<DeclarationValue> getRealValues() {
		return declarationValues;
	}
	
	
	/**
	 * Add a value to this declaration
	 * @param value
	 */
	public void addValue(DeclarationValue value) {
		if (value.isAMissingValue())
			numberOfMissingValues++;
		declarationValues.add(value);
	}
	
	/**
	 * Adds a missing value (a value which is missing from the 
	 * real declaration in the file, but is implied in the W3C 
	 * recommendations.) <br />
	 * Note that it calls {@link DeclarationValue#setIsAMissingValue(true)}
	 * for given declaration.
	 * @param {@link DeclarationValue} to be added
	 * @param position The position in the value list to which 
	 * this value should be added (zero based)
	 * @param
	 */
	public void addMissingValue(DeclarationValue value, int position) {
		value.setIsAMissingValue(true);
		numberOfMissingValues++;
		declarationValues.add(position, value);
	}

	/**
	 * checks whether two declaration have a set of identical or equivalent values, based 
	 * on the <code>checkEquivalent</code> parameter.
	 * @param otherDeclaration Declaration to be checked with
	 * @param onlyCheckEquality If true, only the equality would be checked. 
	 * If this parameter is true, the method would only rely on {@link DeclarationValue.euqals()}.
	 * Otherwise, if values are of type {@link DeclarationEquivalentValue}, it uses
	 * their {@link DeclarationEquivalentValue#equivalent()} methods to check their equivalency.
	 * In this case, this method also considers missing values. 
	 * @return True if both declarations have identical  set of values,
	 * (or equivalent set of values, based on <code>checkEquivalent</code> parameter) that is:
	 * <ol>
	 * 	<li>The number of values for both are the same,</li>
	 * 	<li>For every value in this declaration, there must be a value in other declaration
	 * 		which is either equivalent or identical, based on <code>checkEquivalent</code> parameter.</li>
	 * </ol>
	 */
	// TODO: This method has not well implemented ??
	private boolean valuesEquivalent(Declaration otherDeclaration, boolean onlyCheckEquality) {
		
		if (declarationValues.size() != otherDeclaration.declarationValues.size()) 
			return false;
		/* 
		 * In most cases, we don't consider the order of values. However, sometimes
		 * we need them to be considered, like when we are using numeric values. 
		 * For example, for background-position, we read: 
		 * "Note that a pair of keywords can be reordered while a combination of keyword and length 
		 * or percentage cannot. So ‘center left’ is valid while ‘50% left’ is not."
		 * <http://www.w3.org/TR/css3-background/#the-background-position>
		 * In general this happens for a limited list of properties, which have more than
		 * one value, and they must have more than one value which is not a keyword. 
		 * So first we find the non-keyword values in the declaration.
		 * 
		 */
		
		List<DeclarationValue> allValues = getRealValues(); 
		List<DeclarationValue> otherAllValues = otherDeclaration.getRealValues(); 
	
		if (allValues.size() != otherAllValues.size() ||
				(onlyCheckEquality && numberOfMissingValues != otherDeclaration.numberOfMissingValues))
			return false;
				
		int numberOfValuesForWhichOrderIsImportant = 0;
		for (DeclarationValue v : allValues)
			if (!v.isKeyword() || "inherit".equals(v.getValue()) || "none".equals(v.getValue()) || NamedColorsHelper.getRGBAColor(v.getValue()) != null)
				numberOfValuesForWhichOrderIsImportant++;
		
		boolean[] checkedValues = new boolean[allValues.size()];
		
		for (int i = 0; i < allValues.size(); i++) {
			
			DeclarationValue currentValue = allValues.get(i);
			if (onlyCheckEquality && currentValue.isAMissingValue())
				continue;
			
			boolean orderIsNotImportant = currentValue.isKeyword() || numberOfValuesForWhichOrderIsImportant == 1;
			if (orderIsNotImportant) {
				boolean valueFound = false;
				for (int k = 0; k < otherAllValues.size(); k++) {
					
					if (checkedValues[k])
						continue;
					
					DeclarationValue checkingValue = otherAllValues.get(k);
					
					if (checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
						continue;
					
					if ((!onlyCheckEquality && currentValue.equivalent(checkingValue)) ||
						(onlyCheckEquality && currentValue.equals(checkingValue))) {
						/*
						 * Removing the checking value is necessary for special cases like
						 * background-position: 0px 0px VS background-position: 0px 10px
						 */
						checkedValues[k] = true;
						valueFound = true;
						break;
					}
				}
				
				if (!valueFound)
					return false;

			} else {
				
				// Non-keyword values should appear at the same position in the other declaration
				DeclarationValue checkingValue = otherAllValues.get(i);

				if (checkedValues[i] || checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
					return false;
				
				if ((!onlyCheckEquality && currentValue.equivalent(checkingValue)) ||
						(onlyCheckEquality && currentValue.equals(checkingValue)))
					checkedValues[i] = true;
				else
					return false;

			}
		}

		return true;
	}

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
		return valuesEquivalent(otherDeclaration, !equivalent);
	}
	
	/**
	 * Returns the offset in the source CSS file
	 * @return
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the length of the declaration in the source CSS file
	 * @return
	 */
	public int getLength() {
		return length;
	}
	

	@Override
	public String toString() {
		StringBuilder valueString = new StringBuilder("");
		for (int i = 0; i < declarationValues.size(); i++) {
			DeclarationValue v = declarationValues.get(i);
			if (v.isAMissingValue())
				continue;
			boolean addSpace = true;
			if (addSpace) {
				// Find the next value which is not missing. If it is a comma, don't add space to get "a, b" style values. 
				int k = i;
				while (++k < declarationValues.size()) {
					DeclarationValue tv = declarationValues.get(k);
					if (!tv.isAMissingValue()) {
						addSpace = tv.getType() != ValueType.SEPARATOR;
						break;
					}
				}
			}  
			valueString.append(v + (addSpace ? " " : ""));
		}
		
		String toReturn = valueString.toString();
		if (toReturn.length() > 0)
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		
		return String.format("%s: %s", property, toReturn);
	}

	int hashCode = -1;
	@Override
	public int hashCode() {
		// Only calculate the hashCode once
		if (hashCode == -1) {
			final int prime = 31;
			int result = 1;
			result = prime * result + length;
			result = prime
					* result
					+ ((declarationValues == null) ? 0 : declarationValues
							.hashCode());
			result = prime * result + (isCommaSeparatedListOfValues ? 1231 : 1237);
			result = prime * result + (isImportant ? 1231 : 1237);
			result = prime * result + offset;
			result = prime * result + numberOfMissingValues;
			result = prime * result
					+ ((parentSelector == null) ? 0 : parentSelector.hashCode());
			result = prime * result
					+ ((property == null) ? 0 : property.hashCode());
			hashCode = result;
		}
		return hashCode;
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
		if (offset != other.offset)
			return false;
		if (length != other.length)
			return false;
		if (isCommaSeparatedListOfValues != other.isCommaSeparatedListOfValues)
			return false;
		if (isImportant != other.isImportant)
			return false;
		if (numberOfMissingValues != other.numberOfMissingValues)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (parentSelector == null) {
			if (other.parentSelector != null)
				return false;
		} else if (!parentSelector.equals(other.parentSelector))
			return false;
		if (declarationValues == null) {
			if (other.declarationValues != null)
				return false;
		} else if (!declarationValues.equals(other.declarationValues))
			return false;
		return true;
	}


	@Override
	public Declaration clone() {
		List<DeclarationValue> values = new ArrayList<>();
		for (DeclarationValue v : declarationValues) {
			values.add(v.clone());
		}
		return DeclarationFactory.getDeclaration(property, values, parentSelector, offset, length, isImportant, false);
		//return new Declaration(property, values, parentSelector, lineNumber, colNumber, isImportant, false);
	}
}
