package de.smashnet.elevationlogger;

import java.io.File;
import java.io.Serializable;

import jsqlite.Exception;
import jsqlite.Stmt;
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
import android.os.AsyncTask;
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
			 implements LocationListener, SensorEventListener, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5514644069034973495L;

	/**
	 * Handles sensor service
	 */
	transient SensorManager mSensorManager;
	
	/**
	 * Handles location service
	 */
	transient LocationManager mLocationManager;
	
	/**
	 * We use the air pressure sensor
	 */
	transient Sensor mPressure;
	
	/**
	 * Source for location information
	 */
	String mProvider;
	
	/**
	 * Last measured air pressure
	 */
	float mCurrentPressure = 0.0f;
	
	/**
	 * Used to Schmitt-triggering data recording
	 */
	boolean mRecording;
	
	/**
	 * The trace DB
	 */
	TraceDB mTraceDB;
	
	/**
	 * Spatialite Database
	 */
	transient jsqlite.Database mDatabase;

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
		
		// Unregister sensors and finish GPX file
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		try {
			mDatabase.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("SensorService", "Error closing database!");
		}
		//Serialize trace DB to storage
		mTraceDB.writeTraceDBFile();
		
		Log.i("SensorService", "Service stopped!");
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
		mRecording = false;
		
		mTraceDB = new TraceDB(this, "traces.db");
        
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
		mCurrentPressure = event.values[0];
		Intent intent = new Intent("sensor-data-pressure");
		intent.putExtra("pres", mCurrentPressure);
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
		boolean emulator = true;
		
		//Check if required information is available
		if(!location.hasAccuracy() || !location.hasAltitude() || !location.hasSpeed()) {
			if(emulator) {
				location.setAccuracy(7.0f);
				location.setAltitude(200.0d);
				location.setSpeed(0.0f);
				mCurrentPressure = 980.0f;
			} else {
				return;
			}
		}
		
		// Schmitt-trigger data recording depending on GPS accuracy
		if(location.getAccuracy() <= 14.0 && !mRecording){
			mRecording = true;
			mTraceDB.startNewCurrentTrace();
		}else if(location.getAccuracy() > 18.0 && mRecording){
			mRecording = false;
			mTraceDB.closeCurrentTrace();
		}
		
		String res = getNearestOSMNode(location, 0.001, 30, 1);
		long osm_id = -1;
		double dist = -1.0d;
		if(res != null){
			osm_id = Long.valueOf(res.split(",")[0]);
			dist = Double.valueOf(res.split(",")[1]);
		}
		
		// Broadcast sensor-data to HomeActivity
		Intent intent = new Intent("sensor-data-complete");
		intent.putExtra("lat", location.getLatitude());
		intent.putExtra("lon", location.getLongitude());
		intent.putExtra("alt", location.getAltitude());
		intent.putExtra("acc", location.getAccuracy());
		intent.putExtra("pres", mCurrentPressure);
		intent.putExtra("osm_id", osm_id);
		intent.putExtra("osm_distance", dist);
		intent.putExtra("time", location.getTime());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
		// Sensor data log output
		Log.i("SensorService", "Lat: " + location.getLatitude());
		Log.i("SensorService", "Lon: " + location.getLongitude());
	    Log.i("SensorService", "Alt: " + location.getAltitude());
	    Log.i("SensorService", "Acc: " + location.getAccuracy());
	    Log.i("SensorService", "Pres: " + mCurrentPressure);
	    Log.i("SensorService", "Osm_id: " + osm_id);
	    Log.i("SensorService", "Dist: " + dist);
	    Log.i("SensorService", "Time: " + location.getTime());
	    
	    // Save sensor-data if GPS accuracy is <= 15 meters
	    if(!mRecording)
			return;
	    
	 	mTraceDB.addNodeToCurrentTrace(new LocationNode(location, osm_id, dist, mCurrentPressure));
	}

	private String getNearestOSMNode(Location location, double radius, double distance, int limit) {
		try {
			//jsqlite.Database mDatabase = new jsqlite.Database();
			//mDatabase.open(SensorService.getStorageDir("ElevationLog","map") + "/" + getApplicationContext().getString(R.string.osm_db), jsqlite.Constants.SQLITE_OPEN_READONLY);
			
			String slGetNearestNode = "SELECT osm_id, ST_Distance(geometry, MakePoint(" + location.getLongitude() + ", " + location.getLatitude() + "), 0) AS distance "
					+ "FROM 'regbez-koeln_nodes' "
					+ "WHERE ROWID IN (SELECT ROWID FROM SpatialIndex WHERE f_table_name='regbez-koeln_nodes' "
					+ "AND search_frame=BuildCircleMbr(" + location.getLongitude() + ", " + location.getLatitude() +", " + radius + ")) "
					+ "AND distance < " + distance + " ORDER BY distance LIMIT " + limit + ";";
			
			//mDatabase.exec(slGetNearestNode, new SpatialiteCallback());
			//mDatabase.close();
			Stmt stmt = mDatabase.prepare(slGetNearestNode);
			
			if(stmt.step()){
				String res = stmt.column_int(0) + "," + stmt.column_double(1);
				stmt.close();
				return res;
			}
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("DBquery", "DB error");
		}
		return null;
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
	public static File getStorageDir(String progname, String dirname) {
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
	
	public class QueryNearestPoints extends AsyncTask<String, Integer, String>{
		
		public QueryNearestPoints(Context con, Location loc) {
		}

		/**
		 * Does a background query on the spatialite database for the nearest point to the given coordinates and properties.
		 * <ul>
		 * 	<li>params[0] -> longitude</li>
		 * 	<li>params[1] -> latitude</li>
		 * 	<li>params[2] -> search radius in CSR (usually 0.001)</li>
		 * 	<li>params[3] -> max distance for resulting nodes</li>
		 * 	<li>params[4] -> max amount of results</li>
		 * </ul>
		 */
		@Override
		protected String doInBackground(String... params) {
			if(params.length < 5) {
				Log.e("AsyncQuery", "Too few arguments");
				return null;
			}
			try {
				jsqlite.Database mDatabase = new jsqlite.Database();
				mDatabase.open(SensorService.getStorageDir("ElevationLog","map") + "/" + getApplicationContext().getString(R.string.osm_db), jsqlite.Constants.SQLITE_OPEN_READONLY);
				
				String slGetNearestNode = "SELECT osm_id, ST_Distance(geometry, MakePoint(" + params[0] + ", " + params[1] + "), 0) AS distance "
						+ "FROM 'regbez-koeln_nodes' "
						+ "WHERE ROWID IN (SELECT ROWID FROM SpatialIndex WHERE f_table_name='regbez-koeln_nodes' "
						+ "AND search_frame=BuildCircleMbr(" + params[0] + ", " + params[1] +", " + params[2] + ")) "
						+ "AND distance < " + params[3] + " ORDER BY distance LIMIT " + params[4] + ";";
				
				//mDatabase.exec(slGetNearestNode, new SpatialiteCallback());
				//mDatabase.close();
				Stmt stmt = mDatabase.prepare(slGetNearestNode);
				
				if(stmt.step()){
					String res = stmt.column_int(0) + "," + stmt.column_double(1);
					stmt.close();
					return res;
				}
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("DBquery", "DB error");
			}
			
			return null;
		}
		
		@Override
		public void onPostExecute(String res) {
			if(res == null) {
				Log.i("QueryResult", "null");
				return;
			}
			
			
		}
		
		/*public class SpatialiteCallback implements jsqlite.Callback {

			@Override
			public void columns(String[] cols) {
				for(int i = 0; i < cols.length; i++) {
					System.out.print(cols[i] + " ");
				}
				System.out.println();
			}

			@Override
			public boolean newrow(String[] row) {
				for(int i = 0; i < row.length; i++) {
					System.out.print(row[i] + " ");
				}
				System.out.println();
				return false;
			}

			@Override
			public void types(String[] type) {
				for(int i = 0; i < type.length; i++) {
					System.out.print(type[i] + " ");
				}
				System.out.println();
			}
			
		}*/
	}
}
