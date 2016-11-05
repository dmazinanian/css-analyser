package ca.concordia.cssanalyser.csshelper;

public enum CSSPropertyCategory {
	
	ANIMATION("Animation", "AN", "#F44336"),
	AURAL("Aural", "AU", "#E91E63"),
	BACKGROUND("Background", "BK", "#9C27B0"),
	BEHAVIORAL("Behavioral", "BH", "#673AB7"),
	BORDER("Border", "BR", "#3F51B5"),
	BOX("Box", "BX", "#2196F3"),
	COLOR("Color", "CR", "#03A9F4"),
	DEVICE_ADAPTATION("Device Adaptation", "DA", "#00BCD4"),
	FILTER_EFFECTS("Filter Effects", "FX", "#009688"),
	FLEXIBLE_BOX("Flexible Box", "FB", "#4CAF50"),
	FONT("Font", "FN", "#8BC34A"),
	GENERATED_CONTENT("Generated Content", "GN", "#CDDC39"),
	IMAGE_VALUE("Image Value", "IM", "#FFEB3B"),
	LISTS("Lists", "LI", "#FFC107"),
	MARQUEE("Marquee", "MQ", "#FF9800"),
	MASKING("Masking", "MK", "#FF5722"),
	MULTI_COLUMN_LAYOUT("Multi Column Layout", "MC", "#795548"),
	PAGED_MEDIA("Paged Media", "PG", "#9E9E9E"),
	POINTER_EVENTS("Pointer Events", "PT", "#607D8B"),
	REPLACED_CONTENT("Replaced Content", "RC", "#FFFFFF"),
	SPEECH("Speech", "SP", "#FFCDD2"),
	SVG("SVG", "SVG", "#F8BBD0"),
	TABLE("Table", "TB", "#E1BEE7"),
	TEXT("Text", "TX", "#B39DDB"),
	TEXT_DECORATION("Text Decoration", "TD", "#9FA8DA"),
	TRANSFORM("Transform", "TF", "#90CAF9"),
	TRANSITION("Transition", "TS", "#81D4FA"),
	UI("UI", "UI", "#80DEEA"),
	VIEW("View", "VW", "#80CBC4"),
	WRITING_MODE("Writing Mode", "WM", "#A5D6A7"),
	OTHER("Other", "OT", "#E6EE9C");
	
	private final String description;
	private final String shortDescription;
	private final String hexColor;

	private CSSPropertyCategory(String description, String shortDescription, String hexColor) {
		this.description = description;
		this.shortDescription = shortDescription;
		this.hexColor = hexColor;
	}
	
	@Override
	public String toString() {
		return description;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getHexColor() {
		return hexColor;
	}
	
}
