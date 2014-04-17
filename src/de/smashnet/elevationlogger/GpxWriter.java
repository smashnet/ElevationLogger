package de.smashnet.elevationlogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
public class GpxWriter {
	/**
	 * Name of the gpx file
	 */
	private String filename;
	
	/**
	 * Directory where the file should be stored
	 */
	private File directory;
	
	/**
	 * Creation date of this file
	 */
	private Date time;
	
	/**
	 * Helps buffering contents before they are written to file
	 */
	private StringBuilder sb;
	
	/**
	 * If a file is finished, all following route points are ignored
	 * A file is finished if the footer is written. 
	 */
	private boolean finished = false;
	
	/**
	 * The context this class was instantiated in
	 */
	private Context context;
	
	/**
	 * Basic constructor
	 * 
	 * @param file is used as filename but gets the date prepended at the beginning
	 * @param dir the directory where the file should be saved
	 */
	public GpxWriter(Context context, String file, File dir) {
		time = new Date();
		
		// Create readable date string
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMdd-HHmm", Locale.GERMANY);
		String date = sDateFormat.format(time);
		
		filename = date + "_" + file;
		directory = dir;
		sb = new StringBuilder();
		this.context = context;
	}
	
	/**
	 * Writes a standard GPX file header in the StringBuilder buffer
	 */
	public void writeHeader() {
		Log.i("GpxWriter", "Logging to file: " + filename);
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
		String date = sDateFormat.format(time);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		sb.append("<gpx version=\"1.1\" creator=\"ElevationLogger v0.1 experimental\">\n");
		sb.append("\t<metadata>\n");
		sb.append("\t\t<name>" + filename + "</name>\n");
		sb.append("\t\t<time>" + date + "</time>\n");
		sb.append("\t\t<author>\n");
		sb.append("\t\t\t<name>ElevationLogger</name>\n");
		sb.append("\t\t</author>\n");
		sb.append("\t</metadata>\n");
		sb.append("\t<trk>\n");
		sb.append("\t\t<name>ElevationLogger recording</name>\n");
		sb.append("\t\t<desc></desc>\n");
		sb.append("\t\t<trkseg>\n");
	}
	
	/**
	 * Writes a standard GPX file footer in the StringBuilder buffer and declares
	 * this file as finished
	 */
	public void writeFooter() {
		sb.append("\t\t</trkseg>\n");
		sb.append("\t</trk>\n");
		sb.append("</gpx>\n");
		finished = true;
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
	public void addRoutePoint(double lat, double lon, double alt, float acc, float mbar, long time) {
		if(finished)
			return;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.GERMANY);
		String isoTime = df.format(new Date(time));
		sb.append("\t\t\t<trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n");
		sb.append("\t\t\t\t<ele>" + alt + "</ele>\n");
		sb.append("\t\t\t\t<time>" + isoTime + "</time>\n");
		sb.append("\t\t\t\t<extensions>\n");
		sb.append("\t\t\t\t\t<accuracy>" + acc + "</accuracy>\n");
		sb.append("\t\t\t\t\t<airpressure>" + mbar + "</airpressure>\n");
		sb.append("\t\t\t\t</extensions>\n");
		sb.append("\t\t\t</trkpt>\n");
	}
	
	/**
	 * Takes all contents of the StringBuilder buffer
	 * and writes it to the storage device, finally clearing
	 * the StringBuilder buffer
	 */
	public void flushToFile() {
		try {
			File measurefile = new File(directory, filename);
			if(!measurefile.exists())
				if(!measurefile.createNewFile())
					System.out.println("Couldn't create file");
			
			FileWriter measurefile_writer = new FileWriter(measurefile, true);
			measurefile_writer.append(sb.toString());
			measurefile_writer.close();
			this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(measurefile)));
			
			// Clear stringbuilder
			sb = new StringBuilder();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}
}