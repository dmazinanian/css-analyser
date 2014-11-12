package ca.concordia.cssanalyser.cssmodel.declaration.value;

import ca.concordia.cssanalyser.csshelper.ColorHelper;
import ca.concordia.cssanalyser.csshelper.NamedColorsHelper;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;

/**
 * This class is used to add equivalent values to a value.
 * The {@link #getDeclarationValue(String, String, ValueType)} method
 * returns either a {@link DeclarationValue} or {@link DeclarationEquivalentValue}.
 * @author Davood Mazinanian
 *
 */
public class DeclarationValueFactory {
	
	
	public static DeclarationValue getDeclarationValue(String forProperty, String value, ValueType type) {
		//return new DeclarationValue(value, type);
		forProperty = Declaration.getNonVendorProperty(forProperty);
		value = value.toLowerCase().trim();
		switch (type) {
			case IDENT: {
				if (NamedColorsHelper.isNamedColor(value))
					return new DeclarationEquivalentValue(value, NamedColorsHelper.getRGBAColor(value), ValueType.COLOR);
				switch (value) {
					case "currentColor":
						return new DeclarationValue(value, ValueType.COLOR);
					case "none":
						return new DeclarationEquivalentValue(value, value, ValueType.IDENT);
					case "left":
					case "top":
						switch (forProperty) {
						case "background-position":
						case "background":
						case "perspective-origin":
						case "transform-origin":
							return new DeclarationEquivalentValue(value, "0.0px", ValueType.LENGTH);
						}
						break;
					case "right":
					case "bottom":
						switch (forProperty) {
						case "background-position":
						case "background":
						case "perspective-origin":
						case "transform-origin":
							return new DeclarationEquivalentValue(value, "100.0%", ValueType.LENGTH);
						}
						break;
					case "center":
						switch (forProperty) {
						case "background-position":
						case "background":
						case "perspective-origin":
						case "transform-origin":
							return new DeclarationEquivalentValue(value, "50.0%", ValueType.LENGTH);
						}
						break;
					case "bold":
						return new DeclarationEquivalentValue(value, "700", ValueType.INTEGER);
					case "normal":
						if (forProperty.equals("font-weight"))
							return new DeclarationEquivalentValue(value, "400", ValueType.INTEGER);
						// What should we do for font shorthand property?!
					}

				return new DeclarationValue(value, ValueType.IDENT);
			}
			case INTEGER: {
				
				if ("0".equals(value)) {
					switch (forProperty) {
						case "margin":
						case "margin-left":
						case "margin-right":
						case "margin-top":
						case "margin-bottom":
						case "padding":
						case "padding-left":
						case "padding-right":
						case "padding-bottom":
						case "padding-top":
						case "top":
						case "left":
						case "bottom":
						case "right":
						case "height":
						case "width":
						case "max-height":
						case "max-width":
						case "min-height":
						case "min-width":
						case "background-position":
						case "background-size":
						case "background":
						case "border":				
						case "border-bottom":
						case "border-left":				
						case "border-right":				
						case "border-top":				
						case "outline":
						case "border-top-width":
						case "border-bottom-width":
						case "border-left-width":
						case "border-right-width":
						case "border-width":
						case "outline-width":
						case "border-radius":
						case "border-bottom-left-radius":
						case "border-bottom-right-radius":
						case "border-bottom-top-radius":
						case "border-bottom-bottom-radius":
						case "column-width":
						case "column-rule-width":
						case "column-gap":
						case "perspective-origin":
						case "text-shadow":
						case "box-shadow":
						/*
						 * Dangorous to do!
						 * case "transform-origin":
						 * case "-ms-transform-origin":
						 * case "-webkit-transform-origin":
						 */
						return new DeclarationEquivalentValue("0", "0px", ValueType.LENGTH);	
					}
				}
			}
			case PERCENTAGE: {
				String eqVal = value;
				if ("0".equals(value)) {
					switch (forProperty) {
						case "background-position":
						case "background-size":
						case "border-radius":
						case "-webkit-border-radius":
						case "-moz-border-radius":
						case "border-bottom-left-radius":
						case "border-bottom-right-radius":
						case "border-bottom-top-radius":
						case "border-bottom-bottom-radius":
						case "transform-origin":
							eqVal = "0px";
							break;
						case "rgb":
						case "rgba":
						case "hsl":
						case "hsla":
							eqVal = "0";
					}
				}
				return new DeclarationEquivalentValue(value, eqVal, ValueType.PERCENTAGE);
			}	
			case COLOR: {
				String eqValue = "";
				if (value.startsWith("#")) {
					try {
						eqValue = ColorHelper.RGBAFromHEX(value.substring(1));
					} catch (Exception e) {
						
					}
				} else if (value.startsWith("rgba(")) {
					String[] values = getCommaSeparatedValueParts(value);
					if (values.length != 4)
						throw new RuntimeException("Invalid rgba color: " + value);
					int r = getRgbComponentValue(values[0]);
					int g = getRgbComponentValue(values[1]);
					int b = getRgbComponentValue(values[2]);
					float a = Math.min(Float.valueOf(values[3]), 1);
					eqValue = ColorHelper.RGBA(r, g, b, a);
				} else if (value.startsWith("rgb(")) {
					String[] values = getCommaSeparatedValueParts(value);
					if (values.length != 3)
						throw new RuntimeException("Invalid rgb color: " + value);
					int r = getRgbComponentValue(values[0]);
					int g = getRgbComponentValue(values[1]);
					int b = getRgbComponentValue(values[2]);
					eqValue = ColorHelper.RGBAfromRGB(r, g, b);
				} else if (value.startsWith("hsla(")) {
					String[] values = getCommaSeparatedValueParts(value);
					if (values.length != 4)
						throw new RuntimeException("Invalid hsla color: " + value);
					float h = Math.min(Integer.valueOf(values[0]), 360) / 360F;
					float s = Math.min(Integer.valueOf(values[1].substring(0, values[1].length() - 2)), 100) / 100F;
					float l = Math.min(Integer.valueOf(values[2].substring(0, values[2].length() - 2)), 100) / 100F;
					float a = Math.min(Float.valueOf(values[3]), 1);
					eqValue = ColorHelper.RGBAFromHSLA(h, s, l, a);
				} else if (value.startsWith("hsl(")) {
					String[] values = getCommaSeparatedValueParts(value);
					if (values.length != 3)
						throw new RuntimeException("Invalid hsla color: " + value);
					float h = Math.min(Integer.valueOf(values[0]), 360) / 360F;
					float s = Math.min(Integer.valueOf(values[1].substring(0, values[1].length() - 2)), 100) / 100F;
					float l = Math.min(Integer.valueOf(values[2].substring(0, values[2].length() - 2)), 100) / 100F;
					eqValue = ColorHelper.RGBAFromHSLA(h, s, l, 1F);
				}
				return new DeclarationEquivalentValue(value, eqValue, ValueType.COLOR);
			}
			case LENGTH: {
				String postfix = value.substring(value.length() - 2);
				float floatVal = Float.valueOf(value.replace(postfix, ""));
				String eqVal = "";
				switch(postfix) {
					case "pc": {
						// 1pc = 12pt = 16px
						eqVal = formatFloat(floatVal * 16) + "px";
						break;
					}
					case "pt": {
						// 72pt is 96px
						eqVal = formatFloat(floatVal / 72F * 96F) + "px";
						break;
					}
					case "in": {
						// Every inch is 96px
						eqVal = formatFloat(floatVal * 96F) + "px";
						break;
					}
					case "cm": {
						// Every cm is (2.54^-1 * 96)px
						// In browser, every cm is about 38px
						eqVal = formatFloat(floatVal * 38F) + "px";
						break;
					}
					case "mm": {
						//every mm is 0.01 cm
						eqVal = formatFloat(floatVal * 38F / 100F) + "px";
						break;
					}
					case "px": {
						eqVal = value;
						break;
					}
					case "em": {
						// 1em = 100%, if we are talking about font
						if ("font".equals(forProperty) || "font-size".equals(forProperty) || "line-height".equals(forProperty)) {
							eqVal = formatFloat(floatVal * 100) + "%";
						} else {
							return new DeclarationValue(value, ValueType.LENGTH);
						}
						break;
					}
				}
				
				return new DeclarationEquivalentValue(value, eqVal, ValueType.LENGTH);
			
			}
			case ANGLE: {
				String eqVal = "";
				if (value.endsWith("grad")) {
					// 1grad = 0.9deg
					eqVal = formatFloat(Float.valueOf(value.replace("grad", "")) * 0.9F) + "deg";
				} else if (value.endsWith("rad")) {
					// 2pi rad = 360deg
					eqVal = formatFloat(Float.valueOf(value.replace("grad", "")) / (2 * 3.1415926F) * 360) + "deg";	
				} else if (value.endsWith("turn")) {
					// 1turn = 360deg
					eqVal = formatFloat(Float.valueOf(value.replace("grad", "")) * 360) + "deg";
				} else if (value.endsWith("deg")) {
					eqVal = value;
				}
				return new DeclarationEquivalentValue(value, eqVal, type);
			}
			case FREQUENCY: {		
				String eqVal = value;
				// 1KHz = 1000Hz
				if (value.endsWith("khz")) {
					eqVal = formatFloat(Float.valueOf(value.replace("khz", "")) * 1000) + "hz";
				}
				
				return new DeclarationEquivalentValue(value, eqVal, ValueType.FREQUENCY);
			}
			case TIME: {
				// Each second is 1000 ms
				String eqVal = value;
				if (value.endsWith("ms")) {
					eqVal = formatFloat(Float.valueOf(value.replace("ms", "")) * 1000) + "ms";
				}
				return new DeclarationEquivalentValue(value, eqVal, ValueType.TIME);
			}
			case URL: {
				if ("url('')".equals(value))
					return new DeclarationEquivalentValue(value, "none", ValueType.URL);
				return new DeclarationValue(value, ValueType.URL);
			}
			default:
				return new DeclarationValue(value, type);
		}
	}

	protected static String[] getCommaSeparatedValueParts(String value) {
		
		String[] values = value.substring(value.indexOf("(") + 1, value.indexOf(")")).split(",");
		
		for (int i = 0; i < values.length; i++)
			values[i] = values[i].trim();
		
		return values;
	}
	
	/**
	 * Returns a color component value (like red value) in 255 scale.
	 * (it might be percent or integer, like rgb(100%, 50%, 40%) )
	 * @param color
	 * @return
	 */
	private static int getRgbComponentValue(String value) {
		if (value.endsWith("%"))
			return (int) Math.min(Integer.valueOf(value.substring(0, value.length() - 1)) * 255, 255);
		else
			return Math.min(Integer.valueOf(value), 255);
	}
	
	public static String formatFloat(float f) {
		if(f == (long) f)
			return String.format("%d",(long)f);
		else
			return String.format("%s",f);
	}

	public static DeclarationValue getFontValue(String propertyName, float value, String unit) {
		String val = formatFloat(value) + unit;
		return getDeclarationValue(propertyName, val, ValueType.LENGTH);
	}
	
}
