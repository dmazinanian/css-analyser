package CSSHelper;

import java.util.HashMap;
import java.util.Map;

public final class NamedColors {

	// TODO Its better to have an external file like an XML
	@SuppressWarnings("serial")
	private static final Map<String, String> colorValues = new HashMap<String, String>() {
		{
			// Basic
			put("black", "rgba(0, 0, 0, 1.0)");
			put("silver", "rgba(192, 192, 192, 1.0)");
			put("gray", "rgba(128, 128, 128, 1.0)");
			put("white", "rgba(255, 255, 255, 1.0)");
			put("maroon", "rgba(128, 0, 0, 1.0)");
			put("red", "rgba(255, 0, 0, 1.0)");
			put("purple", "rgba(128, 0, 128, 1.0)");
			put("fuchsia", "rgba(255, 0, 255, 1.0)");
			put("green", "rgba(0, 128, 0, 1.0)");
			put("lime", "rgba(0, 255, 0, 1.0)");
			put("olive", "rgba(128, 128, 0, 1.0)");
			put("yellow", "rgba(255, 255, 0, 1.0)");
			put("navy", "rgba(0, 0, 128, 1.0)");
			put("blue", "rgba(0, 0, 255, 1.0)");
			put("teal", "rgba(0, 128, 128, 1.0)");
			put("aqua", "rgba(0, 255, 255, 1.0)");
			put("transparent", "rgba(0, 0, 0, 0.0");

			// Extended
			put("aliceblue", "rgba(240, 248, 255, 1.0)");
			put("antiquewhite", "rgba(250, 235, 215, 1.0)");
			put("aquamarine", "rgba(127, 255, 212, 1.0)");
			put("azure", "rgba(240, 255, 255, 1.0)");
			put("beige", "rgba(245, 245, 220, 1.0)");
			put("bisque", "rgba(255, 228, 196, 1.0)");
			put("blanchedalmond", "rgba(255, 235, 205, 1.0)");
			put("blueviolet", "rgba(138, 43, 226, 1.0)");
			put("brown", "rgba(165, 42, 42, 1.0)");
			put("burlywood", "rgba(222, 184, 135, 1.0)");
			put("cadetblue", "rgba(95, 158, 160, 1.0)");
			put("chartreuse", "rgba(127, 255, 0, 1.0)");
			put("chocolate", "rgba(210, 105, 30, 1.0)");
			put("coral", "rgba(255, 127, 80, 1.0)");
			put("cornflowerblue", "rgba(100, 149, 237, 1.0)");
			put("cornsilk", "rgba(255, 248, 220, 1.0)");
			put("crimson", "rgba(220, 20, 60, 1.0)");
			put("cyan", "rgba(0, 255, 255, 1.0)");
			put("darkblue", "rgba(0, 0, 139, 1.0)");
			put("darkcyan", "rgba(0, 139, 139, 1.0)");
			put("darkgoldenrod", "rgba(184, 134, 11, 1.0)");
			put("darkgray", "rgba(169, 169, 169, 1.0)");
			put("darkgreen", "rgba(0, 100, 0, 1.0)");
			put("darkgrey", "rgba(169, 169, 169, 1.0)");
			put("darkkhaki", "rgba(189, 183, 107, 1.0)");
			put("darkmagenta", "rgba(139, 0, 139, 1.0)");
			put("darkolivegreen", "rgba(85, 107, 47, 1.0)");
			put("darkorange", "rgba(255, 140, 0, 1.0)");
			put("darkorchid", "rgba(153, 50, 204, 1.0)");
			put("darkred", "rgba(139, 0, 0, 1.0)");
			put("darksalmon", "rgba(233, 150, 122, 1.0)");
			put("darkseagreen", "rgba(143, 188, 143, 1.0)");
			put("darkslateblue", "rgba(72, 61, 139, 1.0)");
			put("darkslategray", "rgba(47, 79, 79, 1.0)");
			put("darkslategrey", "rgba(47, 79, 79, 1.0)");
			put("darkturquoise", "rgba(0, 206, 209, 1.0)");
			put("darkviolet", "rgba(148, 0, 211, 1.0)");
			put("deeppink", "rgba(255, 20, 147, 1.0)");
			put("deepskyblue", "rgba(0, 191, 255, 1.0)");
			put("dimgray", "rgba(105, 105, 105, 1.0)");
			put("dimgrey", "rgba(105, 105, 105, 1.0)");
			put("dodgerblue", "rgba(30, 144, 255, 1.0)");
			put("firebrick", "rgba(178, 34, 34, 1.0)");
			put("floralwhite", "rgba(255, 250, 240, 1.0)");
			put("forestgreen", "rgba(34, 139, 34, 1.0)");
			put("gainsboro", "rgba(220, 220, 220, 1.0)");
			put("ghostwhite", "rgba(248, 248, 255, 1.0)");
			put("gold", "rgba(255, 215, 0, 1.0)");
			put("goldenrod", "rgba(218, 165, 32, 1.0)");
			put("greenyellow", "rgba(173, 255, 47, 1.0)");
			put("grey", "rgba(128, 128, 128, 1.0)");
			put("honeydew", "rgba(240, 255, 240, 1.0)");
			put("hotpink", "rgba(255, 105, 180, 1.0)");
			put("indianred", "rgba(205, 92, 92, 1.0)");
			put("indigo", "rgba(75, 0, 130, 1.0)");
			put("ivory", "rgba(255, 255, 240, 1.0)");
			put("khaki", "rgba(240, 230, 140, 1.0)");
			put("lavender", "rgba(230, 230, 250, 1.0)");
			put("lavenderblush", "rgba(255, 240, 245, 1.0)");
			put("lawngreen", "rgba(124, 252, 0, 1.0)");
			put("lemonchiffon", "rgba(255, 250, 205, 1.0)");
			put("lightblue", "rgba(173, 216, 230, 1.0)");
			put("lightcoral", "rgba(240, 128, 128, 1.0)");
			put("lightcyan", "rgba(224, 255, 255, 1.0)");
			put("lightgoldenrodyellow", "rgba(250, 250, 210, 1.0)");
			put("lightgray", "rgba(211, 211, 211, 1.0)");
			put("lightgreen", "rgba(144, 238, 144, 1.0)");
			put("lightgrey", "rgba(211, 211, 211, 1.0)");
			put("lightpink", "rgba(255, 182, 193, 1.0)");
			put("lightsalmon", "rgba(255, 160, 122, 1.0)");
			put("lightseagreen", "rgba(32, 178, 170, 1.0)");
			put("lightskyblue", "rgba(135, 206, 250, 1.0)");
			put("lightslategray", "rgba(119, 136, 153, 1.0)");
			put("lightslategrey", "rgba(119, 136, 153, 1.0)");
			put("lightsteelblue", "rgba(176, 196, 222, 1.0)");
			put("lightyellow", "rgba(255, 255, 224, 1.0)");
			put("limegreen", "rgba(50, 205, 50, 1.0)");
			put("linen", "rgba(250, 240, 230, 1.0)");
			put("magenta", "rgba(255, 0, 255, 1.0)");
			put("mediumaquamarine", "rgba(102, 205, 170, 1.0)");
			put("mediumblue", "rgba(0, 0, 205, 1.0)");
			put("mediumorchid", "rgba(186, 85, 211, 1.0)");
			put("mediumpurple", "rgba(147, 112, 219, 1.0)");
			put("mediumseagreen", "rgba(60, 179, 113, 1.0)");
			put("mediumslateblue", "rgba(123, 104, 238, 1.0)");
			put("mediumspringgreen", "rgba(0, 250, 154, 1.0)");
			put("mediumturquoise", "rgba(72, 209, 204, 1.0)");
			put("mediumvioletred", "rgba(199, 21, 133, 1.0)");
			put("midnightblue", "rgba(25, 25, 112, 1.0)");
			put("mintcream", "rgba(245, 255, 250, 1.0)");
			put("mistyrose", "rgba(255, 228, 225, 1.0)");
			put("moccasin", "rgba(255, 228, 181, 1.0)");
			put("navajowhite", "rgba(255, 222, 173, 1.0)");
			put("oldlace", "rgba(253, 245, 230, 1.0)");
			put("olivedrab", "rgba(107, 142, 35, 1.0)");
			put("orange", "rgba(255, 165, 0, 1.0)");
			put("orangered", "rgba(255, 69, 0, 1.0)");
			put("orchid", "rgba(218, 112, 214, 1.0)");
			put("palegoldenrod", "rgba(238, 232, 170, 1.0)");
			put("palegreen", "rgba(152, 251, 152, 1.0)");
			put("paleturquoise", "rgba(175, 238, 238, 1.0)");
			put("palevioletred", "rgba(219, 112, 147, 1.0)");
			put("papayawhip", "rgba(255, 239, 213, 1.0)");
			put("peachpuff", "rgba(255, 218, 185, 1.0)");
			put("peru", "rgba(205, 133, 63, 1.0)");
			put("pink", "rgba(255, 192, 203, 1.0)");
			put("plum", "rgba(221, 160, 221, 1.0)");
			put("powderblue", "rgba(176, 224, 230, 1.0)");
			put("rosybrown", "rgba(188, 143, 143, 1.0)");
			put("royalblue", "rgba(65, 105, 225, 1.0)");
			put("saddlebrown", "rgba(139, 69, 19, 1.0)");
			put("salmon", "rgba(250, 128, 114, 1.0)");
			put("sandybrown", "rgba(244, 164, 96, 1.0)");
			put("seagreen", "rgba(46, 139, 87, 1.0)");
			put("seashell", "rgba(255, 245, 238, 1.0)");
			put("sienna", "rgba(160, 82, 45, 1.0)");
			put("skyblue", "rgba(135, 206, 235, 1.0)");
			put("slateblue", "rgba(106, 90, 205, 1.0)");
			put("slategray", "rgba(112, 128, 144, 1.0)");
			put("slategrey", "rgba(112, 128, 144, 1.0)");
			put("snow", "rgba(255, 250, 250, 1.0)");
			put("springgreen", "rgba(0, 255, 127, 1.0)");
			put("steelblue", "rgba(70, 130, 180, 1.0)");
			put("tan", "rgba(210, 180, 140, 1.0)");
			put("thistle", "rgba(216, 191, 216, 1.0)");
			put("tomato", "rgba(255, 99, 71, 1.0)");
			put("turquoise", "rgba(64, 224, 208, 1.0)");
			put("violet", "rgba(238, 130, 238, 1.0)");
			put("wheat", "rgba(245, 222, 179, 1.0)");
			put("whitesmoke", "rgba(245, 245, 245, 1.0)");
			put("yellowgreen", "rgba(154, 205, 50, 1.0)");
		}
	};
	
	/**
	 * Returns the RGBA equivalence for named colors
	 * @param namedColor
	 * @return
	 */
	public static String getRGBAColor(String namedColor) {
		return colorValues.get(namedColor);
	}
}
