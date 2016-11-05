package ca.concordia.cssanalyser.csshelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CSSPropertyCategoryHelper {
	
	@SuppressWarnings("serial")
	private static final Map<CSSPropertyCategory, List<String>> moduleToPropertiesMap = new HashMap<CSSPropertyCategory, List<String>>() {
		{
		put(CSSPropertyCategory.ANIMATION,
				Arrays.asList(new String[] {
						"animation",
						"animation-delay",
						"animation-direction",
						"animation-duration",
						"animation-fill",
						"animation-fill-mode",
						"animation-iteration-count",
						"animation-name",
						"animation-timing-function" }
						)
				);

		put(CSSPropertyCategory.AURAL,
				Arrays.asList(new String[] {
						"azimuth",
						"cue",
						"cue-after",
						"cue-before",
						"elevation",
						"pause",
						"pause-after",
						"pause-before",
						"pitch",
						"pitch-range",
						"play-during",
						"richness",
						"speak",
						"speak-header",
						"speak-numeral",
						"speak-punctuation",
						"speech-rate",
						"stress",
						"voice-family",
						"volume" }
						)
				);

		put(CSSPropertyCategory.BACKGROUND,
				Arrays.asList(new String[] {
						"background",
						"background-attachement",
						"background-blend-mode",
						"background-attachment",
						"background-clip",
						"background-color",
						"background-image",
						"background-inline-policy",
						"background-origin",
						"background-position",
						"background-position-x",
						"background-position-y",
						"background-repeat",
						"background-scroll",
						"background-size" }
						)
				);

		put(CSSPropertyCategory.BEHAVIORAL,
				Arrays.asList(new String[] {
						"behavior" }
						)
				);

		put(CSSPropertyCategory.BORDER,
				Arrays.asList(new String[] {
						"border",
						"border-bottom",
						"border-bottom-color",
						"border-bottom-colors",
						"border-bottom-left-radius",
						"border-bottom-right-radius",
						"border-bottom-style",
						"border-bottom-width",
						"border-color",
						"border-image",
						"border-left",
						"border-left-color",
						"border-left-colors",
						"border-left-style",
						"border-left-width",
						"border-radisu",
						"border-radius",
						"border-radius-bottomleft",
						"border-radius-bottom-left",
						"border-radius-bottomright",
						"border-radius-bottom-right",
						"border-radius-topleft",
						"border-radius-top-left",
						"border-radius-topright",
						"border-radius-top-right",
						"border-right",
						"border-right-color",
						"border-right-colors",
						"border-right-style",
						"border-right-width",
						"border-style",
						"border-top",
						"border-top-color",
						"border-top-colors",
						"border-top-left-radius",
						"border-top-radius",
						"border-top-right-radius",
						"border-top-style",
						"border-top-width",
						"border-width",
						"border-width-bottom",
						"box-shadow",
						"z-index" }
						)
				);

		put(CSSPropertyCategory.BOX,
				Arrays.asList(new String[] {
						"bottom",
						"clear",
						"clip",
						"display",
						"float",
						"height",
						"left",
						"margin",
						"margin-bottom",
						"marging-left",
						"marginheight",
						"margin-left",
						"margin-right",
						"margin-top",
						"margin-top-collapse",
						"max-height",
						"max-width",
						"min-height",
						"min-width",
						"overflow",
						"overflow-x",
						"overflow-y",
						"padding",
						"padding-bottom",
						"padding-display",
						"padding-left",
						"padding-right",
						"padding-start",
						"padding-top",
						"position",
						"right",
						"top",
						"vertical-align",
						"visibility",
						"width" }
						)
				);

		put(CSSPropertyCategory.COLOR,
				Arrays.asList(new String[] {
						"color",
						"opacity" }
						)
				);

		put(CSSPropertyCategory.DEVICE_ADAPTATION,
				Arrays.asList(new String[] {
						"zoom" }
						)
				);

		put(CSSPropertyCategory.FILTER_EFFECTS,
				Arrays.asList(new String[] {
						"filter" }
						)
				);

		put(CSSPropertyCategory.FLEXIBLE_BOX,
				Arrays.asList(new String[] {
						"align-items",
						"align-content",
						"FLEXIBLE_BOX",
						"box-align",
						"box-flex",
						"box-orient",
						"box-pack",
						"flex",
						"flex-align",
						"flex-flow",
						"flex-pack",
						"flex-basis",
						"flex-grow",
						"flex-direction",
						"flex-shrink",
						"flex-wrap",
						"order",
						"justify-content" }
						)
				);

		put(CSSPropertyCategory.FONT,
				Arrays.asList(new String[] {
						"font",
						"font-color",
						"font-face",
						"font-family",
						"font-size",
						"font-smooth",
						"font-smoothing",
						"font-stretch",
						"font-style",
						"font-variant",
						"font-weight",
						"font-feature-settings",
						"font-language-override",
						"font-kerning",
						"font-synthesis",
						"font-variant",
						"force-broken-image-icon",
						"font-variant-caps",
						"font-variant-ligatures",
						"font-variant-east-asian",
						"font-variant-numeric",
						"font-variant-position",
						"font-variant-alternates" }
						)
				);

		put(CSSPropertyCategory.GENERATED_CONTENT,
				Arrays.asList(new String[] {
						"marks",
						"quotes" }
						)
				);

		put(CSSPropertyCategory.IMAGE_VALUE,
				Arrays.asList(new String[] {
						"image-orientation",
						"image-resolution",
						"image-rendering",
						"interpolation-mode",
						"element" }
						)
				);

		put(CSSPropertyCategory.LISTS,
				Arrays.asList(new String[] {
						"counter-increment",
						"counter-reset",
						"list-style",
						"list-style-color",
						"list-style-image",
						"list-style-position",
						"list-style-style",
						"list-style-type" }
						)
				);

		put(CSSPropertyCategory.MARQUEE,
				Arrays.asList(new String[] {
						"marquee-direction",
						"marquee-play-count",
						"marquee-speed",
						"marquee-style" }
						)
				);

		put(CSSPropertyCategory.MASKING,
				Arrays.asList(new String[] {
						"mask-box-image",
						"mask-image",
						"mask-type",
						"mask" }
						)
				);

		put(CSSPropertyCategory.MULTI_COLUMN_LAYOUT,
				Arrays.asList(new String[] {
						"column-count",
						"column-fill",
						"column-gap",
						"column-rule",
						"column-rule-color",
						"column-rule-style",
						"column-rule-width",
						"columns",
						"column-span",
						"column-width",
						"break-after",
						"break-before",
						"break-inside",
						"widows" }
						)
				);

		put(CSSPropertyCategory.PAGED_MEDIA,
				Arrays.asList(new String[] {
						"orphans",
						"page-break-before",
						"page-break-after",
						"page-break-inside",
						"size" }
						)
				);

		put(CSSPropertyCategory.POINTER_EVENTS,
				Arrays.asList(new String[] {
						"touch-action" }
						)
				);

		put(CSSPropertyCategory.REPLACED_CONTENT,
				Arrays.asList(new String[] {
						"object-position",
						"object-fit" }
						)
				);

		put(CSSPropertyCategory.SPEECH,
				Arrays.asList(new String[] {
						"mark",
						"mark-after",
						"mark-before",
						"phonemes",
						"rest",
						"rest-after",
						"rest-before",
						"voice-balance",
						"voice-duration",
						"voice-pitch",
						"voice-pitch-range",
						"voice-rate",
						"voice-stress",
						"voice-volume" }
						)
				);

		put(CSSPropertyCategory.SVG,
				Arrays.asList(new String[] {
						"fill",
						"fill-opacity",
						"pointer-events",
						"shape-rendering",
						"stroke",
						"stroke-dasharray",
						"stroke-opacity",
						"stroke-width",
						"text-anchor",
						"text-rendering" }
						)
				);

		put(CSSPropertyCategory.TABLE,
				Arrays.asList(new String[] {
						"border-collapse",
						"border-spacing",
						"caption-side",
						"empty-cells",
						"table-layout" }
						)
				);

		put(CSSPropertyCategory.TEXT,
				Arrays.asList(new String[] {
						"tap-highlight-color",
						"text-shadow",
						"text-size-adjust",
						"hanging-punctuation" }
						)
				);

		put(CSSPropertyCategory.TEXT_DECORATION,
				Arrays.asList(new String[] {
						"text-decoration-color",
						"text-decoration-line",
						"text-decoration-style",
						"text-decoration",
						"text-underline-position",
						"text-shadow" }
						)
				);

		put(CSSPropertyCategory.TEXT,
				Arrays.asList(new String[] {
						"hyphens",
						"letter-spacing",
						"line-break",
						"overflow-wrap",
						"line-height",
						"tab-size",
						"text-align",
						"text-align-last",
						"text-combine-upright",
						"text-indent",
						"text-transform",
						"white-space",
						"word-break",
						"word-spacing",
						"word-wrap" }
						)
				);

		put(CSSPropertyCategory.TRANSFORM,
				Arrays.asList(new String[] {
						"backface-visibility",
						"perspective",
						"perspective-origin",
						"perspective-origin-y",
						"transform",
						"transform-origin",
						"transform-style" }
						)
				);

		put(CSSPropertyCategory.TRANSITION,
				Arrays.asList(new String[] {
						"transition",
						"transition-delay",
						"transition-duration",
						"transition-property",
						"transition-timing-function" }
						)
				);

		put(CSSPropertyCategory.UI,
				Arrays.asList(new String[] {
						"appearance",
						"box-sizing",
						"content",
						"cursor",
						"cursor-visibility",
						"ime-mode",
						"nav-down",
						"nav-index",
						"nav-left",
						"nav-up",
						"nav-right",
						"outline",
						"outline-color",
						"outline-offset",
						"outline-style",
						"outline-width",
						"resize",
						"resize",
						"text-overflow",
						"touch-callout",
						"touch-select",
						"user-drag",
						"user-focus-key",
						"user-focus-poiner",
						"user-focus",
						"user-input",
						"user-select",
						"user-modify" }
						)
				);

		put(CSSPropertyCategory.VIEW,
				Arrays.asList(new String[] {
						"scroll-behavior" }
						)
				);

		put(CSSPropertyCategory.WRITING_MODE,
				Arrays.asList(new String[] {
						"text-combine-upright ",
						"text-orientation",
						"direction",
						"unicode-bidi",
						"writing-mode",
						"unicode-bidi" }
						)
				);
		}
	};
	
	@SuppressWarnings("serial")
	private static final Map<String, CSSPropertyCategory> propertyToModuleMap = new HashMap<String, CSSPropertyCategory>() {
		{
			for (CSSPropertyCategory category : moduleToPropertiesMap.keySet()) {
				List<String> properties = moduleToPropertiesMap.get(category);
				for (String property : properties) {
					put(property, category);
				}
			}
		}
	};
	
	public static CSSPropertyCategory getCSSCategoryOfProperty(String property) {
		CSSPropertyCategory cssPropertyCategory = propertyToModuleMap.get(property);
		if (cssPropertyCategory == null) {
			cssPropertyCategory = CSSPropertyCategory.OTHER;
		}
		return cssPropertyCategory;
	}
	
	public static List<String> getCSSPropertiesForCategory(CSSPropertyCategory category) {
		return moduleToPropertiesMap.get(category);
	}
	
	public static List<String> getCSSPropertiesForCategory(String categoryName) {
		return moduleToPropertiesMap.get(CSSPropertyCategory.valueOf(categoryName));
	}
	
}
