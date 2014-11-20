package ca.concordia.cssanalyser.cssmodel.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.node.ShortNode;

import ca.concordia.cssanalyser.app.CSSAnalyserCLI;
import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.csshelper.ListStyleHelper;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationEquivalentValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.declaration.value.ValueType;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;


/**
 * Representation of shorthand declarations
 * @author Davood Mazinanian
 *
 */
public class ShorthandDeclaration extends MultiValuedDeclaration {
	
	private static Logger LOGGER = FileLogger.getLogger(ShorthandDeclaration.class);

	private Map<String, Declaration> individualDeclarations;

	private static final Map<String, Set<String>> shorthandProperties = new HashMap<>();

	private boolean isVirtual = false;

	static {
		initializeShorthandsMap();
	}

	public ShorthandDeclaration(String propertyName, List<DeclarationValue> values, Selector belongsTo, int offset, int length, boolean important, boolean addMissingValues) {
		super(propertyName, values, belongsTo, offset, length, important, addMissingValues);
		if (individualDeclarations == null)
			individualDeclarations =  new HashMap<>();
	}

	/**
	 * Sets the value indicating whether this shorthand declaration 
	 * is virtual
	 * (i.e. it has been added as an equivalent for a set of 
	 * individual declarations when finding type III duplication
	 * instances)
	 * @return
	 */
	public void isVirtual(boolean virtual) {
		this.isVirtual = virtual;
	}

	/**
	 * Shows whether this shorthand declaration is virtual
	 * (i.e. it has been added as an equivalent for a set of 
	 * individual declarations when finding type III duplication
	 * instances)
	 * @return
	 */
	public boolean isVirtual() {
		return this.isVirtual;
	}

	private static void initializeShorthandsMap() {

		//addShorthandProperty("animation", )

		addShorthandProperty("background", "background-image",
				"background-repeat",
				"background-attachement",
				"background-origin",
				"background-clip",
				"background-position",
				"background-size",
				"background-color");

		addShorthandProperty("border", "border-color",
				"border-width",
				"border-style");

		addShorthandProperty("border-bottom", "border-bottom-color",
				"border-bottom-width",
				"border-bottom-style");

		addShorthandProperty("border-left", "border-left-color",
				"border-left-width",
				"border-left-style");

		addShorthandProperty("border-right", "border-right-color",
				"border-right-width",
				"border-right-style");

		addShorthandProperty("border-top", "border-top-color",
				"border-top-width",
				"border-top-style");

		addShorthandProperty("border-color", "border-left-color",
				"border-right-color",
				"border-top-color",
				"border-bottom-color");

		addShorthandProperty("border-width", "border-left-width",
				"border-right-width",
				"border-top-width",
				"border-bottom-width");

		addShorthandProperty("border-style", "border-left-style",
				"border-right-style",
				"border-top-style",
				"border-bottom-style");

		addShorthandProperty("outline", "outline-color",
				"outline-width",
				"outline-style");

		//addShorthandProperty("border-image", );
		//addShorthandProperty("target", );

		addShorthandProperty("border-radius", "border-top-left-radius",
				"border-top-right-radius",
				"border-bottom-right-radius",
				"border-bottom-left-radius");

		addShorthandProperty("list-style", "list-style-type",
				"list-style-position",
				"list-style-image");

		addShorthandProperty("margin", "margin-left",
				"margin-right",
				"margin-top",
				"margin-bottom");

		addShorthandProperty("column-rule", "column-rule-style",
				"column-rule-color",
				"column-rule-width");

		addShorthandProperty("columns", "column-width",
				"column-count");

		addShorthandProperty("padding", "padding-left",
				"padding-right",
				"padding-top",
				"padding-bottom");

		addShorthandProperty("transition", "transition-duration", 
				"transition-timing-function",
				"transition-delay", 
				"transition-property");

		addShorthandProperty("font", "font-style",
				"font-variant",
				"font-weight",
				"font-stretch",
				"font-size",
				"line-height",
				"font-family");
		
		addShorthandProperty("animation", "animation-name",
				"animation-duration",
				"animation-timing-function",
				"animation-delay",
				"animation-iteration-count",
				"animation-direction");
	}

