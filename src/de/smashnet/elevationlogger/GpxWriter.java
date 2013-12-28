package de.smashnet.elevationlogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GpxWriter {
	private String filename;
	private File directory;
	private Date time;
	private StringBuilder sb;
	
	public GpxWriter(String file, File dir) {
		time = new Date();
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMdd-HHmm", Locale.GERMANY);
		String date = sDateFormat.format(time);
		
		filename = date + "_" + file;
		directory = dir;
		sb = new StringBuilder();
	}
	
	public void writeHeader() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMdd-HHmm", Locale.GERMANY);
		String date = sDateFormat.format(time);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		sb.append("<gpx version=\"1.1\" creator=\"ElevationLogger v0.1 experimental\">\n");
		sb.append("\t<metadata>\n");
		sb.append("\t\t<name>" + filename + "</name>\n");
		sb.append("\t\t<desc>Recording started " + date + "</desc>\n");
		sb.append("\t\t<author>\n");
		sb.append("\t\t\t<name>Nicolas Inden</name>\n");
		sb.append("\t\t</author>\n");
		sb.append("\t</metadata>\n");
		sb.append("\t<rte>\n");
		sb.append("\t\t<name>ElevationLogger recording</name>\n");
		sb.append("\t\t<desc></desc>\n");
	}
	
	public void writeFooter() {
		sb.append("\t</rte>\n");
		sb.append("</gpx>\n");
	}
	
	public void addRoutePoint(double lat, double lon, double alt, float acc, float mbar, long time) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY);
		String isoTime = df.format(new Date(time));
		sb.append("\t\t<rtept lat=\"" + lat + "\" lon=\"" + lon + "\">\n");
		sb.append("\t\t\t<ele>" + alt + "</ele>\n");
		sb.append("\t\t\t<time>" + isoTime + "</time>\n");
		sb.append("\t\t\t<desc>mbar:" + mbar + ",acc:" + acc + "</desc>\n");
		sb.append("\t\t</rtept>\n");
	}
	
	public void flushToFile() {
		try {
			File measurefile = new File(directory, filename);
			if(!measurefile.createNewFile())
					System.out.println("Couldn't create file");
			
			FileWriter measurefile_writer = new FileWriter(measurefile, true);
			measurefile_writer.append(sb.toString());
			measurefile_writer.close();
			
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