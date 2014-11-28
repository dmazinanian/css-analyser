package ca.concordia.cssanalyser.cssmodel;

/**
 * All the CSS model objects extend this object
 * @author Davood
 *
 */
public abstract class CSSModelObject {
	
	protected LocationInfo locationInfo;
	
	public LocationInfo getLocationInfo() {
		return locationInfo;
	}

	public void setLocationInfo(LocationInfo locationInfo) {
		this.locationInfo = locationInfo;
	}

}
