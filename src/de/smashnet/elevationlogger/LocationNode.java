package de.smashnet.elevationlogger;

import java.io.Serializable;

import android.location.Location;

/**
 * Represents one node in a LocationTrace.
 * 
 * @author Nicolas Inden
 *
 */
public class LocationNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6550849231918696225L;
	/**
	 * The GPS latitude of this node.
	 */
	private double mLatitude;
	/**
	 * The GPS longitude of this node.
	 */
	private double mLongitude;
	/**
	 * GPS accuracy in meters while these coordinates were determined.
	 */
	private float mAccuracy;
	/**
	 * The nearest OpenStreetMap node to the given GPS coordinates.
	 */
	private long mOSMNode;
	/**
	 * The distance between GPS coordinates and OSM node.
	 */
	private double mDistance;
	/**
	 * The measured air pressure in mbar for these coordinates.
	 */
	private double mAirPressure;
	/**
	 * Altitude received from GPS.
	 */
	private double mGPSAltitude;
	/**
	 * The date and time this node was visited.
	 */
	private long mDateVisited;
	
	public LocationNode(Location loc, long osmNode, double dist, double pressure) {
		setLatitude(loc.getLatitude());
		setLongitude(loc.getLongitude());
		setAccuracy(loc.getAccuracy());
		setOSMNode(osmNode);
		setDistance(dist);
		setAirPressure(pressure);
		setGPSAltitude(loc.getAltitude());
		setDateVisited(loc.getTime());
	}

	/**
	 * @return the mLatitude
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * @param mLatitude the mLatitude to set
	 */
	public void setLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}

	/**
	 * @return the mLongitude
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * @param mLongitude the mLongitude to set
	 */
	public void setLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}

	/**
	 * @return the mAccuracy
	 */
	public float getAccuracy() {
		return mAccuracy;
	}

	/**
	 * @param mAccuracy the mAccuracy to set
	 */
	public void setAccuracy(float mAccuracy) {
		this.mAccuracy = mAccuracy;
	}

	/**
	 * @return the mOSMNode
	 */
	public long getOSMNode() {
		return mOSMNode;
	}

	/**
	 * @param mOSMNode the mOSMNode to set
	 */
	public void setOSMNode(long mOSMNode) {
		this.mOSMNode = mOSMNode;
	}

	/**
	 * @return the mDistance
	 */
	public double getDistance() {
		return mDistance;
	}

	/**
	 * @param mDistance the mDistance to set
	 */
	public void setDistance(double mDistance) {
		this.mDistance = mDistance;
	}

	/**
	 * @return the mAirPressure
	 */
	public double getAirPressure() {
		return mAirPressure;
	}

	/**
	 * @param mAirPressure the mAirPressure to set
	 */
	public void setAirPressure(double mAirPressure) {
		this.mAirPressure = mAirPressure;
	}

	/**
	 * @return the mGPSAltitude
	 */
	public double getGPSAltitude() {
		return mGPSAltitude;
	}

	/**
	 * @param mGPSAltitude the mGPSAltitude to set
	 */
	public void setGPSAltitude(double mGPSAltitude) {
		this.mGPSAltitude = mGPSAltitude;
	}

	/**
	 * @return the mDateVisited
	 */
	public long getDateVisited() {
		return mDateVisited;
	}

	/**
	 * @param mDateVisited the mDateVisited to set
	 */
	public void setDateVisited(long mDateVisited) {
		this.mDateVisited = mDateVisited;
	}

}
