package ca.concordia.cssanalyser.cssmodel.media;

import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.CSSModelObject;

public class MediaQuery extends CSSModelObject {

	private final int definedInLine;
	private final int definedInColumn;
	
	public enum MediaQueryPrefix {
		NOT,
		ONLY;
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	private final MediaQueryPrefix prefix;
	private String mediaType;
	private final Set<MediaFeatureExpression> mediaFeatureExpresions;
	

	public MediaQuery(String mediaType) {
		this(mediaType, -1, -1);
	}

	public MediaQuery(String mediaTypeName, int line, int col) {
		this(null, mediaTypeName, line, col);
	}

	public MediaQuery(MediaQueryPrefix prefix, String mediaTypeName, int line, int coloumn) {
		definedInLine = line;
		definedInColumn = coloumn;
		mediaType = mediaTypeName;
		mediaFeatureExpresions = new LinkedHashSet<>();
		this.prefix = prefix;
	}

	public String getMediaType() {
		return mediaType;
	}
	
	public void addMediaFeatureExpression(MediaFeatureExpression expression) {
		mediaFeatureExpresions.add(expression);
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		if (this.prefix != null)
			toReturn.append(prefix + " ");
		toReturn.append(mediaType);
		for (MediaFeatureExpression mfe : mediaFeatureExpresions) {
			if (toReturn.length() > 0)
				toReturn.append(" and ");
			toReturn.append(mfe);
		}
		return toReturn.toString();
	}

	@Override
	public MediaQuery clone() {
		MediaQuery toReturn =  new MediaQuery(prefix, mediaType, getLine(), getColumn());
		for (MediaFeatureExpression mfe : mediaFeatureExpresions)
			toReturn.mediaFeatureExpresions.add(mfe.clone());
		return toReturn;
	}

	public int getLine() {
		return definedInLine;
	}

	public int getColumn() {
		return definedInColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + definedInColumn;
		result = prime * result + definedInLine;
		result = prime
				* result
				+ ((mediaFeatureExpresions == null) ? 0
						: mediaFeatureExpresions.hashCode());
		result = prime * result
				+ ((mediaType == null) ? 0 : mediaType.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaQuery other = (MediaQuery) obj;
		if (definedInColumn != other.definedInColumn)
			return false;
		if (definedInLine != other.definedInLine)
			return false;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		if (prefix != other.prefix)
			return false;
		if (mediaFeatureExpresions == null) {
			if (other.mediaFeatureExpresions != null)
				return false;
		} else if (!mediaFeatureExpresions.equals(other.mediaFeatureExpresions))
			return false;
		return true;
	}
	
	
}
