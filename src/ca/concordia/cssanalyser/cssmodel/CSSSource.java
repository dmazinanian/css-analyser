package ca.concordia.cssanalyser.cssmodel;

public abstract class CSSSource {
	
	public enum CSSSourceType {
		INLINE,
		INTERNAL,
		EXTERNAL
	}
	
	private final CSSSourceType type;
	
	public CSSSource() {
		type = CSSSourceType.EXTERNAL;
	}
	
	public CSSSource(CSSSourceType type) {
		this.type = type;
	}
	
	public CSSSourceType getType() {
		return type;
	}
}