	@Override
	protected void addMissingValues() {

		if (declarationValues == null || declarationValues.size() == 0)
			return;

		switch (getNonVendorProperty(getNonHackedProperty(property))) {
		
			case "border-radius": {
	
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
				addIndividualDeclaration("border-top-left-radius", borderTLHRadius, borderTLVRadius);
				addIndividualDeclaration("border-top-right-radius", borderTRHRadius, borderTRVRadius);
				addIndividualDeclaration("border-bottom-right-radius", borderBRHRadius, borderBRVRadius);
				addIndividualDeclaration("border-bottom-left-radius", borderBLHRadius, borderBLVRadius);
				
				final String TLHR = "border-top-left-horizontal-radius";
				final String TLVR = "border-top-left-vertical-radius";
				final String TRHR = "border-top-right-horizontal-radius";
				final String TRVR = "border-top-right-vertical-radius";
				final String BRHR = "border-bottom-right-horizontal-radius";
				final String BRVR = "border-bottom-right-vertical-radius";
				final String BLHR = "border-bottom-left-horizontal-radius";
				final String BLVR = "border-bottom-left-vertical-radius";
				
				assignStylePropertyToValue(TLHR, borderTLHRadius);
				assignStylePropertyToValue(TLVR, borderTLVRadius);
				assignStylePropertyToValue(TRHR, borderTRHRadius);
				assignStylePropertyToValue(TRVR, borderTRVRadius);
				assignStylePropertyToValue(BRHR, borderBRHRadius);
				assignStylePropertyToValue(BRVR, borderBRVRadius);
				assignStylePropertyToValue(BLHR, borderBLHRadius);
				assignStylePropertyToValue(BLVR, borderBLVRadius);
				
				break;
			}
			case "margin":
			case "padding":
			case "border-width":
			case "border-style":
			case "border-color": {
				
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
				
				int insertionPoint = property.indexOf("border");
				String prefix = "", postfix = "";
				if (insertionPoint >= 0) {
					prefix = "border";
					postfix = property.substring(insertionPoint + 6);
				} else {
					prefix = property;
				}
				
				String propertyTopName = prefix + "-top" + postfix;
				String propertyRightName = prefix + "-right" + postfix;
				String propertyBottomName = prefix + "-bottom" + postfix;
				String propertyLeftName = prefix + "-left" + postfix;
				
				addIndividualDeclaration(propertyTopName, val1);
				addIndividualDeclaration(propertyRightName, val2);
				addIndividualDeclaration(propertyBottomName, val3);
				addIndividualDeclaration(propertyLeftName, val4);
				
				assignStylePropertyToValue(propertyTopName, val1);
				assignStylePropertyToValue(propertyRightName, val2);
				assignStylePropertyToValue(propertyBottomName, val3);
				assignStylePropertyToValue(propertyLeftName, val4);
							
				break;
			}
			case "border":				
			case "border-bottom":
			case "border-left":				
			case "border-right":				
			case "border-top":				
			case "outline":
			case "column-rule": {
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
				
				String propertyStyleName = property + "-style";
				String propertyColorName = property + "-color";
				String propertyWidthName = property + "-width";
				
				addIndividualDeclaration(propertyStyleName, valueStyle);
				addIndividualDeclaration(propertyColorName, valueColor);
				addIndividualDeclaration(propertyWidthName, valueWidth);
				
				assignStylePropertyToValue(propertyStyleName, valueStyle);
				assignStylePropertyToValue(propertyColorName, valueColor);
				assignStylePropertyToValue(propertyWidthName, valueWidth);
				
				
				break;
			}
			case "columns": {
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
						
				 
				final String COLUMNWIDTH = "column-width";
				final String COLUMNCOUNT = "column-count";
				
				addIndividualDeclaration(COLUMNWIDTH, colWidth);
				addIndividualDeclaration(COLUMNCOUNT, colCount);
				
				assignStylePropertyToValue(COLUMNWIDTH, colWidth);
				assignStylePropertyToValue(COLUMNCOUNT, colCount);
				
				break;
			}
			case "list-style": {
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
							
				final String LISTTYPE = "list-style-type";
				final String LISTPOSITION = "list-style-position";
				final String LISTIMAGE = "list-style-image";
				
				addIndividualDeclaration(LISTTYPE, listType);
				addIndividualDeclaration(LISTPOSITION, listPosition);
				addIndividualDeclaration(LISTIMAGE, listImage);
				
				assignStylePropertyToValue(LISTTYPE, listType);
				assignStylePropertyToValue(LISTPOSITION, listPosition);
				assignStylePropertyToValue(LISTIMAGE, listImage);
				
				break;
			}
			case "transition": {
	
				// http://www.w3.org/TR/css3-transitions/#transition-shorthand-property
				// transition is comma-separated
				
				// See box-shadow case in MultiValuedDeclaration
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
						
						final String DURATION = "transition-duration";
						final String TIMINGFUNCTION = "transition-timing-function";
						final String DELAY = "transition-delay";
						final String PROPERTY = "transition-property";
						
						addIndividualDeclaration(DURATION, transitionDuration);
						addIndividualDeclaration(TIMINGFUNCTION, transitionTimingFunction);
						addIndividualDeclaration(DELAY, transitionDelay);
						addIndividualDeclaration(PROPERTY, transitionProperty);
						
						assignStylePropertyToValue(DURATION, currentLayerStartIndex, transitionDuration, false);
						assignStylePropertyToValue(TIMINGFUNCTION, currentLayerStartIndex, transitionTimingFunction, false);
						assignStylePropertyToValue(DELAY, currentLayerStartIndex, transitionDelay, false);
						assignStylePropertyToValue(PROPERTY, currentLayerStartIndex, transitionProperty, false);
						
					}
					
				}
				
				break;
			}
			case "font": {
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
				int slashPosition = -1;
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
				
				final String STYLE = "font-style";
				final String VARIANT = "font-variant";
				final String WEIGHT = "font-weight";
				final String STRETCH = "font-stretch";
				final String SIZE = "font-size";
				final String HEIGHT = "line-height";
				final String FAMILY = "font-family";
				
				addIndividualDeclaration(STYLE, fontStyle);
				addIndividualDeclaration(VARIANT, fontVarient);
				addIndividualDeclaration(WEIGHT, fontWeight);
				addIndividualDeclaration(STRETCH, fontStretch);		
				addIndividualDeclaration(SIZE, fontSize);
				addIndividualDeclaration(HEIGHT, lineHeight);
				addIndividualDeclaration(FAMILY, fontFamilies);
				
				assignStylePropertyToValue(STYLE, fontStyle);
				assignStylePropertyToValue(VARIANT, fontVarient);
				assignStylePropertyToValue(WEIGHT, fontWeight);
				assignStylePropertyToValue(STRETCH, fontStretch);		
				assignStylePropertyToValue(SIZE, fontSize);
				assignStylePropertyToValue(HEIGHT, lineHeight);
				for (DeclarationValue fontFamily : fontFamilies)
					assignStylePropertyToValue(FAMILY, fontFamily, true); // the order is important
				
				break;
			}
	
