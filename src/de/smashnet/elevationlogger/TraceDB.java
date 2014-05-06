package de.smashnet.elevationlogger;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Simple facility to manage traces. Keeps track of what traces have already
 * been sent, and which are still to be sent.
 * 
 * @author Nicolas Inden
 *
 */
public class TraceDB implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4494993505645535954L;
	/**
	 * The file where the DB is stored
	 */
	private File mTraceDBFile;
	/**
	 * The list of known traces
	 */
	private LinkedList<LocationTrace> mTraces;
	/**
	 * The trace that is currently used.
	 */
	private LocationTrace mCurrentTrace;
	/**
	 * The context this class was instantiated in
	 */
	private Context mContext;
	
	/**
	 * Open database from given filename. If file does not exist a
	 * new DB is created.
	 * 
	 * @param dbFilename
	 */
	public TraceDB(Context con, String dbFilename) {
		mContext = con;
		mTraceDBFile = new File(getStorageDir("ElevationLog","db"), dbFilename);
		if(!mTraceDBFile.exists()) {
			//File does not exist, so create a new empty DB
			try {
				mTraceDBFile.createNewFile();
				mTraces = new LinkedList<LocationTrace>();
				Log.i("DBStorage", "Created new trace DB");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("DBStorage", "Error creating db file: " + e);
			}
		} else {
			//If file exists, read db
			Log.i("DBStorage", "Loading existing trace DB");
			mTraces = loadTraceDBFile();
		}
	}
	
	/**
	 * Starts a new trace for the following locations. This trace is also
	 * added to mTraces.
	 */
	public void startNewCurrentTrace() {
		mCurrentTrace = new LocationTrace(mContext, getStorageDir("ElevationLog","traces"));
		mTraces.add(mCurrentTrace);
	}
	
	/**
	 * Add a LocationNode to the current trace.
	 * 
	 * @param node the node to be added
	 */
	public void addNodeToCurrentTrace(LocationNode node) {
		if(mCurrentTrace == null)
			mCurrentTrace = new LocationTrace(mContext, getStorageDir("ElevationLog","traces"));
		mCurrentTrace.addNode(node);
	}
	
	/**
	 * Closes the current trace.
	 */
	public void closeCurrentTrace() {
		mCurrentTrace = null;
	}

	/**
	 * Deserializes the db file from storage.
	 * 
	 * @return res the LinkedList of LocationTraces
	 */
	@SuppressWarnings("unchecked")
	private LinkedList<LocationTrace> loadTraceDBFile(){
		try {
			LinkedList<LocationTrace> res = null;
			
			FileInputStream fileIn = new FileInputStream(mTraceDBFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			res = (LinkedList<LocationTrace>) in.readObject();
			
			in.close();
			fileIn.close();
			Log.i("DBStorage", "Successfully loaded trace DB");
			return res;
		} catch (EOFException ef) {
			Log.w("DBStorage", "Existing db corrupted, creating new one.");
			try {
				mTraceDBFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new LinkedList<LocationTrace>();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.e("DBStorage", "Error reading DB: " + ioe);
		} catch (ClassNotFoundException nfe) {
			nfe.printStackTrace();
			Log.e("DBStorage", "No such class: " + nfe);
		}
		return null;
	}
	
	/**
	 * Serialize list of traces and save to file.
	 * 
	 * @throws IOException
	 */
	public void writeTraceDBFile(){
		try {
			FileOutputStream fileOut = new FileOutputStream(mTraceDBFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			out.writeObject(mTraces);
			
			out.close();
			fileOut.close();
			
			mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mTraceDBFile)));
			Log.i("DBStorage", "Serialized data is saved in " + mTraceDBFile.getAbsolutePath());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Log.e("DBStorage", "Error writing trace DB file: " + ioe);
		}
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
        		Log.w("DBStorage", "Directory not created");
        } else {
        	Log.i("DBStorage", "Directory exists!");
        }
        return file;
    }
}
