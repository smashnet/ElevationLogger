package de.smashnet.elevationlogger;

import java.io.File;

import jsqlite.Exception;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SensorService lets GPS and air pressure sensors run even
 * if the app is not on foreground.
 * 
 * If the HomeActivity is in foreground the sensor values are passed
 * there using LocalBroadcastManager.
 * 
 * @author Nicolas Inden
 * @contact nicolas.inden@smashnet.de
 * @date 29.12.2013
 */
public class SensorService extends Service
			 implements LocationListener, SensorEventListener{
	
	/**
	 * Handles sensor service
	 */
	SensorManager mSensorManager;
	
	/**
	 * Handles location service
	 */
	LocationManager mLocationManager;
	
	/**
	 * We use the air pressure sensor
	 */
	Sensor mPressure;
	
	/**
	 * Source for location information
	 */
	String mProvider;
	
	/**
	 * Last measured air pressure
	 */
	float currentPressure = 0.0f;
	
	/**
	 * Used to Schmitt-triggering data recording
	 */
	boolean recording;
	
	/**
	 * Writes values to a GPX file
	 */
	GpxWriter mGpxWriter;
	
	/**
	 * Spatialite Database
	 */
	jsqlite.Database mDatabase;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
	}
	
	/**
	 * Do some cleanup if the Service is destroyed/stopped:
	 * <ul>
	 * 	<li>Unregister SensorManager listener</li>
	 * 	<li>Stop LocationManager updates</li>
	 * 	<li>Write GPX footer and flush to file</li>
	 * </ul>
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("SensorService", "Service stopped!");
		
		// Unregister sensors and finish GPX file
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		if(mGpxWriter != null){
			mGpxWriter.writeFooter();
			mGpxWriter.flushToFile();
		}
		try {
			mDatabase.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("SensorService", "Error closing database!");
		}
	}
	
	/**
	 * This is invoked when the Service is started. It initializes the air pressure sensor,
	 * the GPS LocationManager and the GpxWriter.
	 * 
	 * @param intent the intent
	 * @param flags the flags
	 * @param startId the startId
	 * @return The service mode. In this case: START_STICKY (only stop if explicitly asked to stop)
	 */
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("SensorService", "Received start id " + startId + ": " + intent);
		recording = false;
        
		// Init air pressure sensor
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mSensorManager.registerListener(this, mPressure, 500000); // 500ms
		Log.i("SensorService", "Started air pressure");
			    
		// Init GPS
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mProvider = LocationManager.GPS_PROVIDER;
		if(mProvider != null){
			mLocationManager.requestLocationUpdates(mProvider, 400, 0, this);
			Log.i("SensorService", "Started GPS");
		}
		
		//Init Spatialite database
		mDatabase = new jsqlite.Database();
		try {
			mDatabase.open(getStorageDir("ElevationLog","map") + "/" + getString(R.string.osm_db), jsqlite.Constants.SQLITE_OPEN_READONLY);
			Log.i("SensorService", "Successfully opened spatialite database :)");
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("SensorService", "Error opening spatialite database!");
		}
			    
		// GpxWriter is initialized in onLocationChanged()
		
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Nothing to do here so far ;-)
		
	}

	/**
	 * We use this function to broadcast air pressure values to the HomeActivity to have
	 * a more frequent update for the air pressure. Nevertheless, no data recording here
	 * without a position.
	 * 
	 * @param event the SensorEvent containing the sensor value
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		currentPressure = event.values[0];
		Intent intent = new Intent("sensor-data-pressure");
		intent.putExtra("pres", currentPressure);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	/**
	 * This is invoked each time we receive a new location from the LocationManager. The values
	 * are stored into a GPX file and additionally are broadcasted to the HomeActivity
	 * for displaying purposes.
	 * 
	 * @param location the location object
	 */
	@Override
	public void onLocationChanged(Location location) {
		//Check if required information is available
		if(!location.hasAccuracy() || !location.hasAltitude() || !location.hasSpeed())
			return;
		
		// Schmitt-trigger data recording depending on GPS accuracy
		if(location.getAccuracy() <= 14.0 && !recording){
			recording = true;
			
			mGpxWriter = new GpxWriter(this, "record.gpx", getStorageDir("ElevationLog","measurements"));
			mGpxWriter.writeHeader();
		}else if(location.getAccuracy() > 18.0 && recording){
			recording = false;
			
			mGpxWriter.writeFooter();
			mGpxWriter.flushToFile();
		}
		
		// Broadcast sensor-data to HomeActivity
		Intent intent = new Intent("sensor-data-complete");
		intent.putExtra("lat", location.getLatitude());
		intent.putExtra("lon", location.getLongitude());
		intent.putExtra("alt", location.getAltitude());
		intent.putExtra("acc", location.getAccuracy());
		intent.putExtra("pres", currentPressure);
		intent.putExtra("time", location.getTime());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
		// Sensor data log output
		Log.i("SensorService", "Lat: " + location.getLatitude());
		Log.i("SensorService", "Lon: " + location.getLongitude());
	    Log.i("SensorService", "Alt: " + location.getAltitude());
	    Log.i("SensorService", "Acc: " + location.getAccuracy());
	    Log.i("SensorService", "Pres: " + currentPressure);
	    Log.i("SensorService", "Time: " + location.getTime());
	    
	    // Save sensor-data to GPX file if GPS accuracy is <= 15 meters
	    if(!recording)
			return;
	 	mGpxWriter.addRoutePoint(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy(), currentPressure, location.getTime());
	 	mGpxWriter.flushToFile();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Nothing to do here so far ;-)
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Nothing to do here so far ;-)
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Nothing to do here so far ;-)
		
	}

	/**
	 * Returns a File object for ExternalStoragePublicDirectory/$progname/$dirname
	 * If the directories do not exists yet, they are created
	 * 
	 * @param progname the name of this app
	 * @param dirname the name of the subdirectory where data should be stored
	 * @return the File object representing the storage directory
	 */
	public File getStorageDir(String progname, String dirname) {
        // Get the directory for the user's public pictures directory. 
        File file = new File(Environment.getExternalStoragePublicDirectory(progname), dirname);
        if (!file.exists()) {
        	if(!file.mkdirs())
        		System.out.println("Directory not created");
        } else {
        	System.out.println("Directory exists!");
        }
        return file;
    }
}
