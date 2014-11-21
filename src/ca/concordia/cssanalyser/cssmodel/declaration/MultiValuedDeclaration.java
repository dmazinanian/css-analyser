package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationEquivalentValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

/**
 * Represents multi-valued declarations
 * @author Davood Mazinanian
 *
 */

public class MultiValuedDeclaration extends Declaration {

	protected final List<DeclarationValue> declarationValues;
	/**
	 * Each property is mapped to a Set or List of DeclarationValues.
	 * For layered (comma-separated) properties (such as background), 
	 * a layer number is also assigned to each property so we can understand the values
	 * belong to the property of which layer.
	 * For non-layered properties, the layer assigned to each property is one.
	 * We use {@link PropertyAndLayer} class for the keys to store the layer with the property name. 
	 */
	protected final Map<PropertyAndLayer, Collection<DeclarationValue>> stylePropertyToDeclarationValueMap;
	protected int numberOfMissingValues;
	
	/**
	 * Represents a style property name, 
	 * and the layer to which it belongs.
	 * The layer is for multi-layered, comma-separated values,
	 * such as in the background declaration. 
	 * @author Davood Mazinanian
	 *
	 */
	protected static class PropertyAndLayer {
		private final String propertyName;
		private final int propertyLayer;
		
		public PropertyAndLayer(String propertyName) {
			this(propertyName, 1);
		}
		
		public PropertyAndLayer(String propertyName, int propertyLayer) {
			this.propertyName = propertyName;
			this.propertyLayer = propertyLayer;
		}
		
		public String getPropertyName() { return this.propertyName; }
		public int getPropertyLayer() { return this.propertyLayer; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + propertyLayer;
			result = prime * result	+ ((propertyName == null) ? 0 : propertyName.hashCode());
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
			PropertyAndLayer other = (PropertyAndLayer) obj;
			if (propertyLayer != other.propertyLayer)
				return false;
			if (propertyName == null) {
				if (other.propertyName != null)
					return false;
			} else if (!propertyName.equals(other.propertyName))
				return false;
			return true;
		}
		
	}
	
	private static final Set<String> muliValuedProperties = new HashSet<>();
	
	static {
		initializeMultiValuedProperties();
	}

