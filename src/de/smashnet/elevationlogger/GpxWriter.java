package de.smashnet.elevationlogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * This class should simplify the creation of GPX files.
 * 
 * @author Nicolas Inden
 * @contact nicolas.inden@smashnet.de
 * @date 29.12.2013
 */
public class GpxWriter implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3172959510015296715L;

	/**
	 * Name of the gpx file
	 */
	private String mFilename;
	
	/**
	 * Directory where the file should be stored
	 */
	private File mDirectory;
	
	/**
	 * Creation date of this file
	 */
	private Date mTime;
	
	/**
	 * Helps buffering contents before they are written to file
	 */
	private StringBuilder mSB;
	
	/**
	 * If a file is finished, all following route points are ignored
	 * A file is finished if the footer is written. 
	 */
	private boolean mFinished = false;
	
	/**
	 * Basic constructor
	 * 
	 * @param file is used as filename but gets the date prepended at the beginning
	 * @param dir the directory where the file should be saved
	 */
	public GpxWriter(Date time, File dir, String file) {
		mTime = time;
		
		// Create readable date string
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMdd-HHmm", Locale.GERMANY);
		String date = sDateFormat.format(mTime);
		
		mFilename = date + "_" + file;
		mDirectory = dir;
		mSB = new StringBuilder();
	}
	
	/**
	 * Writes a standard GPX file header in the StringBuilder buffer
	 */
	public void writeHeader() {
		Log.i("GpxWriter", "Logging to file: " + mFilename);
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
		String date = sDateFormat.format(mTime);
		mSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		mSB.append("<gpx version=\"1.1\" creator=\"ElevationLogger v0.1 experimental\">\n");
		mSB.append("\t<metadata>\n");
		mSB.append("\t\t<name>" + mFilename + "</name>\n");
		mSB.append("\t\t<time>" + date + "</time>\n");
		mSB.append("\t\t<author>\n");
		mSB.append("\t\t\t<name>ElevationLogger</name>\n");
		mSB.append("\t\t</author>\n");
		mSB.append("\t</metadata>\n");
		mSB.append("\t<trk>\n");
		mSB.append("\t\t<name>ElevationLogger recording</name>\n");
		mSB.append("\t\t<desc></desc>\n");
		mSB.append("\t\t<trkseg>\n");
	}
	
	/**
	 * Writes a standard GPX file footer in the StringBuilder buffer and declares
	 * this file as finished
	 */
	public void writeFooter() {
		mSB.append("\t\t</trkseg>\n");
		mSB.append("\t</trk>\n");
		mSB.append("</gpx>\n");
		mFinished = true;
	}
	
	/**
	 * Add a route point to the StringBuidler buffer with the following values:
	 * 
	 * @param lat the latitude value
	 * @param lon the longitude value
	 * @param alt the altitude value
	 * @param acc the accuracy of lat/lon
	 * @param mbar the air pressure
	 * @param time the UTC time of the corresponding GPS fix
	 */
	public void addRoutePoint(double lat, double lon, double alt, float acc, double mbar, long time, long osm) {
		if(mFinished)
			return;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
		String isoTime = df.format(new Date(time));
		mSB.append("\t\t\t<trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n");
		mSB.append("\t\t\t\t<ele>" + alt + "</ele>\n");
		mSB.append("\t\t\t\t<time>" + isoTime + "</time>\n");
		mSB.append("\t\t\t\t<extensions>\n");
		mSB.append("\t\t\t\t\t<accuracy>" + acc + "</accuracy>\n");
		mSB.append("\t\t\t\t\t<airpressure>" + mbar + "</airpressure>\n");
		mSB.append("\t\t\t\t\t<nearestosm>" + osm + "</nearestosm>\n");
		mSB.append("\t\t\t\t</extensions>\n");
		mSB.append("\t\t\t</trkpt>\n");
	}
	
	/**
	 * Takes all contents of the StringBuilder buffer
	 * and writes it to the storage device, finally clearing
	 * the StringBuilder buffer
	 */
	public void flushToFile() {
		try {
			File measurefile = new File(mDirectory, mFilename);
			if(!measurefile.exists())
				if(!measurefile.createNewFile())
					System.out.println("Couldn't create file");
			
			FileWriter measurefile_writer = new FileWriter(measurefile, true);
			measurefile_writer.append(mSB.toString());
			measurefile_writer.close();
			
			// Clear stringbuilder
			mSB = new StringBuilder();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFilename() {
		return mFilename;
	}

	public void setFilename(String filename) {
		this.mFilename = filename;
	}

	public File getDirectory() {
		return mDirectory;
	}

	public void setDirectory(File directory) {
		this.mDirectory = directory;
	}
}