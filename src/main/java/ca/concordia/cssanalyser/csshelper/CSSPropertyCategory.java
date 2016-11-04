package ca.concordia.cssanalyser.csshelper;

public enum CSSPropertyCategory {
	
	ANIMATION("Animation"),
	AURAL("Aural"),
	BACKGROUND("Background"),
	BEHAVIORAL("Behavioral"),
	BORDER("Border"),
	BOX("Box"),
	COLOR("Color"),
	DEVICE_ADAPTATION("Device Adaptation"),
	FILTER_EFFECTS("Filter Effects"),
	FLEXIBLE_BOX("Flexible Box"),
	FONT("Font"),
	GENERATED_CONTENT("Generated Content"),
	IMAGE_VALUE("Image Value"),
	LISTS("Lists"),
	MARQUEE("Marquee"),
	MASKING("Masking"),
	MULTI_COLUMN_LAYOUT("Multi Column Layout"),
	PAGED_MEDIA("Paged Media"),
	POINTER_EVENTS("Pointer Events"),
	REPLACED_CONTENT("Replaced Content"),
	SPEECH("Speech"),
	SVG("SVG"),
	TABLE("Table"),
	TEXT("Text"),
	TEXT_DECORATION("Text Decoration"),
	TRANSFORM("Transform"),
	TRANSITION("Transition"),
	UI("UI"),
	VIEW("View"),
	WRITING_MODE("Writing Mode"),
	OTHER("Other");
	
	private final String description;

	private CSSPropertyCategory(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
