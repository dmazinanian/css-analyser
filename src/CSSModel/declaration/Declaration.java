package CSSModel.declaration;

import java.util.ArrayList;
import java.util.List;

import sun.security.util.Length;

import CSSHelper.ColorHelper;
import CSSHelper.ListStyleHelper;
import CSSHelper.NamedColors;
import CSSModel.declaration.value.DeclarationEquivalentValue;
import CSSModel.declaration.value.DeclarationValue;
import CSSModel.declaration.value.ValueType;
import CSSModel.selectors.Selector;

/**
 * The representation of a single CSS declaration which consists of a
 * list of a property (as a String) and a list of values. 
 * 
 * @author Davood Mazinanian
 *
 */
public class Declaration {

	protected final String property;
	protected final List<DeclarationValue> declarationValues;
	protected final Selector belongsToSelector;
	protected final int lineNumber;
	protected final int colNumber;
	protected final boolean isImportant;
	private final boolean commaSeparatedListOfValues; 

	
	public Declaration(String property, List<DeclarationValue> values, Selector belongsTo, boolean important) {
		this(property, values, belongsTo, -1, -1, important);
	}
	
	public Declaration(String property, DeclarationValue value, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important) {
		this(property, getDecValueListForSingleValue(value), belongsTo, fileLineNumber, fileColNumber, important);
	}
	
	private static List<DeclarationValue> getDecValueListForSingleValue(DeclarationValue val) {
		List<DeclarationValue> vals = new ArrayList<>();
		vals.add(val);
		return vals;
	}
	