			case "background": {
				// Background is comma separated.
	
				List<DeclarationValue> allValues = new ArrayList<>(declarationValues);
				DeclarationValue sentinel = new DeclarationValue(",", ValueType.SEPARATOR);
				allValues.add(sentinel);
	
				int currentLayerStartIndex = 0;
				int currentLayerIndex = 0;
				/* 
				 * [ <bg-layer> , ]* <final-bg-layer>
					<bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} 
					<final-bg-layer> = <bg-image> || <position> [ / <bg-size> ]? || <repeat-style> || <attachment> || <box>{1,2} || <'background-color'>
				 */
				int totalAddedMissingValues = 0;
				for (int currentValueIndex = 0; currentValueIndex < allValues.size(); currentValueIndex++) {
					DeclarationValue currentValue = allValues.get(currentValueIndex);
					if (currentValue.getType() == ValueType.SEPARATOR && ",".equals(currentValue.getValue())) {
						// This is increased for every layer
						currentLayerIndex++;
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
	
	
						final String IMAGE = "background-image";
						final String REPEAT = "background-repeat";
						final String ATTACHEMENT = "background-attachement";
						final String ORIGIN = "background-origin";
						final String CLIP = "background-clip";
						final String POSITION = "background-position";
						final String SIZE = "background-size";
						final String COLOR = "background-color";
						
						addIndividualDeclaration(IMAGE, bgImage);
						addIndividualDeclaration(REPEAT, bgRepeat);
						addIndividualDeclaration(ATTACHEMENT, bgAttachement);
						addIndividualDeclaration(ORIGIN, bgOrigin);
						addIndividualDeclaration(CLIP, bgClip);
						addIndividualDeclaration(POSITION, bgPositionX, bgPositionY);
						addIndividualDeclaration(SIZE, bgSizeH, bgSizeW);
						
						assignStylePropertyToValue(IMAGE, currentLayerIndex, bgImage, false);
						assignStylePropertyToValue(REPEAT, currentLayerIndex, bgRepeat, false);
						assignStylePropertyToValue(ATTACHEMENT, currentLayerIndex, bgAttachement, false);
						assignStylePropertyToValue(ORIGIN, currentLayerIndex, bgOrigin, false);
						assignStylePropertyToValue(CLIP, currentLayerIndex, bgClip, false);
						assignStylePropertyToValue(POSITION + "-x", currentLayerIndex, bgPositionX, false);
						assignStylePropertyToValue(POSITION + "-y", currentLayerIndex, bgPositionY, false);
						assignStylePropertyToValue(SIZE, currentLayerIndex, bgImage, false);

						
						// Color only comes in the last layer
						if (isLastLayer) {
							addIndividualDeclaration(COLOR, bgColor);
							assignStylePropertyToValue(COLOR, bgColor);
						}
	
						currentLayerStartIndex = currentValueIndex + 1;
						

					}
				}
	
				break;
			}
			case "animation": {
				LOGGER.warn("Animation property is not handled");
				break;
			}
			default:
				throw new NotImplementedException("Shorthand property " + property + " not handled.");
		}
	}

	/**
	 * Returns an Iterable containing all shorthand property names
	 * @return
	 */
	public static Iterable<String> getAllShorthandProperties() {
		return shorthandProperties.keySet();
	}

	private static void addShorthandProperty(String shorthandPropertyName, String... individualPropertyNames) {
		shorthandProperties.put(shorthandPropertyName, new HashSet<>(Arrays.asList(individualPropertyNames)));
	}

	/**
	 * Specifies whether a property is a shorthand or not.
	 * @param property
	 * @return
	 */
	public static boolean isShorthandProperty(String property) {
		property = getNonVendorProperty(getNonHackedProperty(property));
		return shorthandProperties.containsKey(property);
	}

	public static Set<String> getIndividualPropertiesForAShorthand(String shorthandProperty) {
		Set<String> result = new HashSet<>();
		Set<String> currentLevel = shorthandProperties.get(shorthandProperty);
		if (currentLevel != null) {
			result.addAll(currentLevel);
			for (String property : currentLevel)
				result.addAll(getIndividualPropertiesForAShorthand(property));
		}
		return result;
	}

	/**
	 * If a property could become a part of a shorthand property, this method returns
	 * those shorthand properties. For example, border-left-color could be a part of 
	 * border-color or border-left shorthand properties. So this method would return them.
	 * If not, the returned set is empty. 
	 * @param property
	 * @return
	 */
	// TODO: Maybe consider using a BiMap
	public static Set<String> getShorthandPropertyNames(String property) {
		String nonVendorproperty = getNonVendorProperty(getNonHackedProperty(property));
		String prefix = "";
		if (!property.equals(nonVendorproperty))
			prefix = property.substring(0, property.indexOf(nonVendorproperty));
		Set<String> toReturn = new HashSet<>();
		for (Entry<String, Set<String>> entry : shorthandProperties.entrySet()) {
			if (entry.getValue().contains(nonVendorproperty)) {
				toReturn.add(prefix + entry.getKey());
				// This method has to act recursively, to return border for border-left-width
				Set<String> recursiveProperties = getShorthandPropertyNames(entry.getKey());
				for (String s : recursiveProperties)
					toReturn.add(prefix + s);
			}
		}
		return toReturn;
	}
	
	/**
	 * Adds an individual declaration to the list of individual declarations
	 * for current declaration. 
	 * @param propertyName The name of the property as String
	 * @param values List<Values> of the individual declaration.
	 */
	public void addIndividualDeclaration(String propertyName, List<DeclarationValue> values) {
		if (individualDeclarations == null) {
			individualDeclarations =  new HashMap<>();
		}	

		Declaration individual = individualDeclarations.get(propertyName);

		if (isCommaSeparatedListOfValues && individual != null) {
			MultiValuedDeclaration multiValuedInvividual = (MultiValuedDeclaration)individual;
			multiValuedInvividual.getValues().add(new DeclarationValue(",", ValueType.SEPARATOR));
			for (DeclarationValue v : values) {
				multiValuedInvividual.getValues().add(v);
			}
		} else {
			individual = DeclarationFactory.getDeclaration(propertyName, values, parentSelector, offset, length, isImportant, true);
		}

		addIndividualDeclaration(individual);	
	}

	/**
	 * Adds an individual declaration to the list of individual declarations
	 * for current declaration. 
	 * @param propertyName The name of the property as String
	 * @param values Values of the individual declaration.
	 */
	public void addIndividualDeclaration(String propertyName, DeclarationValue... values) {

		List<DeclarationValue> valuesList = new ArrayList<>(Arrays.asList(values));
		// Because DeclarationFactory.getDeclaration() method only accepts a list of DeclarationValues
		
		addIndividualDeclaration(propertyName, valuesList);
	
	}

	/**
	 * Adds an individual declaration to this shorthand declaration. 
	 * This method first clones the given declaration then 
	 * calls {@link DeclarationValue#setIsAMissingValue(boolean)} 
	 * method with <code>false</code> argument for each value of 
	 * cloned declaration. Then it adds it to the list of individual
	 * declarations of current shorthand declration.
	 * @param declaration
	 */
	public void addIndividualDeclaration(Declaration declaration) {

		if (individualDeclarations == null)
			individualDeclarations =  new HashMap<>();

		if (!isVirtual) {
			/*
			 * Copy, so if we are adding a real declaration, we don't want to
			 * modify it. 
			 */
			declaration = declaration.clone();
			if (declaration instanceof MultiValuedDeclaration) {
				for (DeclarationValue v : ((MultiValuedDeclaration)declaration).declarationValues) {
					v.setIsAMissingValue(false);
				}
			}

		}

		individualDeclarations.put(declaration.getProperty(), declaration);

	}

	/**
	 * Returns all individual declarations that constitute this shorthand.
	 * For example, for shorthand declaration <code>margin: 2px 4px;</code>
	 * this method returns:
	 * <ul>
	 * 	<li><code>margin-top: 2px;</code></li>
	 * 	<li><code>margin-right: 4px;</code></li>
	 * 	<li><code>margin-bottom: 2px;</code></li>
	 * 	<li><code>margin-left: 4px;</code></li>
	 * </ul>
	 * @return A collection of individual {@link Declaration}s
	 */
	public Collection<Declaration> getIndividualDeclarations() {
		return individualDeclarations.values();
	}

	/**
	 * Compares two shorthand declarations to see whether they
	 * are the same shorthand property and they have 
	 * the equal or equivalent set of individual properties. This 
	 * method is mainly used in finding type III duplications. 
	 * @param otherDeclaration
	 * @return True if the mentioned criteria are true;
	 */
	public boolean individualDeclarationsEquivalent(ShorthandDeclaration otherDeclaration) {

		if (individualDeclarations.size() != otherDeclaration.individualDeclarations.size())
			return false;

		if (!property.equals(otherDeclaration.property))
			return false;

		for (Entry<String, Declaration> entry : individualDeclarations.entrySet()) {
			Declaration otherIndividualDeclaration = otherDeclaration.individualDeclarations.get(entry.getKey());
			Declaration checkingIndividualDeclaration = entry.getValue();
			if (otherIndividualDeclaration != null && 
					(checkingIndividualDeclaration.declarationIsEquivalent(otherIndividualDeclaration) || 
							checkingIndividualDeclaration.declarationEquals(otherIndividualDeclaration)
							)
					) {
				;
			} else {
				return false;
			}
		}

		return true;
	}

	@Override
	public ShorthandDeclaration clone() {
		List<DeclarationValue> values = new ArrayList<>();
		for (DeclarationValue v : declarationValues) {
			values.add(v.clone());
		}
		ShorthandDeclaration sd = new ShorthandDeclaration(property, values, parentSelector, offset, length, isImportant, true);
		sd.isVirtual(isVirtual);
		return sd;
	}
	
	@Override
	protected String getValuesString() {
		if (!isVirtual)
			return super.getValuesString();
		else {
			return "VIRTUAL";
		}
	}
	
	@Override
	public int getDeclarationNumber() {
		if (this.isVirtual) {
			// Get the mean of the numbers of individual declarations
			int sum = 0;
			for (Declaration individual : individualDeclarations.values())
				sum += individual.getDeclarationNumber();

			return (int)Math.floor((float)sum / this.individualDeclarations.values().size());
		}
		return super.getDeclarationNumber();
	}

}