	public MultiValuedDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int offset, int length, boolean important, boolean addMissingValues) {
		super(propertyName, belongsTo, offset, length, important);
		this.declarationValues = values;
		this.stylePropertyToDeclarationValueMap = new HashMap<>();
		
		if (addMissingValues)
			addMissingValues();
	}
	
	private static void initializeMultiValuedProperties() {
		String[] properties = new String[] { 
				"background-position", 
				"background-size",
				"border-top-left-radius",
				"border-top-right-radius",
				"border-bottom-right-radius",
				"border-bottom-left-radius",
				"transform-origin",
				"transition-property",
				"transform",
				"perspective-origin",
				"border-spacing",
				"text-shadow",
				"box-shadow",
				"content",
				"font-family",
				"quotes"
		};
		for (String property : properties)
			muliValuedProperties.add(property);
	}

	public static boolean isMultiValuedProperty(String propertyName) {
		return muliValuedProperties.contains(getNonVendorProperty(getNonHackedProperty(propertyName)));
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

		switch (getNonVendorProperty(getNonHackedProperty(property))) {
	
			case "background-position": {
				//http://www.w3.org/TR/css3-background/#the-background-position
				final String BACKGROUND_POSITION_LEFT = "background-position-left";
				final String BACKGROUND_POSITION_TOP = "background-position-top";
	
				DeclarationValue firstValue = declarationValues.get(0);
				DeclarationValue secondValue = null;
				
				// TODO add four-valued background-position
				if (declarationValues.size() == 1) {
					if ("inherit".equals(firstValue.getValue())) {
						break;
					} else {
						if (firstValue.isKeyword()) {
							secondValue = new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH);
						}
						else {
							secondValue = new DeclarationEquivalentValue("50%", "50%", ValueType.LENGTH);
						}
						addMissingValue(secondValue, 1);
					}
				} else {
					secondValue = declarationValues.get(1);
				}
				
				
				if ("top".equals(firstValue.getValue()) || "bottom".equals(firstValue.getValue()) ||
					"left".equals(secondValue.getValue()) || "right".equals(secondValue.getValue())) {
					assignStylePropertyToValue(BACKGROUND_POSITION_TOP, firstValue);
					assignStylePropertyToValue(BACKGROUND_POSITION_LEFT, secondValue);
				} else {
					assignStylePropertyToValue(BACKGROUND_POSITION_LEFT, firstValue);
					assignStylePropertyToValue(BACKGROUND_POSITION_TOP, secondValue);
				}
				break;
				
			}
			case "background-size": {
				//http://www.w3.org/TR/css3-background/#the-background-size
				final String BOTH = "background-size-both";
				final String WIDTH = "background-size-width";
				final String HEIGHT = "background-size-height";
				DeclarationValue firstValue = declarationValues.get(0);
				assignStylePropertyToValue(WIDTH, firstValue);
				DeclarationValue secondValue = null;
				if (declarationValues.size() == 1) {
					String val = firstValue.getValue();
					if (!("cover".equals(val) || "contain".equals(val) || "inherit".equals(val))) {
						secondValue = new DeclarationValue("auto", ValueType.LENGTH);
						assignStylePropertyToValue(HEIGHT, secondValue);
						addMissingValue(secondValue, 1);
					} else {
						assignStylePropertyToValue(BOTH, firstValue);
						// There if no second value
					}
				} else {
					secondValue = declarationValues.get(1);
					assignStylePropertyToValue(HEIGHT, secondValue);
				}
				break;
			}
			case "border-top-left-radius":
			case "border-top-right-radius":
			case "border-bottom-right-radius":
			case "border-bottom-left-radius": {
				final String HRADIUS = "horizontal-radius";
				final String VRADIUS = "vertical-radius";
				DeclarationValue firstValue = declarationValues.get(0);
				DeclarationValue secondValue = null;
				// http://www.w3.org/TR/css3-background/
				if (declarationValues.size() == 1) {
					String val = firstValue.getValue();
					secondValue = new DeclarationValue(val, ValueType.LENGTH);
					addMissingValue(secondValue, 1);
				} else {
					secondValue = declarationValues.get(1);
				}
				assignStylePropertyToValue(HRADIUS, firstValue);
				assignStylePropertyToValue(VRADIUS, secondValue);
				break;
			}
			case "transform" : {
				String TRANSFORM = "transform";
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue(TRANSFORM, value, true);
				}
				break;
			}
			case "transform-origin": {
				final String XAXIS = "x-axis";
				final String YAXIS = "y-axis";
				final String ZAXIS = "z-axis";
				// http://www.w3.org/TR/css3-transforms
				if (declarationValues.size() == 1) {
					addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
					addMissingValue(new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH), 2);
				} else if (declarationValues.size() == 2) {
					addMissingValue(new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH), 2);
				}
				assignStylePropertyToValue(XAXIS, declarationValues.get(0)); 
				assignStylePropertyToValue(YAXIS, declarationValues.get(1)); 
				assignStylePropertyToValue(ZAXIS, declarationValues.get(2)); 
				break;
			}
			case "perspective-origin": {
				final String XAXIS = "x-axis";
				final String YAXIS = "y-axis";
				// http://www.w3.org/TR/css3-transforms/
				if (declarationValues.size() == 1)
					addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
				assignStylePropertyToValue(XAXIS, declarationValues.get(0)); 
				assignStylePropertyToValue(YAXIS, declarationValues.get(1)); 
				break;
			}
			case "border-spacing": {
				final String HSPACING = "h-spacing";
				final String VSPACING = "v-spacing";
				if (declarationValues.size() == 1)
					addMissingValue(declarationValues.get(0).clone(), 1);
				assignStylePropertyToValue(HSPACING, declarationValues.get(0));
				assignStylePropertyToValue(VSPACING, declarationValues.get(1));
				break;
			}
			case "text-shadow":
			case "box-shadow": {
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
				int currentLayerIndex = 0;
				
				for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
					DeclarationValue currentValue = allValues.get(currentValueIndex);
					
					// Find the first separator. Sentinel is used here :)
					if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
						currentLayerIndex++;
						DeclarationValue inset = null,
										 hOffset = null,
								 		 vOffset = null,
								 		 blurRadius = null,
								 		 spreadDistance = null,
								 		 color = null;
						int numberOfLengths = 0;
						// Count from current layer's start index to this separator
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
									hOffset = currentLayerCurrentValue;
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
						if ("box-shadow".equals(getNonVendorProperty(getNonHackedProperty(property)))) {
							colorPosition++;
							if (inset != null) {
								vOffsetPosition++;
								colorPosition++;
								blurPosition++;
								distancePosition++;
							}
						}
						
						if (vOffset == null) {
							vOffset = new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH);
							addMissingValue(vOffset, vOffsetPosition);
							totalAddedMissingValues++;
						}
						
						if (blurRadius == null) {
							blurRadius = new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH);
							addMissingValue(blurRadius, blurPosition);
							totalAddedMissingValues++;
						}
						
						if ("box-shadow".equals(property) && spreadDistance == null) {
							spreadDistance = new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH);
							addMissingValue(spreadDistance, distancePosition);
							totalAddedMissingValues++;
						}
						
						if (color == null) {
							color = new DeclarationValue("currentColor", ValueType.COLOR);
							addMissingValue(color, currentLayerStartIndex + missingValueOffset + colorPosition);
							totalAddedMissingValues++;
						}
						
						currentLayerStartIndex = currentValueIndex + 1;
						
						final String COLOR = "color";
						final String HOFFSET = "hoffset";
						final String VOFFSET = "voffset";
						final String BLUR = "blur";
						final String SPREAD = "spread";
						final String INSET = "inset";
						
						assignStylePropertyToValue(COLOR, currentLayerIndex, color, false);
						assignStylePropertyToValue(HOFFSET, currentLayerIndex, hOffset, false);
						assignStylePropertyToValue(VOFFSET, currentLayerIndex, vOffset, false);
						assignStylePropertyToValue(BLUR, currentLayerIndex, blurRadius, false);
						if ("box-shadow".equals(property)) {
							assignStylePropertyToValue(SPREAD, currentLayerIndex, spreadDistance, false);
							if (inset != null)
								assignStylePropertyToValue(INSET, currentLayerIndex, inset, false);
						}
					}
					
					
				}
	
				break;
			}
			
			case "content": {
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue("content", 1, value, true);
				}
				break;
			}
			
			case "font-family": {
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue("font-family", 1, value, true);
				}
				break;
			}
			
			case "transition-property": {
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue("transition-property", 1, value, false);
				}
				break;
			}
			
			case "quotes": {
				/*
				 * http://www.w3schools.com/cssref/pr_gen_quotes.asp
				 * An even number of strings. Each two of them are used for one level
				 */
				final String LEFTQ = "leftq";
				final String RIGHTQ = "rightq";
				
				if (declarationValues.size() % 2 != 0) {
					throw new RuntimeException("'quotes' property should have even number of variables");
				}
				for (int i = 0; i < declarationValues.size(); i += 2) {
					assignStylePropertyToValue(LEFTQ, (i / 2) + 1, declarationValues.get(i), true);
					assignStylePropertyToValue(RIGHTQ, (i / 2) + 1, declarationValues.get(i + 1), true);
				}
				break;
			}
			default:
				throw new NotImplementedException("Multivalued property " + property + " not handled.");
					
		}
		
	}
	
	/**
	 * Maps a styleProperty to a value.
	 * For instance, in <code>margin: 2px</code>,
	 * this function is called so margin-left, -right, -bottom and -top all map to the value of 2px.
	 * If this method is called for the first time, it adds the new value to a Set mapped to the given styleProperty.
	 * If the styleProperty already exists (i.e., the method has already been called), 
	 * it adds the new value to the existing underlying collection (either a List or Set, based on the previous call to 
	 * this method or its overload).
	 * @param styleProperty
	 * @param value
	 */
	
	protected void assignStylePropertyToValue(String styleProperty, DeclarationValue value) {
		assignStylePropertyToValue(styleProperty, value, false);
	}
	
	/**
	 * Maps a styleProperty to a value.
	 * For instance, in <code>margin: 2px</code>,
	 * this function is called so margin-left, -right, -bottom and -top all map to the value of 2px.
	 * If this method is called for the first time, it adds the new value to a Set or List mapped to the given styleProperty,
	 * based on the value of orderImportant parameter (List => orderImportant == true, otherwise, Set).
	 * If this method is called not for the first time, it ignores the value for orderImportant and adds it to the 
	 * existing underlying collection.
	 * @param styleProperty
	 * @param value
	 * @param orderImportant
	 */
	protected void assignStylePropertyToValue(String styleProperty, DeclarationValue value, boolean orderImportant) {
		
		assignStylePropertyToValue(styleProperty, 1, value, orderImportant);
		
	}
	
	protected void assignStylePropertyToValue(String styleProperty, int layer, DeclarationValue value, boolean orderImportant) {
		
		value.setCorrespondingStyleProperty(styleProperty);
		
		PropertyAndLayer propertyAndLayer = new PropertyAndLayer(styleProperty, layer);
		
		Collection<DeclarationValue> values = stylePropertyToDeclarationValueMap.get(propertyAndLayer);
		if (values == null) {
			if (orderImportant)
				values = new ArrayList<>();
			else
				values = new HashSet<>();
		}
		
		values.add(value);
		stylePropertyToDeclarationValueMap.put(propertyAndLayer, values);
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
	 * Returns the number of missing values this declaration have
	 * @return
	 */
	public int getNumberOfMissingValues() {
		return numberOfMissingValues;
	}
	
	/**
	 * Returns a list of values for current declaration.
	 * This list also includes the values which are not in the 
	 * real file, but are added later (missing values).
	 * @return
	 */
	public List<DeclarationValue> getValues() {
		return declarationValues;
	}
	
	/**
	 * Only returns the real values for this declaration
	 * (not the missing values which were added later).
	 * @return
	 */
	public Iterable<DeclarationValue> getRealValues() {
		List<DeclarationValue> realValues = new ArrayList<>();
		for (DeclarationValue value : getValues()) {
			if (!value.isAMissingValue())
				realValues.add(value);
		}
		return realValues;
	}
	
	/**
	 * Only returns the missing values for this declaration
	 * which are added later
	 * @return
	 */
	public Iterable<DeclarationValue> getMissingValues() {
		List<DeclarationValue> missingValues = new ArrayList<>();
		for (DeclarationValue value : getValues()) {
			if (value.isAMissingValue())
				missingValues.add(value);
		}
		return missingValues;
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
	
	@Override
	public String toString() {
		String toReturn = getValuesString();
		
		return String.format("%s: %s", property, toReturn);
	}

	/**
	 * Returns the string corresponding to the values
	 * @return
	 */
	protected String getValuesString() {
		
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
		
		return valueString.toString().trim();
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
		MultiValuedDeclaration other = (MultiValuedDeclaration) obj;
		if (length != other.length)
			return false;
		if (offset != other.offset)
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
		//return DeclarationFactory.getDeclaration(property, values, parentSelector, offset, length, isImportant, false);
		//return new Declaration(property, values, parentSelector, lineNumber, colNumber, isImportant, false);
		return new MultiValuedDeclaration(property, values, parentSelector, offset, length, isImportant, true);
	}

	@Override
	protected boolean valuesEqual(Declaration otherDeclaration) {
		
		if (!(otherDeclaration instanceof MultiValuedDeclaration))
			throw new RuntimeException("This method cannot be called on a declaration rather than MultiValuedDeclaration.");
		
		MultiValuedDeclaration otherMultiValuedDeclaration = (MultiValuedDeclaration)otherDeclaration;
		
		if (otherMultiValuedDeclaration.getValues().size() != getValues().size())
			return false;
		
		for (PropertyAndLayer propertyAndLayer : stylePropertyToDeclarationValueMap.keySet()) {
			Collection<DeclarationValue> valuesForThisStyleProperty = stylePropertyToDeclarationValueMap.get(propertyAndLayer);
			if (valuesForThisStyleProperty != null) {
				if (!valuesForThisStyleProperty.equals(otherMultiValuedDeclaration.stylePropertyToDeclarationValueMap.get(propertyAndLayer)))
					return false;
			} else {
				return false;
			}
		}
		
		return true;
		
	}
	
	@Override
	protected boolean valuesEquivalent(Declaration otherDeclaration) {
		
		if (!(otherDeclaration instanceof MultiValuedDeclaration))
			throw new RuntimeException("This method cannot be called on a declaration rather than MultiValuedDeclaration.");
		
		MultiValuedDeclaration otherMultiValuedDeclaration = (MultiValuedDeclaration)otherDeclaration;
		
		if (otherMultiValuedDeclaration.getValues().size() != getValues().size())
			return false;
		
		
		
		for (PropertyAndLayer propertyAndLayer : stylePropertyToDeclarationValueMap.keySet()) {
			
			Collection<DeclarationValue> valuesForThisStyleProperty = stylePropertyToDeclarationValueMap.get(propertyAndLayer);
			Collection<DeclarationValue> valuesForOtherStyleProperty = otherMultiValuedDeclaration.stylePropertyToDeclarationValueMap.get(propertyAndLayer);
			
			if (valuesForThisStyleProperty != null && valuesForOtherStyleProperty != null) {
				
				if (valuesForThisStyleProperty.size() != valuesForOtherStyleProperty.size())
					return false;
				
				/*
				 * If the underlying object is a List, we have to take care of the order.
				 * Otherwise, order is not important.  
				 */
				if (valuesForThisStyleProperty instanceof List && valuesForOtherStyleProperty instanceof List) {
					
					List<DeclarationValue> values1List = (List<DeclarationValue>)valuesForThisStyleProperty;
					List<DeclarationValue> values2List = (List<DeclarationValue>)valuesForOtherStyleProperty;
					
					for (int i = 0; i < valuesForOtherStyleProperty.size(); i++) {
						if (!values1List.get(i).equivalent(values2List.get(i))) 
							return false;
					}
					
				} else { // Order is not important
					
					List<DeclarationValue> values1List = new ArrayList<>(valuesForThisStyleProperty);
					List<DeclarationValue> values2List = new ArrayList<>(valuesForOtherStyleProperty);
					
					Set<Integer> checkedValues = new HashSet<>();
					
					for (int i = 0; i < values1List.size(); i++) {
						boolean foundEquivalent = false;
						for (int j = 0; j < values2List.size(); j++) {
							// Check if we have not already mapped this value to another one
							if (checkedValues.contains(j))
								continue;
							
							// Are the values are equivalent
							if (values1List.get(i).equivalent(values2List.get(j))) {
								foundEquivalent = true;
								checkedValues.add(j);
								break;
							}
						}
						
						if (!foundEquivalent)
							return false;
					}
					
					if (checkedValues.size() != values1List.size())
						return false;
					
				}
				
			} else {
				return false;
			}
		}
		
		return true;
		
	}
	
	/**
	 * Returns all the style properties possible for the values of this multi-valued declaration
	 * @return
	 */
	@Override
	public Collection<String> getStyleProperties() {
		Set<String> toReturn = new HashSet<>();
		for (PropertyAndLayer propertyAndLayer : stylePropertyToDeclarationValueMap.keySet())
			toReturn.add(propertyAndLayer.getPropertyName());
		return toReturn;
	}
	
	/**
	 * Returns a Collection of DeclarationValue's for the given property.
	 * For the multi-valued properties, it returns the values corresponding to the first layer.
	 * Returns null if such a DeclarationValue is not found.
	 * @param styleProperty
	 * @return
	 */
	public Collection<DeclarationValue> getDeclarationValuesForStyleProperty(String styleProperty) {
		return getDeclarationValuesForStyleProperty(styleProperty, 1);
	}
	
	/**
	 * Returns a Collection of DeclarationValue's for the given property, in the given layer.
	 * Returns null if such a DeclarationValue is not found. 
	 * @param styleProperty
	 * @return
	 */
	public Collection<DeclarationValue> getDeclarationValuesForStyleProperty(String styleProperty, int forLayer) {
		return stylePropertyToDeclarationValueMap.get(new PropertyAndLayer(styleProperty, forLayer));
	}

	@Override
	public Map<String, List<Collection<DeclarationValue>>> getPropertyToValuesMap() {
		
		//Map<Integer, List<PropertyAndLayer>> layerToPropertyMapper = getLayerToPropertyAndLayerMap();
		
		Map<String, List<Collection<DeclarationValue>>> toReturn = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : stylePropertyToDeclarationValueMap.keySet()) {
			Collection<DeclarationValue> values = stylePropertyToDeclarationValueMap.get(propertyAndLayer);
			List<Collection<DeclarationValue>> declarationValues = toReturn.get(propertyAndLayer.getPropertyName());
			if (declarationValues == null) {
				declarationValues = new ArrayList<Collection<DeclarationValue>>();
				toReturn.put(propertyAndLayer.getPropertyName(), declarationValues);
			}
			declarationValues.set(propertyAndLayer.propertyLayer - 1, values);
		}
		return toReturn;
		
	}

	protected Map<Integer, List<PropertyAndLayer>> getLayerToPropertyAndLayerMap() {
		Map<Integer, List<PropertyAndLayer>> layerToPropertyMapper = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : stylePropertyToDeclarationValueMap.keySet()) {
			List<PropertyAndLayer> currentList = layerToPropertyMapper.get(propertyAndLayer.getPropertyLayer());
			if (currentList == null) {
				currentList = new ArrayList<>();	
				layerToPropertyMapper.put(propertyAndLayer.getPropertyLayer(), currentList);
			}
			
			currentList.add(propertyAndLayer);
		}
		return layerToPropertyMapper;
	}
	 
}
