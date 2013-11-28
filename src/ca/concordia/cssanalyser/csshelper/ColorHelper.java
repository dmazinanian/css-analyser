package ca.concordia.cssanalyser.csshelper;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.LexicalUnit;

/**
 * Utility class for converting color values together
 * 
 * @author Davood Mazinanian
 * 
 */
public class ColorHelper {
	/**
	 * Returns a color component value (like red value) in 255 scale.
	 * (it might be percent or integer, like rgb(100%, 50%, 40%) )
	 * @param color
	 * @return
	 */
	private static int getRgbComponentValue(LexicalUnit color) {
		switch (color.getLexicalUnitType()) {
		case LexicalUnit.SAC_INTEGER:
			return Math.min(color.getIntegerValue(), 255);
		case LexicalUnit.SAC_PERCENTAGE:
			return (int) Math.min(color.getFloatValue() * 255, 255);
		default:
			throw new CSSException(CSSException.SAC_SYNTAX_ERR, "RGB component value must be integer or percentage, was " + color, null);
		}
	}
	
	/**
	 * Returns RGBA from a HEX string (# should not be included)
	 * @param stringValue
	 * @return
	 * @throws Exception
	 */
	public static String RGBAFromHEX(String stringValue) throws Exception {
		int r = 0, g = 0, b = 0;
		if (stringValue.length() == 3) {
			String sh = stringValue.substring(0, 1);
			r = Integer.parseInt(sh + sh, 16);
			sh = stringValue.substring(1, 2);
			g = Integer.parseInt(sh + sh, 16);
			sh = stringValue.substring(2, 3);
			b = Integer.parseInt(sh + sh, 16);
		} else if (stringValue.length() == 6) {
			r = Integer.parseInt(stringValue.substring(0, 2), 16);
			g = Integer.parseInt(stringValue.substring(2, 4), 16);
			b = Integer.parseInt(stringValue.substring(4, 6), 16);
		} else {
			throw new Exception("Invalid hex value");
		}
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, 1F);
	}
	
	/**
	 * Returns rgba() string from SAC rgb LexicalUnit value
	 * @param colors
	 * @return
	 */
	public static String RGBAfromRGB(LexicalUnit colors) {
		LexicalUnit red = colors;
		int r = getRgbComponentValue(red);
		LexicalUnit green = red.getNextLexicalUnit().getNextLexicalUnit();
		int g = getRgbComponentValue(green);
		LexicalUnit blue = green.getNextLexicalUnit().getNextLexicalUnit();
		int b = getRgbComponentValue(blue);
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, 1F);
	}

	/**
	 * Gets rgba() string from SAC rgba LexicalUnit value
	 * @param colors
	 * @return
	 */
	public static String RGBA(LexicalUnit colors) {
		LexicalUnit red = colors;
		int r = getRgbComponentValue(red);
		LexicalUnit green = red.getNextLexicalUnit().getNextLexicalUnit();
		int g = getRgbComponentValue(green);
		LexicalUnit blue = green.getNextLexicalUnit().getNextLexicalUnit();
		int b = getRgbComponentValue(blue);
		LexicalUnit alpha = blue.getNextLexicalUnit().getNextLexicalUnit();
		// The problem is, the value is either in Integer or float so we need to
		// check for both of them
		float a = Math.min(alpha.getIntegerValue(), 1);
		if (a == 0) // Lets try float
			a = Math.min(alpha.getFloatValue(), 1);
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, a);
	}

	/**
	 * Converts SAC HSLA (or HSL) LexicalUnit value to RGBA
	 * @param value
	 * @return
	 */
	public static String RGBAFromHSLA(LexicalUnit value) {

		LexicalUnit hue = value;
		float h = Math.min(hue.getIntegerValue(), 360) / 360F;
		LexicalUnit saturation = hue.getNextLexicalUnit().getNextLexicalUnit();
		float s = Math.min(saturation.getFloatValue(), 100) / 100F;
		LexicalUnit lightness = saturation.getNextLexicalUnit()
				.getNextLexicalUnit();
		float l = Math.min(lightness.getFloatValue(), 100) / 100F;
		float a = 1F;
		if (lightness.getNextLexicalUnit() != null) {
			LexicalUnit alpha = lightness.getNextLexicalUnit()
					.getNextLexicalUnit();
			// Same as colorRGBA
			a = Math.min(alpha.getIntegerValue(), 1);
			if (a == 0)
				a = Math.min(alpha.getFloatValue(), 1);
		}

		int r, g, b;
		float m2;
		if (l <= 0.5)
			m2 = l * (s + 1);
		else
			m2 = l + s - l * s;

		float m1 = l * 2 - m2;
		r = (int) (HUEToRGB(m1, m2, h + 1 / 3F) * 255);
		g = (int) (HUEToRGB(m1, m2, h) * 255);
		b = (int) (HUEToRGB(m1, m2, h - 1 / 3F) * 255);

		return String.format("rgba(%s, %s, %s, %s)", r, g, b, a);

	}

	private static float HUEToRGB(float m1, float m2, float h) {
		if (h < 0)
			h++;
		if (h > 1)
			h--;
		if (h * 6 < 1)
			return m1 + (m2 - m1) * h * 6;
		if (h * 2 < 1)
			return m2;
		if (h * 3 < 2)
			return m1 + (m2 - m1) * (2 / 3F - h) * 6;
		return m1;
	}
}
