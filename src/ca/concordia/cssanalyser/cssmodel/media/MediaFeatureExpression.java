package ca.concordia.cssanalyser.cssmodel.media;

/**
 * A media feature of the form feature: expression (e.g. min-width: 100px)
 * @author Davood Mazinanian
 *
 */
public class MediaFeatureExpression {
	
	private final String feature;
	private String expression;
	
	public MediaFeatureExpression(String feature) {
		this(feature, "");
	}
	
	public MediaFeatureExpression(String feature, String expression) {
		this.feature = feature;
		this.expression = expression;
	}

	public String getFeature() {
		return feature;
	}

	public String getExpression() {
		return expression;
	}
	
	@Override
	public String toString() {
		return "(" + feature + 
				(!"".equals(expression) ? ": " + expression : "") +
				")"; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((feature == null) ? 0 : feature.hashCode());
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
		MediaFeatureExpression other = (MediaFeatureExpression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (feature == null) {
			if (other.feature != null)
				return false;
		} else if (!feature.equals(other.feature))
			return false;
		return true;
	}
	
	public MediaFeatureExpression clone() {
		return new MediaFeatureExpression(feature, expression);
	}
	
}
