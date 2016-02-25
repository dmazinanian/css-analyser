package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;
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
	
	protected final boolean isCommaSeparatedListOfValues;

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
	private static final Set<String> muliValuedProperties = new HashSet<>();
	
	static {
		initializeMultiValuedProperties();
	}

	public MultiValuedDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, boolean important, boolean addMissingValues, LocationInfo location) {
		super(propertyName, belongsTo, important, location);
		this.declarationValues = values;
		this.stylePropertyToDeclarationValueMap = new HashMap<>();
		this.isCommaSeparatedListOfValues = isCommaSeparated(property);
		if (addMissingValues)
			addMissingValues();
	}
	
	private static void initializeMultiValuedProperties() {
		String[] properties = new String[] { 
				"background-clip",
				"background-origin",
				"background-size",
				"background-position",
				"background-image",
				"background-repeat",
				"background-attachment",
				"border-top-left-radius",
				"border-top-right-radius",
				"border-bottom-right-radius",
				"border-bottom-left-radius",
				"transform-origin",
				"transition-property",
				"transition-timing-function",
				"transition-delay",
				"transform",
				"transition-duration",
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
			case "background-size":
			case "background-position":
			case "background-image":
			case "background-repeat":
			case "background-attachment":
			case "box-shadow":
			case "text-shadow":
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
				
				List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
				DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
				allValues.add(sentinel);
				
				List<DeclarationValue> currentLayerValues = new ArrayList<>();
				int currentLayerIndex = 0;
				
				for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
					DeclarationValue currentValue = allValues.get(currentValueIndex);
					if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
						currentLayerIndex++;
						
						
						final String BACKGROUND_POSITION_LEFT = "background-position-left";
						final String BACKGROUND_POSITION_TOP = "background-position-top";
			
						DeclarationValue firstValue = currentLayerValues.get(0);
						DeclarationValue secondValue = null;
						
						// TODO add four-valued background-position
						if (currentLayerValues.size() == 1) {
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
							secondValue = currentLayerValues.get(1);
						}
						
						
						if ("top".equals(firstValue.getValue()) || "bottom".equals(firstValue.getValue()) ||
							"left".equals(secondValue.getValue()) || "right".equals(secondValue.getValue())) {
							assignStylePropertyToValue(BACKGROUND_POSITION_TOP, currentLayerIndex, firstValue, false);
							assignStylePropertyToValue(BACKGROUND_POSITION_LEFT, currentLayerIndex, secondValue, false);
						} else {
							assignStylePropertyToValue(BACKGROUND_POSITION_LEFT, currentLayerIndex, firstValue, false);
							assignStylePropertyToValue(BACKGROUND_POSITION_TOP, currentLayerIndex, secondValue, false);
						}
						
						currentLayerValues.clear();
						
					} else {
						currentLayerValues.add(currentValue);
					}
				}
				
				break;
				
			}
			case "background-size": {
				//http://www.w3.org/TR/css3-background/#the-background-size
				
				List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
				DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
				allValues.add(sentinel);
				
				List<DeclarationValue> currentLayerValues = new ArrayList<>();
				int currentLayerIndex = 0;
				
				for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
					DeclarationValue currentValue = allValues.get(currentValueIndex);
					if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
						currentLayerIndex++;
				
						final String WIDTH = "background-size-width";
						final String HEIGHT = "background-size-height";
						DeclarationValue firstValue = currentLayerValues.get(0);
						assignStylePropertyToValue(WIDTH, currentLayerIndex, firstValue, false);
						DeclarationValue secondValue = null;
						if (currentLayerValues.size() == 1) {
							String val = firstValue.getValue();
							if (!("cover".equals(val) || "contain".equals(val) || "inherit".equals(val) || "auto".equals(val))) {
								secondValue = new DeclarationValue("auto", ValueType.LENGTH);
								assignStylePropertyToValue(HEIGHT, currentLayerIndex, secondValue, false);
								addMissingValue(secondValue, 1);
							} else {
								secondValue = firstValue.clone();
								assignStylePropertyToValue(HEIGHT, currentLayerIndex, secondValue, false);
								// There if no second value
							}
						} else {
							secondValue = currentLayerValues.get(1);
							assignStylePropertyToValue(HEIGHT, currentLayerIndex, secondValue, false);
						}
						
						currentLayerValues.clear();
					} else {
						currentLayerValues.add(currentValue);
					}
				}
					
				break;
			}
			case "background-clip":
			case "background-origin":
			case "background-image":
			case "background-repeat":
			case "background-attachment": {
				int currentLayer = 0;
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR) {
						assignStylePropertyToValue(getNonVendorProperty(getNonHackedProperty(property)), ++currentLayer, value, true);
					}
				}
				break;
			}
			case "border-top-left-radius":
			case "border-top-right-radius":
			case "border-bottom-right-radius":
			case "border-bottom-left-radius": {
				final String HRADIUS = property + "-horizontal";
				final String VRADIUS = property + "-vertical";
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
								else if ("none".equals(currentLayerCurrentValue.getValue())) {
											 hOffset = new DeclarationValue("0", ValueType.LENGTH);
											 vOffset = new DeclarationValue("0", ValueType.LENGTH);
									 		 blurRadius = new DeclarationValue("0", ValueType.LENGTH);
									 		 spreadDistance = new DeclarationValue("0", ValueType.LENGTH);
									 		 color = new DeclarationEquivalentValue("transparent", "rgba(0, 0, 0, 0)", ValueType.COLOR);
								}
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
			
			case "content":
			case "font-family": {
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue(property, 1, value, false);
					/*else
						assignStylePropertyToValue(property + "-comma", 1, value, true);*/
				}
				break;
			}
			
			case "transition-property":
			case "transition-duration":
			case "transition-timing-function":
			case "transition-delay": {
				for (DeclarationValue value : declarationValues) {
					if (value.getType() != ValueType.SEPARATOR)
						assignStylePropertyToValue(property, 1, value, false);
//					else
//						assignStylePropertyToValue(property + "-comma", 1, value, false);
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
				
				if (declarationValues.size() == 1 && "none".equals(declarationValues.get(0).getValue())) {
					assignStylePropertyToValue(LEFTQ, 1, declarationValues.get(0), true);
					assignStylePropertyToValue(RIGHTQ, 1, declarationValues.get(0), true);
				} else if (declarationValues.size() % 2 == 0) {
					for (int i = 0; i < declarationValues.size(); i += 2) {
						assignStylePropertyToValue(LEFTQ, (i / 2) + 1, declarationValues.get(i), true);
						assignStylePropertyToValue(RIGHTQ, (i / 2) + 1, declarationValues.get(i + 1), true);
					}
				}
				else {
					throw new RuntimeException("'quotes' property should have even number of variables");
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
		
		value.setCorrespondingStyleProperty(styleProperty, layer);
		
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
			result = prime * result + locationInfo.hashCode();
			result = prime
					* result
					+ ((declarationValues == null) ? 0 : declarationValues
							.hashCode());
			result = prime * result + (isCommaSeparatedListOfValues ? 1231 : 1237);
			result = prime * result + (isImportant ? 1231 : 1237);
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
		if (locationInfo == null) {
			if (other.locationInfo != null)
				return false;
		} else if (!locationInfo.equals(other.locationInfo))
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
		return new MultiValuedDeclaration(property, values, parentSelector, isImportant, true, locationInfo);
	}

	@Override
	protected boolean valuesEqual(Declaration otherDeclaration) {
		
		if (!(otherDeclaration instanceof MultiValuedDeclaration))
			throw new RuntimeException("This method cannot be called on a declaration rather than MultiValuedDeclaration.");
		
		MultiValuedDeclaration otherMultiValuedDeclaration = (MultiValuedDeclaration)otherDeclaration;
		
		if (((Collection<DeclarationValue>)otherMultiValuedDeclaration.getRealValues()).size() != ((Collection<DeclarationValue>)getRealValues()).size())
			return false;
		
		for (PropertyAndLayer propertyAndLayer : getAllSetPropertyAndLayers()) {
			Collection<DeclarationValue> valuesForThisStyleProperty = getDeclarationValuesForStyleProperty(propertyAndLayer);
			if (valuesForThisStyleProperty != null) {
				if (!valuesForThisStyleProperty.equals(otherMultiValuedDeclaration.getDeclarationValuesForStyleProperty(propertyAndLayer)))
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
		
		Set<PropertyAndLayer> allSetPropertyAndLayers = getAllSetPropertyAndLayers();
		Set<PropertyAndLayer> otherAllSetPropertyAndLayers = otherMultiValuedDeclaration.getAllSetPropertyAndLayers();
		
		if (allSetPropertyAndLayers.size() != otherAllSetPropertyAndLayers.size())
			return false;
		
		for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {
			
			Collection<DeclarationValue> valuesForThisStyleProperty = getDeclarationValuesForStyleProperty(propertyAndLayer);
			Collection<DeclarationValue> valuesForOtherStyleProperty = otherMultiValuedDeclaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
			
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
		for (PropertyAndLayer propertyAndLayer : getAllSetPropertyAndLayers())
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
	@Override
	public Collection<DeclarationValue> getDeclarationValuesForStyleProperty(String styleProperty, int forLayer) {
		return stylePropertyToDeclarationValueMap.get(new PropertyAndLayer(styleProperty, forLayer));
	}

	@Override
	public Map<String, List<Collection<DeclarationValue>>> getPropertyToValuesMap() {
			
		Map<String, List<Collection<DeclarationValue>>> toReturn = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : getAllSetPropertyAndLayers()) {
			Collection<DeclarationValue> values = getDeclarationValuesForStyleProperty(propertyAndLayer);
			List<Collection<DeclarationValue>> declarationValues = toReturn.get(propertyAndLayer.getPropertyName());
			if (declarationValues == null) {
				declarationValues = new ArrayList<Collection<DeclarationValue>>();
				toReturn.put(propertyAndLayer.getPropertyName(), declarationValues);
			}
			declarationValues.set(propertyAndLayer.getPropertyLayer() - 1, values);
		}
		return toReturn;
		
	}

	protected Map<Integer, List<PropertyAndLayer>> getLayerToPropertyAndLayerMap() {
		Map<Integer, List<PropertyAndLayer>> layerToPropertyMapper = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : getAllSetPropertyAndLayers()) {
			List<PropertyAndLayer> currentList = layerToPropertyMapper.get(propertyAndLayer.getPropertyLayer());
			if (currentList == null) {
				currentList = new ArrayList<>();	
				layerToPropertyMapper.put(propertyAndLayer.getPropertyLayer(), currentList);
			}
			
			currentList.add(propertyAndLayer);
		}
		return layerToPropertyMapper;
	}

	@Override
	public Iterable<DeclarationValue> getDeclarationValues() {
		return declarationValues;
	}

	@Override
	public int getNumberOfValueLayers() {
		Set<Integer> layer = new HashSet<>();
		for (PropertyAndLayer propertyAndLayer : getAllSetPropertyAndLayers()) {
			if (propertyAndLayer.getPropertyLayer() > -1)
				layer.add(propertyAndLayer.getPropertyLayer());
		}
		return layer.size();
	}
	
	@Override
	public Set<PropertyAndLayer> getAllSetPropertyAndLayers() {
		Set<PropertyAndLayer> allSetPropertyAndLayers = new HashSet<>();
		for (DeclarationValue value : declarationValues) {
			if (value.getCorrespondingStyleProperty() != null)
				allSetPropertyAndLayers.add(new PropertyAndLayer(value.getCorrespondingStyleProperty(), value.getCorrespondingStyleLayer()));
		}
		return allSetPropertyAndLayers;
	}

}
