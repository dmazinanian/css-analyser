package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.csshelper.ListStyleHelper;
import ca.concordia.cssanalyser.csshelper.NamedColors;
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
	protected final int lineNumber;
	protected final int colNumber;
	protected final boolean isImportant;
	protected int numberOfMissingValues;
	protected final boolean isCommaSeparatedListOfValues;
	
	/**
	 * Creates a new instance of Declaration and 
	 * add missing values (initial values) for properties in which user can eliminate some values.
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param fileLineNumber
	 * @param fileColNumber
	 * @param important
	 */
	public Declaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important) {
		this(propertyName, values, belongsTo, fileLineNumber, fileColNumber, important, true); 
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param values
	 * @param belongsTo
	 * @param fileLineNumber
	 * @param fileColNumber
	 * @param important
	 * @param addMissingValues
	 */
	public Declaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int fileLineNumber, int fileColNumber, boolean important, boolean addMissingValues) {
		property = propertyName;
		declarationValues = values;
		parentSelector = belongsTo;
		lineNumber = fileLineNumber;
		colNumber = fileColNumber;
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
	private void addMissingValues() {
		
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
		
		case "border-radius":

			//http://www.w3.org/TR/css3-background/#the-border-radius
			DeclarationValue borderTLHRadius, borderTLVRadius,
							 borderTRHRadius, borderTRVRadius,
							 borderBRHRadius, borderBRVRadius,
							 borderBLHRadius, borderBLVRadius;
			
			int slashPosition = declarationValues.indexOf(new DeclarationValue("/", ValueType.SEPARATOR));
			if (declarationValues.size() == 0) 
				return;
			borderTLHRadius = declarationValues.get(0);
			borderTRHRadius = borderTLHRadius.clone(); 
			borderBRHRadius = borderTLHRadius.clone();
			borderBLHRadius = borderTLHRadius.clone();
		
			/*
			 * If there is no slash, check number of declarations
			 * else, check the number of declarations before slash 
			 */
			switch (slashPosition < 0 ? declarationValues.size() : slashPosition) {
			case 1:
				addMissingValue(borderTRHRadius, 1);
				addMissingValue(borderBRHRadius, 2);
				addMissingValue(borderBLHRadius, 3);
				break;
			case 2:
				borderTRHRadius = declarationValues.get(1); 
				borderBLHRadius = borderTRHRadius.clone();
				addMissingValue(borderBRHRadius, 2);
				addMissingValue(borderBLHRadius, 3);
				break;
			case 3:
				borderTRHRadius = declarationValues.get(1); 
				borderBRHRadius = declarationValues.get(2);
				borderBLHRadius = borderTRHRadius.clone(); 
				addMissingValue(borderBLHRadius, 3);
				break;
			case 4:
				borderTRHRadius = declarationValues.get(1); 
				borderBRHRadius = declarationValues.get(2); 
				borderBLHRadius = declarationValues.get(3);
			}

			if (slashPosition < 0)
				addMissingValue(new DeclarationValue("/", ValueType.SEPARATOR), 4);
			
			borderTLVRadius = borderTLHRadius.clone(); 
			borderTRVRadius = borderTRHRadius.clone(); 
			borderBRVRadius = borderBRHRadius.clone();
			borderBLVRadius = borderBLHRadius.clone();
			
			switch (declarationValues.size()) {
			case 5: // no item after slash
				addMissingValue(borderTLVRadius, 5);
				addMissingValue(borderTRVRadius, 6);
				addMissingValue(borderBRVRadius, 7);
				addMissingValue(borderBLVRadius, 8);
				break;
			case 6: // 1 item after slash
				borderTLVRadius = declarationValues.get(5);
				borderTRVRadius = borderTLVRadius.clone(); 
				borderBRVRadius = borderTLVRadius.clone();
				borderBLVRadius = borderTLVRadius.clone();
				addMissingValue(borderTRVRadius, 6);
				addMissingValue(borderBRVRadius, 7);
				addMissingValue(borderBLVRadius, 8);
				break;
			case 7: // 2 items after slash
				borderTLVRadius = declarationValues.get(5);
				borderTRVRadius = declarationValues.get(6); 
				borderBRVRadius = borderTLVRadius.clone();
				borderBLVRadius = borderTRVRadius.clone();
				addMissingValue(borderBRVRadius, 7);
				addMissingValue(borderBLVRadius, 8);
				break;
			case 8: // 3 items after slash
				borderTLVRadius = declarationValues.get(5);
				borderTRVRadius = declarationValues.get(6); 
				borderBRVRadius = declarationValues.get(7);
				borderBLVRadius = borderTRVRadius.clone();
				addMissingValue(borderBLVRadius, 8);
			}

			// Add shorthand values
			ShorthandDeclaration shorthand = (ShorthandDeclaration)this;
			shorthand.addIndividualDeclaration("border-top-left-radius", borderTLHRadius, borderTLVRadius);
			shorthand.addIndividualDeclaration("border-top-right-radius", borderTRHRadius, borderTRVRadius);
			shorthand.addIndividualDeclaration("border-bottom-right-radius", borderBRHRadius, borderBRVRadius);
			shorthand.addIndividualDeclaration("border-bottom-left-radius", borderBLHRadius, borderBLVRadius);
			
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
			
		case "margin":
		case "padding":
		case "border-width":
		case "border-style":
		case "border-color":
			
			if (declarationValues.size() == 0)
				return;
			
			DeclarationValue val1 = declarationValues.get(0),
							 val2 = val1.clone(),
							 val3 = val1.clone(),				 
							 val4 = val1.clone();
					
			switch (declarationValues.size()) {
			case 1:
				addMissingValue(val2, 1);
				addMissingValue(val3, 2);
				addMissingValue(val4, 3);
				break;
			case 2:
				val2 = declarationValues.get(1);
				val4 = val2.clone(); 
				addMissingValue(val3, 2);
				addMissingValue(val4, 3);
				break;
			case 3:
				val2 = declarationValues.get(1);
				val3 = declarationValues.get(2);
				val4 = val2.clone();
				addMissingValue(val4, 3);
				break;
			case 4:
				val2 = declarationValues.get(1);
				val3 = declarationValues.get(2);
				val4 = declarationValues.get(3);
			}
			
			shorthand = (ShorthandDeclaration)this;
			int insertionPoint = property.indexOf("border");
			String prefix = "", postfix = "";
			if (insertionPoint >= 0) {
				prefix = "border";
				postfix = property.substring(insertionPoint + 6);
			} else {
				prefix = property;
			}
			shorthand.addIndividualDeclaration(prefix + "-top" + postfix, val1);
			shorthand.addIndividualDeclaration(prefix + "-right" + postfix, val2);
			shorthand.addIndividualDeclaration(prefix + "-bottom" + postfix, val3);
			shorthand.addIndividualDeclaration(prefix + "-left" + postfix, val4);
						
			break;
			
		case "border":				
		case "border-bottom":
		case "border-left":				
		case "border-right":				
		case "border-top":				
		case "outline":
		case "column-rule":
			// http://www.w3.org/TR/css3-background/#the-border-shorthands
			// http://www.w3.org/TR/css3-ui/#outline-properties (working draft)
			// http://www.w3.org/TR/css3-multicol/
			DeclarationValue valueColor = null,
							 valueWidth = null,
							 valueStyle = null;
			
			for (DeclarationValue v : declarationValues) {
				if (v.getType() == ValueType.COLOR) {
					valueColor = v;
				} else if (v.getType() == ValueType.LENGTH) {
					valueWidth = v;
				} else if (v.getType() == ValueType.IDENT) {
					if ("thin".equals(v.getValue()) || "medium".equals(v.getValue()) || "thick".equals(v.getValue()))
						valueWidth = v;
					else
						valueStyle = v;
				}
			}
			
			if (valueWidth == null) {
				valueWidth = new DeclarationValue("medium", ValueType.LENGTH);
				addMissingValue(valueWidth, 0);
			}
			
			if (valueStyle == null) {
				valueStyle = new DeclarationValue("none", ValueType.IDENT);
				addMissingValue(valueStyle, 1);
			}
			
			if (valueColor == null) {
				valueColor = new DeclarationValue("currentColor", ValueType.COLOR);
				addMissingValue(valueColor, 2);
			}
			
			shorthand = (ShorthandDeclaration)this;
			shorthand.addIndividualDeclaration(property + "-style", valueStyle);
			shorthand.addIndividualDeclaration(property + "-color", valueColor);
			shorthand.addIndividualDeclaration(property + "-width", valueWidth);
			
			
			break;
			
		case "columns":
			// http://www.w3.org/TR/css3-multicol/
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
			
			shorthand = (ShorthandDeclaration)this;
			
			shorthand.addIndividualDeclaration("column-width", colWidth);
			shorthand.addIndividualDeclaration("column-count", colCount);
			
			break;
			
		case "list-style":
			// http://www.w3.org/TR/CSS21/generate.html
			
			DeclarationValue listType = null,
							 listPosition = null,
							 listImage = null;
			List<Integer> nonValues = new ArrayList<>();
			for (int i = 0; i < declarationValues.size(); i++) {
				DeclarationValue v  = declarationValues.get(i);
				if (v.getType() == ValueType.URL)
					listImage = v;
				else if (v.getType() == ValueType.IDENT) {
					String val = v.getValue();
					switch (val) {
					case "inside":
					case "hanging":
					case "outside":
						listPosition = v;
						break;
					case "inline":
						listType = v;
					case "none":
						nonValues.add(i);
						break;
					case "inherit":
						break;
					default:
						if (ListStyleHelper.isStyleTypeName(val))
							listType = v;
					}
				}
			}

			if (declarationValues.size() == 1) {
				switch (declarationValues.get(0).getValue()) {
				case "inherit":
				case "none":
					return;
				default:
					// throw new Exception("Invalid list-style declaration");
				}

			}
			
			if (nonValues.size() >= 2) {
				listType = declarationValues.get(nonValues.get(0));
				listImage = declarationValues.get(nonValues.get(1));
			} else if (nonValues.size() == 1) {
				if (nonValues.contains(0))
					listType = declarationValues.get(nonValues.get(0));
				else
					listImage = declarationValues.get(nonValues.get(0));
			}

			if (listType == null) {
				listType = new DeclarationValue("none", ValueType.IDENT);
				addMissingValue(listType, 0);
			}

			if (listPosition== null) {
				listPosition = new DeclarationValue("outside", ValueType.IDENT);
				addMissingValue(listPosition, 1);
			}

			if (listImage == null) {
				listImage = new DeclarationValue("none", ValueType.IDENT);
				addMissingValue(listImage, 2);
			}
						
			
			shorthand = (ShorthandDeclaration)this;

			shorthand.addIndividualDeclaration("list-style-type", listType);
			shorthand.addIndividualDeclaration("list-style-position", listPosition);
			shorthand.addIndividualDeclaration("list-style-image", listImage);
			
			break;
			
		case "transition":

			// http://www.w3.org/TR/css3-transitions/#transition-shorthand-property
			// transition is comma-separated
			
			List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
			DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
			allValues.add(sentinel);
			
			int currentLayerStartIndex = 0;
			int totalAddedMissingValues = 0;
			
			for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
				DeclarationValue currentValue = allValues.get(currentValueIndex);
				
				if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
					DeclarationValue transitionProperty = null,
							 		 transitionTimingFunction = null,
							 		 transitionDuration = null,
							 		 transitionDelay = null;
					
					for (int currentLayerValueIndex = currentLayerStartIndex; currentLayerValueIndex < currentValueIndex; currentLayerValueIndex++) {
						DeclarationValue currentLayerCurrentValue = allValues.get(currentLayerValueIndex);
						switch (currentLayerCurrentValue.getType()) {
						case TIME:
							// First time value is for duration, second for delay, based on W3C
							if (transitionDuration == null)
								transitionDuration = currentLayerCurrentValue;
							else
								transitionDelay = currentLayerCurrentValue;
							break;
						case FUNCTION:
							if (currentLayerCurrentValue.getValue().startsWith("steps") || 
									currentLayerCurrentValue.getValue().startsWith("cubic-bezier")) {
									transitionTimingFunction = currentLayerCurrentValue;
							}
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
								transitionProperty = currentLayerCurrentValue;
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
						transitionTimingFunction = new DeclarationValue("ease", ValueType.IDENT);
						addMissingValue(transitionTimingFunction, currentLayerStartIndex + missingValueOffset + 2);
						totalAddedMissingValues++;
					}
					
					if (transitionDelay == null) {
						transitionDelay = new DeclarationValue("0s", ValueType.TIME);
						addMissingValue(transitionDelay, currentLayerStartIndex + missingValueOffset + 3);
						totalAddedMissingValues++;
					}
					currentLayerStartIndex = currentValueIndex + 1;	
					
					shorthand = (ShorthandDeclaration)this;
					shorthand.addIndividualDeclaration("transition-duration", transitionDuration);
					shorthand.addIndividualDeclaration("transition-timing-function", transitionTimingFunction);
					shorthand.addIndividualDeclaration("transition-delay", transitionDelay);
					shorthand.addIndividualDeclaration("transition-property", transitionProperty);				
					
				}
				
			}
			
			break;
			
		case "font":
			DeclarationValue fontStyle = new DeclarationValue("normal", ValueType.IDENT),
							 fontVarient = new DeclarationValue("normal", ValueType.IDENT),
							 fontWeight = new DeclarationValue("normal", ValueType.IDENT),
							 fontStretch = new DeclarationValue("normal", ValueType.IDENT),
							 fontSize = new DeclarationValue("medium", ValueType.IDENT),
							 lineHeight = new DeclarationValue("normal", ValueType.IDENT);
			
			boolean fontStyleFound = false,
					fontVarianFound = false,
					fontWeightFound = false,
					fontStretchFound = false,
					fontSizeFound = false,
					lineHeightFound = false;

			List<DeclarationValue> fontFamilies = new ArrayList<>();
			List<Integer> normalPositions = new ArrayList<>();
			slashPosition = -1;
			boolean slashFound = false;
			for (int currentValueIndex = 0; currentValueIndex < declarationValues.size(); currentValueIndex++) {
				DeclarationValue currentValue = declarationValues.get(currentValueIndex);
				switch (currentValue.getType()) {
				case IDENT:
					switch (currentValue.getValue()) {
					case "caption":
					case "icon":
					case "menu":
					case "message-box":
					case "small-caption":
					case "status-bar":
						return;
					case "italic":
					case "oblique": // normal
						fontStyle = currentValue;
						fontStyleFound = true;
						break;
					case "small-caps":
						fontVarient = currentValue;
						fontVarianFound = true;
						break;
					case "bold":
					case "bolder":
					case "lighter":
						fontWeight = currentValue;
						fontWeightFound = true;
						break;
					case "ultra-condensed":
					case "extra-condensed":
					case "condensed":
					case "semi-condensed":
					case "semi-expanded":
					case "expanded":
					case "extra-expanded":
					case "ultra-expanded":
						fontStretch = currentValue;
						fontStretchFound = true;
						break;
					case "xx-small":
					case "x-small":
					case "small":
					case "medium":
					case "large":
					case "x-large":
					case "xx-large":
					case "larger":
					case "smaller":
						fontSize = currentValue;
						fontSizeFound = true;
						break;
					case "normal":
						normalPositions.add(currentValueIndex);
					case "inherit":
						//?
						break;
					default:
						fontFamilies.add(currentValue);
					}
					break;
				case STRING:
					fontFamilies.add(currentValue);
					break;
				case OPERATOR:
					if ("/".equals(currentValue.getValue()) && currentValueIndex + 1 < declarationValues.size()) {
						lineHeight = declarationValues.get(currentValueIndex + 1);
						slashPosition = currentValueIndex;
						slashFound = true;
						lineHeightFound = true;
					}
					break;
				case INTEGER:
					if (currentValueIndex > 0 && "/".equals(declarationValues.get(currentValueIndex - 1).getValue())) {
						lineHeight = currentValue;
						lineHeightFound = true;
					}
					else {
						for (int weight = 100; weight <= 900; weight += 100) {
							if (String.valueOf(weight).equals(currentValue.getValue()))
							{
								fontWeight = currentValue;
								fontWeightFound = true;
								break;
							}
						}
					}
					break;
				case PERCENTAGE:
				case LENGTH:
					if (currentValueIndex > 0 && "/".equals(declarationValues.get(currentValueIndex - 1).getValue())) {
						lineHeight = currentValue;
						lineHeightFound = true;
					}
					else {
						fontSize = currentValue;
						fontSizeFound = true;
					}
					break;				
				default:
				}
			}
			
			int numberOfNormalsBeforeSlash = normalPositions.size();
			if (slashFound && normalPositions.contains(slashPosition + 1))
				numberOfNormalsBeforeSlash--;
			
			if (!fontStyleFound && numberOfNormalsBeforeSlash < 4) {			 
				addMissingValue(fontStyle, 0);
				numberOfNormalsBeforeSlash++;
			}
			
			if (!fontVarianFound && numberOfNormalsBeforeSlash < 4) {
				addMissingValue(fontVarient, 1);
				numberOfNormalsBeforeSlash++;
			}
			
			if (!fontWeightFound && numberOfNormalsBeforeSlash < 4) {
				addMissingValue(fontWeight, 2);
				numberOfNormalsBeforeSlash++;
			}
			
			if (!fontStretchFound && numberOfNormalsBeforeSlash < 4) {
				addMissingValue(fontStretch, 3);
				numberOfNormalsBeforeSlash++;
			}
			
			if (!fontSizeFound) {
				addMissingValue(fontSize, 4);			
			}
			
			if (!slashFound) {
				addMissingValue(new DeclarationValue("/", ValueType.OPERATOR), 5);
				slashPosition = 5;
				if (!lineHeightFound) {
					addMissingValue(lineHeight, slashPosition + 1);
				}
			}
			
			if (fontFamilies.size() == 0) {
				DeclarationValue font = new DeclarationValue("inherit", ValueType.IDENT);
				fontFamilies.add(font);
				addMissingValue(font, declarationValues.size());
			}
			
			shorthand = (ShorthandDeclaration)this;

			shorthand.addIndividualDeclaration("font-style", fontStyle);
			shorthand.addIndividualDeclaration("font-variant", fontVarient);
			shorthand.addIndividualDeclaration("font-weight", fontWeight);
			shorthand.addIndividualDeclaration("font-stretch", fontStretch);
			shorthand.addIndividualDeclaration("font-size", fontSize);
			shorthand.addIndividualDeclaration("line-height", lineHeight);
			for (DeclarationValue fontFamily : fontFamilies)
				shorthand.addIndividualDeclaration("font-family", fontFamily);
			
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
			allValues = new ArrayList<>(declarationValues);
			sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
			allValues.add(sentinel);
			
			currentLayerStartIndex = 0;
			totalAddedMissingValues = 0;
			
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
					boolean isLastLayer = currentValueIndex == allValues.size() - 1;
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
					slashFound = false;

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
							
					shorthand = (ShorthandDeclaration)this;
					
					shorthand.addIndividualDeclaration("background-image", bgImage);
					shorthand.addIndividualDeclaration("background-repeat", bgRepeat);
					shorthand.addIndividualDeclaration("background-attachement", bgAttachement);
					shorthand.addIndividualDeclaration("background-origin", bgOrigin);
					shorthand.addIndividualDeclaration("background-clip", bgClip);
					shorthand.addIndividualDeclaration("background-position", bgPositionX, bgPositionY);
					shorthand.addIndividualDeclaration("background-size", bgSizeH, bgSizeW);
					if (isLastLayer)
						shorthand.addIndividualDeclaration("background-color", bgColor);
					
					currentLayerStartIndex = currentValueIndex + 1;
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
			if (!v.isKeyword() || "inherit".equals(v.getValue()) || "none".equals(v.getValue()) || NamedColors.getRGBAColor(v.getValue()) != null)
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
		return (property.equals(otherDeclaration.property) && valuesEquivalent(otherDeclaration, false));
	}
	
	/**
	 * Return true if the given declarations is equal
	 * with this declaration
	 * @param otherDeclaration
	 * @return
	 */
	public boolean declarationEquals(Declaration otherDeclaration) {
		return (property.equals(otherDeclaration.property) && valuesEquivalent(otherDeclaration, true));
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
			result = prime * result + colNumber;
			result = prime
					* result
					+ ((declarationValues == null) ? 0 : declarationValues
							.hashCode());
			result = prime * result + (isCommaSeparatedListOfValues ? 1231 : 1237);
			result = prime * result + (isImportant ? 1231 : 1237);
			result = prime * result + lineNumber;
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
		if (colNumber != other.colNumber)
			return false;
		if (lineNumber != other.lineNumber)
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
		//return DeclarationFactory.getDeclaration(property, values, parentSelector.clone(), lineNumber, colNumber, isImportant)
		return new Declaration(property, values, parentSelector.clone(), lineNumber, colNumber, isImportant, false);
	}

	/**
	 * Copies current declaration to a new selector. Values are cloned.
	 *  
	 * @param newParent
	 * @return
	 */
	public Declaration cloneToSelector(Selector newParent) {
		List<DeclarationValue> values = new ArrayList<>();
		for (DeclarationValue v : declarationValues) {
			values.add(v.clone());
		}
		return DeclarationFactory.getDeclaration(property, values, newParent, lineNumber, colNumber, isImportant);
	}
}
