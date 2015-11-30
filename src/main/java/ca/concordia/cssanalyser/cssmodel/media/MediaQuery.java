package ca.concordia.cssanalyser.cssmodel.media;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.CSSModelObject;

public class MediaQuery extends CSSModelObject {

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
	

	public MediaQuery(String mediaTypeName) {
		this(null, mediaTypeName);
	}

	public MediaQuery(MediaQueryPrefix prefix, String mediaTypeName) {
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
	
	public Iterable<MediaFeatureExpression> getMediaFeatureExpressions() {
		return mediaFeatureExpresions;
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
		MediaQuery toReturn =  new MediaQuery(prefix, mediaType);
		toReturn.locationInfo = locationInfo;
		for (MediaFeatureExpression mfe : mediaFeatureExpresions)
			toReturn.mediaFeatureExpresions.add(mfe.clone());
		return toReturn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + locationInfo.hashCode();
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
		if (locationInfo == null) {
			if (other.locationInfo != null)
				return false;
		} else {
			if (!locationInfo.equals(other.locationInfo))
				return false;
		}
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
	
	/**
	 * Compares two media queries only based on 
	 * their media types, prefixes and media feature expressions.
	 * Media feature expressions are checked with {@link MediaFeatureExpression#mediaFeatureEquals()}}.
	 * The order of media feature expressions is not important
	 * @param otherMediaQuery
	 * @return
	 */
	public boolean mediaQueryEquals(MediaQuery otherMediaQuery) {
		if (mediaType == null) {
			if (otherMediaQuery.mediaType != null)
				return false;
		} else if (!mediaType.equals(otherMediaQuery.mediaType))
			return false;
		if (prefix != otherMediaQuery.prefix)
			return false;
		if (mediaFeatureExpresions == null) {
			if (otherMediaQuery.mediaFeatureExpresions != null)
				return false;
		} else {
			Set<MediaFeatureExpression> alreadyMatchedFeatureInOther = new HashSet<>();
			if (mediaFeatureExpresions.size() != otherMediaQuery.mediaFeatureExpresions.size())
				return false;
			for (MediaFeatureExpression mediaFeatureExpression : mediaFeatureExpresions) {
				boolean found = false;
				for (MediaFeatureExpression mediaFeatureExpression2 : otherMediaQuery.mediaFeatureExpresions) {
					if (!alreadyMatchedFeatureInOther.contains(mediaFeatureExpression2) &&
							mediaFeatureExpression.mediaFeatureEquals(mediaFeatureExpression2)) {
						alreadyMatchedFeatureInOther.add(mediaFeatureExpression2);
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}			
		}
		
		return true;
	}
}
