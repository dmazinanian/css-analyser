package ca.concordia.cssanalyser.cssmodel.media;

public class SingleMedia extends Media {

	private String name;

	public SingleMedia(String mediaName) {
		this(mediaName, -1, -1);
	}

	public SingleMedia(String mediaName, int line, int col) {
		super(line, col);
		name = mediaName;
	}

	public String getMediaName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SingleMedia))
			return false;
		SingleMedia otherMedia = (SingleMedia) obj;
		return name.equals(otherMedia.name);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

}
