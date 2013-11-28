package ca.concordia.cssanalyser.cssmodel.media;

public class AtomicMedia extends Media {

	private String name;

	public AtomicMedia(String mediaName) {
		this(mediaName, -1, -1);
	}

	public AtomicMedia(String mediaName, int line, int col) {
		super(line, col);
		name = mediaName;
	}

	public String getMediaName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AtomicMedia))
			return false;
		AtomicMedia otherMedia = (AtomicMedia) obj;
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
