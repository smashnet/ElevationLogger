package de.smashnet.elevationlogger;

import java.io.File;

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

public class SensorService extends Service
			 implements LocationListener, SensorEventListener{
	
	SensorManager mSensorManager;
	Sensor mPressure;
	LocationManager mLocationManager;
	String mProvider;
	float currentPressure = 0.0f;
	GpxWriter mGpxWriter;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("SensorServie", "Service destroyed!");
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		mGpxWriter.writeFooter();
		mGpxWriter.flushToFile();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
        
		// Init air pressure sensor
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mSensorManager.registerListener(this, mPressure, 500000); // 500ms
		Log.i("LocalService", "Started air pressure");
			    
		// Init GPS
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mProvider = LocationManager.GPS_PROVIDER;
		if(mProvider != null){
			mLocationManager.requestLocationUpdates(mProvider, 400, 0, this);
			Log.i("LocalService", "Started GPS");
		}
			    
		// Create GpxWriter
		mGpxWriter = new GpxWriter("record.gpx", getStorageDir());
		mGpxWriter.writeHeader();
		
		// We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		currentPressure = event.values[0];
	}

	@Override
	public void onLocationChanged(Location location) {
		mGpxWriter.addRoutePoint(location.getLatitude(), location.getLongitude(),
				location.getAltitude(), location.getAccuracy(), currentPressure, location.getTime());
		mGpxWriter.flushToFile();
		
		// Broadcast sensor-data to HomeActivity
		Intent intent = new Intent("sensor-data");
		intent.putExtra("lat", location.getLatitude());
		intent.putExtra("lon", location.getLongitude());
		intent.putExtra("alt", location.getAltitude());
		intent.putExtra("acc", location.getAccuracy());
		intent.putExtra("pres", currentPressure);
		intent.putExtra("time", location.getTime());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
		Log.i("SensorService", "Lat: " + location.getLatitude());
		Log.i("SensorService", "Lon: " + location.getLongitude());
	    Log.i("SensorService", "Alt: " + location.getAltitude());
	    Log.i("SensorService", "Acc: " + location.getAccuracy());
	    Log.i("SensorService", "Pres: " + currentPressure);
	    Log.i("SensorService", "Time: " + location.getTime());
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	public File getStorageDir() {
        // Get the directory for the user's public pictures directory. 
        File file = new File(Environment.getExternalStoragePublicDirectory("ElevationLog"), "measurements");
        if (!file.exists()) {
        	if(!file.mkdirs())
        		System.out.println("Directory not created");
        } else {
        	System.out.println("Directory exists!");
        }
        return file;
    }
}
