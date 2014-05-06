package de.smashnet.elevationlogger;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

import android.content.Context;

/**
 * Represents a sequence of OSM nodes which the user passed
 * at some point in time. One location-node consists of a OSM
 * node ID, the distance to this OSM-node and the measured
 * air pressure.
 * 
 * @author Nicolas Inden
 *
 */
public class LocationTrace implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9004954282610176535L;
	/**
	 * The list of nodes this trace consists of.
	 */
	private LinkedList<LocationNode> mNodes;
	/**
	 * Date and time when this trace was recorded.
	 */
	private Date mDate;
	/**
	 * True, if this trace has already been uploaded.
	 */
	private boolean mUploaded;
	Context mContext;
	File mDir;
	
	/**
	 * 
	 * @param con the context that is passed to the GpxWriter
	 * @param dir the folder where the gpx of this trace is saved
	 */
	public LocationTrace(Context con, File dir) {
		mContext = con;
		mDir = dir;
		mNodes = new LinkedList<LocationNode>();
		setDate(new Date());
	}
	
	/**
	 * Adds a LocationNode to this trace and writes appropriate gpx content
	 * to GpxWriter buffer.
	 * 
	 * @param node
	 */
	public void addNode(LocationNode node) {
		mNodes.add(node);
	}
	
	/**
	 * @return the list of LocationNodes
	 */
	public LinkedList<LocationNode> getNodes() {
		return mNodes;
	}
	
	/**
	 * Create a standard conform GPX file from this trace.
	 */
	public void writeToGPXFile() {
		GpxWriter gpxWriter = new GpxWriter(mContext, getDate(), mDir, "record.gpx");
		gpxWriter.writeHeader();
		
		for(LocationNode node : mNodes) {
			gpxWriter.addRoutePoint(node.getLatitude(), node.getLongitude(), node.getGPSAltitude(),
					node.getAccuracy(), node.getAirPressure(), node.getDateVisited(), node.getOSMNode());
			gpxWriter.flushToFile();
		}
		gpxWriter.writeFooter();
	}

	/**
	 * @return the mUploaded
	 */
	public boolean isUploaded() {
		return mUploaded;
	}

	/**
	 * @param mUploaded the mUploaded to set
	 */
	public void setUploaded(boolean mUploaded) {
		this.mUploaded = mUploaded;
	}

	/**
	 * @return the mDate
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * @param mDate the mDate to set
	 */
	public void setDate(Date mDate) {
		this.mDate = mDate;
	}
}