	public Declaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important) {
		property = propertyName;
		declarationValues = values;
		belongsToSelector = belongsTo;
		lineNumber = fileLineNumber;
		colNumber = fileColNumber;
		isImportant = important;
		switch (propertyName) {
		case "font-family":
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
			commaSeparatedListOfValues = true;
			break;
		default:
			commaSeparatedListOfValues = false;
		}
		
		addMissingValues();
	}

	
	
	private void addMissingValues() {
		switch (property) {
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
			
			if (declarationValues.size() == 1) {
				String val = declarationValues.get(0).getValue();
				addMissingValue(new DeclarationValue(val, ValueType.LENGTH), 1);
			}
			break;
		
		case "border-radius":
		case "-webkit-border-radius":
		case "-moz-border-radius":
			//http://www.w3.org/TR/css3-background/#the-border-radius
			int slashPosition = declarationValues.indexOf(new DeclarationValue("/", ValueType.SEPARATOR));
			switch (slashPosition < 0 ? declarationValues.size() : slashPosition) {
			case 1:
				DeclarationValue v1 = declarationValues.get(0).clone(); 
				for (int i = 1; i <= 3; i++)
					addMissingValue(v1, i);
				break;
			case 2:
				v1 = declarationValues.get(0).clone(); 
				DeclarationValue v2 = declarationValues.get(1).clone(); 
				addMissingValue(v1, 2);
				addMissingValue(v2, 3);
				break;
			case 3:
				v2 = declarationValues.get(1).clone();
				addMissingValue(v2, 3);
			}

			if (slashPosition < 0)
				addMissingValue(new DeclarationValue("/", ValueType.SEPARATOR), 4);
			
			switch (declarationValues.size()) {
			case 5:
				for (int i = 5; i <= 8; i++)
					addMissingValue(new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH), i);
				break;
			case 6:
				for (int i = 6; i <= 8; i++)
					addMissingValue(declarationValues.get(5).clone(), i);
				break;
			case 7:
				addMissingValue(declarationValues.get(5).clone(), 7);
				addMissingValue(declarationValues.get(6).clone(), 8);
				break;
			case 8:
				addMissingValue(declarationValues.get(6).clone(), 8);
			}
			
			break;
				
		case "transform-origin":
		case "-ms-transform-origin":
		case "-webkit-transform-origin":
			if (declarationValues.size() == 1) {
				addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
				addMissingValue(new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH), 2);
			} else if (declarationValues.size() == 2) {
				addMissingValue(new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH), 2);
			}
			break;
		 
		case "perspective-origin":
		case "-webkit-perspective-origin":
			if (declarationValues.size() == 1)
				addMissingValue(new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH), 1);
			break;
		case "border-spacing":
			if (declarationValues.size() == 1)
				addMissingValue(declarationValues.get(0).clone(), 1);
			break;
		case "margin":
		case "padding":
		case "border-width":
		case "border-style":
		case "border-color":
			switch (declarationValues.size()) {
			case 1:
				DeclarationValue v1 = declarationValues.get(0).clone(); 
				for (int i = 1; i <= 3; i++)
					addMissingValue(v1, i);
				break;
			case 2:
				v1 = declarationValues.get(0).clone(); 
				DeclarationValue v2 = declarationValues.get(1).clone(); 
				addMissingValue(v1, 2);
				addMissingValue(v2, 3);
				break;
			case 3:
				v2 = declarationValues.get(1).clone();
				addMissingValue(v2, 3);
			}
			break;
		case "border":				
		case "border-bottom":
		case "border-left":				
		case "border-right":				
		case "border-top":				
		case "outline":
		case "column-rule":
		case "-webkit-column-rule":
		case "-moz-column-rule":
			// http://www.w3.org/TR/css3-background/#the-border-shorthands
			// http://www.w3.org/TR/css3-ui/#outline-properties (working draft)
			DeclarationValue borderColor = null,
							 borderWidth = null,
							 borderStyle = null;
			for (DeclarationValue v : declarationValues) {
				if (v.getType() == ValueType.COLOR) {
					borderColor = v;
				} else if (v.getType() == ValueType.LENGTH) {
					borderWidth = v;
				} else if (v.getType() == ValueType.IDENT) {
					if ("thin".equals(v.getValue()) || "medium".equals(v.getValue()) || "thick".equals(v.getValue()))
						borderWidth = v;
					else
						borderStyle = v;
				}
			}
			
			if (borderWidth == null) {
				borderWidth = new DeclarationValue("medium", ValueType.LENGTH);
				addMissingValue(borderWidth, 0);
			}
			
			if (borderStyle == null) {
				borderStyle = new DeclarationValue("none", ValueType.IDENT);
				addMissingValue(borderStyle, 1);
			}
			
			if (borderColor == null) {
				borderColor = new DeclarationValue("currentColor", ValueType.COLOR);
				addMissingValue(borderColor, 2);
			}
			
			break;
		case "columns":
		case "-webkit-columns":
		case "-moz-columns":
			DeclarationValue colWidth = null,
							 colCount = null;
			for (DeclarationValue v : declarationValues) {
				if (v.getType() == ValueType.INTEGER)
					colCount = v;
				else if (v.getType() == ValueType.LENGTH)
					colWidth = v;
			}
			
			if (colWidth == null) {
				colWidth = new DeclarationEquivalentValue("0", "0.0px", ValueType.LENGTH);
				addMissingValue(colWidth, 0);
			}
			
			if (colCount == null) {
				colCount = new DeclarationValue("auto", ValueType.LENGTH);
				addMissingValue(colCount, 1);
			}
			
			break;
		case "list-style":
			
			if (declarationValues.size() < 3) {
				// If some of the values are missing, we should add initial values
			
				boolean listTypeFound = false;
				boolean listPositionFound = false;
				boolean listImageFound = false;
				int numberOfNones = 0;
				for (DeclarationValue v : declarationValues) {
					if (v.getType() == ValueType.URL)
						listImageFound = true;
					else if (v.getType() == ValueType.IDENT) {
						String val = v.getValue();
						switch (val) {
						case "inside":
						case "hanging":
						case "outside":
							listPositionFound = true;
							break;
						case "inline":
							listTypeFound = true;
						case "none":
							numberOfNones++;
							break;
						case "inherit":
							break;
						default:
							if (ListStyleHelper.isStyleTypeName(val))
								listTypeFound = true;
						}
					}
				}
				
				if (declarationValues.size() == 1) {
					switch (declarationValues.get(0).getValue()) {
					case "inherit":
					case "none":
						addMissingValue(new DeclarationValue(declarationValues.get(0).getValue(), ValueType.IDENT), 1);
						addMissingValue(new DeclarationValue(declarationValues.get(0).getValue(), ValueType.IDENT), 2);
						listPositionFound = true;
						listTypeFound = true;
						listImageFound = true;
					}
					
				}
				
				if (!listPositionFound) {
					addMissingValue(new DeclarationValue("outside", ValueType.IDENT), 1);
				}
				
				if (!listTypeFound && numberOfNones <= 1) {
						addMissingValue(new DeclarationValue("none", ValueType.IDENT), 0);
						numberOfNones++;
				}
				
				if (!listImageFound && numberOfNones <= 1) {
						addMissingValue(new DeclarationValue("none", ValueType.IDENT), 2);
				}
								
			}
			break;
			
		case "transition":
		case "-webkit-transition":
			// http://www.w3.org/TR/css3-transitions/#transition-shorthand-property
			// transition is comma-separated
			
			List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
			DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
			allValues.add(sentinel);
			
			int currentLayerStartIndex = 0;
			int totalAddedMissingValues = 0;
			
			for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
				DeclarationValue currentValue = allValues.get(currentValueIndex);
				
				if (currentValue.getType() == ValueType.OPERATOR && ",".equals(currentValue.getValue())) {
					DeclarationValue transitionProperty = null,
							 		 transitionTimingFunction = null,
							 		 transitionDuration = null,
							 		 transitionDelay = null;
					
					for (int currentLayerValueIndex = currentLayerStartIndex; currentLayerValueIndex < currentValueIndex; currentLayerValueIndex++) {
						DeclarationValue currentLayerCurrentValue = allValues.get(currentLayerValueIndex);
						switch (currentValue.getType()) {
						case TIME:
							// First time value is for duration, second for delay, based on W3C
							if (transitionDuration == null)
								transitionDuration = currentLayerCurrentValue;
							else
								transitionDelay = currentLayerCurrentValue;
							break;
						case IDENT:
							switch (currentLayerCurrentValue.getValue()) {
							// In general, every transition IDENT has a steps or cubic-bezier equivalence.
							case "ease":
							case "linear":
							case "ease-in":
							case "ease-out":
							case "ease-in-out":
							case "step-start":
							case "step-end":
								transitionTimingFunction = currentLayerCurrentValue;
								break;
							default:
								if (currentLayerCurrentValue.getValue().startsWith("steps") || 
									currentLayerCurrentValue.getValue().startsWith("cubic-bezier")) {
									transitionTimingFunction = currentLayerCurrentValue;
								} else {
									transitionProperty = currentLayerCurrentValue;
								}
							}
							break;
							default:
								// Do nothing
						}
					}
					
					int missingValueOffset = totalAddedMissingValues; 
					if (transitionProperty == null) {
						transitionProperty = new DeclarationValue("all", ValueType.IDENT);
						/*
						 * Maybe we have already added some missing values to the previous layer.
						 * So, the new layer should start from a new point. the missingValueOffset
						 * variable plays this role. 
						 */
						addMissingValue(transitionProperty, currentLayerStartIndex + missingValueOffset + 0);
						totalAddedMissingValues++;
					}
					
					if (transitionDuration == null) {
						transitionDuration = new DeclarationValue("0s", ValueType.TIME);
						addMissingValue(transitionDuration, currentLayerStartIndex + missingValueOffset + 1);
						totalAddedMissingValues++;
					}
					
					if (transitionTimingFunction == null) {
						transitionDuration = new DeclarationValue("ease", ValueType.IDENT);
						addMissingValue(transitionTimingFunction, currentLayerStartIndex + missingValueOffset + 2);
						totalAddedMissingValues++;
					}
					
					if (transitionDelay == null) {
						transitionDelay = new DeclarationValue("0s", ValueType.TIME);
						addMissingValue(transitionDelay, currentLayerStartIndex + missingValueOffset + 3);
						totalAddedMissingValues++;
					}
						
				}
				currentLayerStartIndex = currentValueIndex + 1;
			}
				
			break;
			
		// TODO
		case "text-shadow":
		case "box-shadow":
			break;
			
		case "background":
			// Background is comma separated.

			allValues = new ArrayList<>(declarationValues);
			sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
			allValues.add(sentinel);
			
			currentLayerStartIndex = 0;
			/* 
			 * [ <bg-layer> , ]* <final-bg-layer>
					<bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} 
					<final-bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} || <'background-color'>
			 */
			totalAddedMissingValues = 0;
			for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
				DeclarationValue currentValue = allValues.get(currentValueIndex);
				if (currentValue.getType() == ValueType.SEPARATOR) {
					boolean isLastLayer = currentValueIndex == declarationValues.size();
					DeclarationValue bgImage = null, 
									 bgRepeat = null, 
									 bgAttachement = null,
									 bgOrigin = null, 
									 bgClip = null,
									 bgPositionX = null,
									 bgPositionY = null,
									 bgSizeW = null,
									 bgSizeH = null,
									 bgColor = null;
					
					int numberOfBoxes = 0;
					List<Integer> lenghtValuesIndices = new ArrayList<>();
					boolean slashFound = false;

					for (int currentLayerValueIndex = currentLayerStartIndex; currentLayerValueIndex < currentValueIndex; currentLayerValueIndex++) {
						// Current layer indices are from currentLayerValueIndex to currentValueIndex
						DeclarationValue currentLayerCurrentValue = allValues.get(currentLayerValueIndex);
						switch (currentLayerCurrentValue.getType()) {
						case URL:
							bgImage = currentLayerCurrentValue;
							break;
						case COLOR:
							if (isLastLayer)
								bgColor = currentLayerCurrentValue;
							break;
						case IDENT:
							switch (currentLayerCurrentValue.getValue()) {
							case "repeat-x":
							case "repeat-y":
							case "repeat":
							case "space":
							case "round":
							case "no-repeat":
								bgRepeat = currentLayerCurrentValue;
								break;
							case "fixed":
							case "local":
							case "scroll":
								bgAttachement = currentLayerCurrentValue;
								break;
							case "padding-box":
							case "border-box":
							case "content-box":
								numberOfBoxes++;
								if (numberOfBoxes >= 1)
									bgOrigin = currentLayerCurrentValue;
								if (numberOfBoxes == 2)
									bgClip = currentLayerCurrentValue;
							}
							break;
						case LENGTH:
							lenghtValuesIndices.add(currentLayerValueIndex);
							break;
						case OPERATOR:
							if ("/".equals(currentLayerCurrentValue.getValue())) {
								slashFound = true;
								DeclarationValue tempValue;
								if (currentLayerValueIndex - 1 >= currentLayerStartIndex) {
									tempValue = allValues.get(currentLayerValueIndex - 1);
									if (tempValue.getType() != ValueType.LENGTH) {
										tempValue = null;
									} else {
										bgPositionY = tempValue;
										if (currentLayerValueIndex - 2 >= 0) {
											tempValue = allValues.get(currentLayerValueIndex - 2);
											if (tempValue.getType() != ValueType.LENGTH) {
												tempValue = null;
											} else {
												bgPositionX = tempValue;
											}
										} else {
											bgPositionX = bgPositionY;
											bgPositionY = null;
										}

										if (currentLayerValueIndex + 1 < currentValueIndex) {
											tempValue = allValues.get(currentLayerValueIndex + 1);
											if (tempValue.getType() != ValueType.LENGTH) {
												tempValue = null;
											} else {
												bgSizeW = tempValue;
												if (currentLayerValueIndex + 2 < currentValueIndex) {
													tempValue = allValues.get(currentLayerValueIndex + 2);
													if (tempValue.getType() != ValueType.LENGTH) {
														tempValue = null;
													} else {
														bgSizeH = tempValue;
													}
												}
											}
										}
									}
								}
							}
							break;
						default:
							// Do nothing
						}
					}
					int missingValueOffset = totalAddedMissingValues; 
					if (bgImage == null) {
						bgImage = new DeclarationEquivalentValue("url('')", "none", ValueType.URL);
						addMissingValue(bgImage, currentLayerStartIndex + missingValueOffset + 0); // For better readability!
						totalAddedMissingValues++;
					}
					if (bgPositionX == null) {
						if (lenghtValuesIndices.size() == 0) {
							bgPositionX = new DeclarationEquivalentValue("center", "50%", ValueType.LENGTH);
							bgPositionY = bgPositionX.clone();
							addMissingValue(bgPositionX, currentLayerStartIndex + missingValueOffset + 1);
							addMissingValue(bgPositionY, currentLayerStartIndex + missingValueOffset + 2);
							totalAddedMissingValues += 2;
						} else if (lenghtValuesIndices.size() == 1) {
							bgPositionX = allValues.get(lenghtValuesIndices.get(0));
							bgPositionY = bgPositionX.clone();
							addMissingValue(bgPositionY, currentLayerStartIndex + missingValueOffset + 2);
							totalAddedMissingValues++;
						} else if (lenghtValuesIndices.size() == 2) {
							bgPositionX = allValues.get(lenghtValuesIndices.get(0));
							bgPositionY = allValues.get(lenghtValuesIndices.get(1));
						}
					} else {
						if (bgPositionY == null) {
							bgPositionY = bgPositionX.clone();
							addMissingValue(bgPositionY, currentLayerStartIndex + missingValueOffset + 2);
							totalAddedMissingValues++;
						}
					}

					if (!slashFound)
						addMissingValue(new DeclarationValue("/", ValueType.OPERATOR), currentLayerStartIndex + missingValueOffset + 3);

					if (bgSizeW == null) {
						bgSizeW = new DeclarationValue("auto", ValueType.IDENT);
						bgSizeH = bgSizeW.clone();
						addMissingValue(bgSizeW, currentLayerStartIndex + missingValueOffset + 4);
						addMissingValue(bgSizeH, currentLayerStartIndex + missingValueOffset + 5);
						totalAddedMissingValues += 2;
					} else {
						if (bgSizeH == null) {
							bgSizeH = bgSizeW.clone();
							addMissingValue(bgSizeH, currentLayerStartIndex + missingValueOffset + 5);
							totalAddedMissingValues++;
						}
					}

					if (bgRepeat == null) {
						bgRepeat = new DeclarationValue("repeat", ValueType.IDENT);
						addMissingValue(bgRepeat, currentLayerStartIndex + missingValueOffset + 6);
						totalAddedMissingValues++;
					}

					if (bgAttachement == null) {
						bgAttachement = new DeclarationValue("scroll", ValueType.IDENT);
						addMissingValue(bgAttachement, currentLayerStartIndex + missingValueOffset + 7);
						totalAddedMissingValues++;
					}

					if (bgOrigin == null) {
						bgOrigin = new DeclarationValue("padding-box", ValueType.IDENT);
						addMissingValue(bgOrigin, currentLayerStartIndex + missingValueOffset + 8);
						totalAddedMissingValues++;
					}

					if (bgClip == null) {
						bgClip= new DeclarationValue("border-box", ValueType.IDENT);
						addMissingValue(bgClip, currentLayerStartIndex + missingValueOffset + 9);
						totalAddedMissingValues++;
					}

					if (isLastLayer && bgColor == null) {
						bgColor = new DeclarationEquivalentValue("transparent", "rgba(0, 0, 0, 0.0", ValueType.COLOR);
						addMissingValue(bgColor, currentLayerStartIndex + missingValueOffset + 10);
					}
							

					currentLayerStartIndex = currentValueIndex + 1;
				} else {
					
				}
			}
			
			break;
		}
		
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
		return belongsToSelector;
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
		declarationValues.add(value);
	}
	
	/**
	 * Adds a missing value (a value which is missing from the 
	 * real declaration in the file, but is implied in the W3C 
	 * recommendations.) <br />
	 * @param {@link DeclarationValue} to be added
	 * @param position The position in the value list to which 
	 * this value should be added (zero based)
	 * @param
	 */
	public void addMissingValue(DeclarationValue value, int position) {
		value.isAMissingValue(true);
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
		
		List<DeclarationValue> allValues = new ArrayList<>(getRealValues()); 
		List<DeclarationValue> otherAllValues = new ArrayList<>(otherDeclaration.getRealValues()); 
		// Copy, because we are going to modify it during the process
	
		if (allValues.size() != otherAllValues.size())
			return false;
		
		int numberOfMissingValues1 = 0;
		int numberOfMissingValues2 = 0;
		if (onlyCheckEquality) {
			for (DeclarationValue v : allValues)
				if (v.isAMissingValue()) numberOfMissingValues1++;
			for (DeclarationValue v : otherAllValues)
				if (v.isAMissingValue()) numberOfMissingValues2++;
			
			if (numberOfMissingValues2 < numberOfMissingValues1) {
				List<DeclarationValue> temp = allValues;
				allValues = otherAllValues;
				otherAllValues = temp;
			}
		}
		
		int numberOfValuesForWhichOrderIsImportant = 0;
		for (DeclarationValue v : allValues)
			if (!v.isKeyword() || "inherit".equals(v.getValue()) || "none".equals(v.getValue()) || NamedColors.getRGBAColor(v.getValue()) != null)
				numberOfValuesForWhichOrderIsImportant++;
		
		for (int i = 0; i < allValues.size(); i++) {
			
			DeclarationValue currentValue = allValues.get(i);
			if (onlyCheckEquality && currentValue.isAMissingValue())
				continue;
			
			boolean orderIsNotImportant = currentValue.isKeyword() || numberOfValuesForWhichOrderIsImportant == 1;
			if (orderIsNotImportant) {
				boolean valueFound = false;
				for (int k = 0; k < otherAllValues.size(); k++) {
					
					DeclarationValue checkingValue = otherAllValues.get(k);
					
					if (checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
						continue;
					
					if ((!onlyCheckEquality && twoValuesAreEquivalent(currentValue, checkingValue)) ||
						(onlyCheckEquality && currentValue.equals(checkingValue))) {
						/*
						 * Removing the checking value is necessary for special cases like
						 * background-position: 0px 0px VS background-position: 0px 10px
						 */
						otherAllValues.set(k, null);
						valueFound = true;
						break;
					}
				}
				
				if (!valueFound)
					return false;

			} else {
				
				// Non-keyword values should appear at the same position in the other declaration
				DeclarationValue checkingValue = otherAllValues.get(i);
				if (checkingValue == null || (onlyCheckEquality && checkingValue.isAMissingValue()))
					return false;
				if ((!onlyCheckEquality && twoValuesAreEquivalent(currentValue, checkingValue)) ||
						(onlyCheckEquality && currentValue.equals(checkingValue)))
					otherAllValues.set(i, null);
				else
					return false;
			
			}
		}

		return true;
	}

	/**
	 * Checks two values to identify if they are equivalent.
	 * Both values must be of type {@link DeclarationEquivalentValue},
	 * otherwise, the {@link #equals()} method specifies the equivalency. 
	 * @param value1
	 * @param value2
	 * @return
	 */
	private boolean twoValuesAreEquivalent(DeclarationValue value1, DeclarationValue value2) {
			
		if (value1 == null || value2 == null)
				return false;
		
		boolean equal = false;

		if (value1 instanceof DeclarationEquivalentValue && value2 instanceof DeclarationEquivalentValue)
			equal = ((DeclarationEquivalentValue)value1).equivalent((DeclarationEquivalentValue)value2);
		else 
			// Equals are equivalent too. 
			equal = value1.equals(value2);
		
		return equal;
	}
	

	/**
	 * Return true if two declarations are equivalent
	 * @param otherDeclaration
	 * @return
	 */
	public boolean equivalent(Declaration otherDeclaration) {
		return (property.equals(otherDeclaration.property) && valuesEquivalent(otherDeclaration, false));
	}
	
	/**
	 * Returns the line number in the source CSS file
	 * @return
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Returns the column number in the source CSS file
	 * @return
	 */
	public int getColumnNumber() {
		return colNumber;
	}
	

	@Override
	public String toString() {
		StringBuilder valueString = new StringBuilder("");
		for (DeclarationValue v : declarationValues) {
			if (v.isAMissingValue())
				continue;
			if (commaSeparatedListOfValues)
				valueString.append(v + ", ");
			else 
				valueString.append(v + " ");
		}
		if (commaSeparatedListOfValues)
			valueString.delete(valueString.length() - 2, valueString.length());
		else 
			valueString.delete(valueString.length() - 1, valueString.length());
		return String.format("%s: %s", property, valueString);
	}

	/**
	 * The equals method for Declaration only takes the values for 
	 * "property: value" into account. It doesn't take the Selector 
	 * to which this declaration belongs into account. Note that
	 * this method does NOT take equivalent values into account. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;
		Declaration otherDeclaration = (Declaration) obj;
		
		return (property.equals(otherDeclaration.property) && valuesEquivalent(otherDeclaration, true));
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = 31 * hashCode + property.hashCode();
		int h = 0;
		for (DeclarationValue v : declarationValues)
			if (!v.isAMissingValue())
				h += v.hashCode();
		hashCode = 31 * hashCode + h;

		return hashCode;
	}

}
