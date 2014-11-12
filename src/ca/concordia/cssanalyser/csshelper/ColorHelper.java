package ca.concordia.cssanalyser.csshelper;


/**
 * Utility class for converting color values together
 * 
 * @author Davood Mazinanian
 * 
 */
public class ColorHelper {
	
	
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
	public static String RGBAfromRGB(int r, int g, int b) {
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, 1F);
	}

	/**
	 * Gets rgba() string from SAC rgba LexicalUnit value
	 * @param colors
	 * @return
	 */
	public static String RGBA(int r, int g, int b, float a) {
		return String.format("rgba(%s, %s, %s, %s)", r, g, b, a);
	}

	/**
	 * Converts SAC HSLA (or HSL) LexicalUnit value to RGBA
	 * @param value
	 * @return
	 */
	public static String RGBAFromHSLA(float h, float s, float l, float a) {
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
