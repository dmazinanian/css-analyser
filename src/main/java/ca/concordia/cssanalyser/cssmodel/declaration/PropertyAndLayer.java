package ca.concordia.cssanalyser.cssmodel.declaration;

/**
 * Represents a style property name, 
 * and the layer to which it belongs.
 * The layer is for multi-layered, comma-separated values,
 * such as in the background declaration. 
 * @author Davood Mazinanian
 *
 */
public class PropertyAndLayer {
	private final String propertyName;
	private final int propertyLayer;
	
	public PropertyAndLayer(String propertyName) {
		this(propertyName, 1);
	}
	
	public PropertyAndLayer(String propertyName, int propertyLayer) {
		this.propertyName = propertyName;
		this.propertyLayer = propertyLayer;
	}
	
	public String getPropertyName() { return this.propertyName; }
	public int getPropertyLayer() { return this.propertyLayer; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + propertyLayer;
		result = prime * result	+ ((propertyName == null) ? 0 : propertyName.hashCode());
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
		PropertyAndLayer other = (PropertyAndLayer) obj;
		if (propertyLayer != other.propertyLayer)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s (layer %s)", propertyName, propertyLayer);
	}
	
}