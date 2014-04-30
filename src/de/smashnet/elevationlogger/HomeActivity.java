package de.smashnet.elevationlogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jsqlite.Exception;
import jsqlite.Stmt;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Main view of the ElevationLogger Android tool. 
 * 
 * @author Nicolas Inden
 * @contact nicolas.inden@smashnet.de
 * @date 29.12.2013
 */
public class HomeActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	/**
	 * Our handler for received Intents. This will be called whenever an Intent
	 * with an action named "custom-event-name" is broadcasted.
	 */
	private BroadcastReceiver mMessageReceiverComplete = new SensorDataCompleteReceiver();
	private BroadcastReceiver mMessageReceiverPressure = new SensorDataPressureReceiver();

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	/**
	 * SensorService running
	 */
	boolean serviceRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "sensor-data".
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverComplete,
		    new IntentFilter("sensor-data-complete"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverPressure,
			new IntentFilter("sensor-data-pressure"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public void onResume() {
	  super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	/**
	 * If the Toggle Service button is pushed, the SensorService is toggled.
	 * Hell, did you expect that??
	 * 
	 * @param view
	 */
	public void onToggleService(View view) {
		if(!serviceRunning) {
			// Start SensorService
			Intent i = new Intent(this, SensorService.class);
			this.startService(i);
			serviceRunning = true;
			
			// Feedback for user on home-tab
			writeLog("Enabled SensorService! (be patient ;)");
		}else{
			// Stop SensorService
			Intent i = new Intent(this, SensorService.class);
			this.stopService(i);
			serviceRunning = false;
			
			// Feedback for user on home-tab
			writeLog("Disabled SensorService!");
		}
	}
	
	/**
	 * This one is invoked if the "Send Results" button is pushed. It processes so far
	 * unprocessed GPX files written by the SensorService. GPX files are converted into
	 * simple traces (location,pressure) tuples. (Lat,Lon) locations are mapped to
	 * nearest OSM node.
	 * 
	 * @param view
	 */
	public void onSendResults(View view) {
		//So far just testing content
		writeLog("Test");
	}
	
	/**
	 * Little helper to write text to the event log of HomeActivity.
	 * 
	 * @param text the text to be displayed
	 */
	public void writeLog(String text) {
		// Create readable date string
		Date time = new Date();
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMdd-HHmmss", Locale.GERMANY);
		String date = sDateFormat.format(time);
		
		TextView welcome = new TextView(this);
		LinearLayout log = (LinearLayout) findViewById(R.id.linlay_log);
		welcome.setText(date + ": " + text);
		
		log.addView(welcome);
	}
	
	/**
	 * Custom receiver for sensor data from SensorService
	 * @author Nicolas Inden
	 */
	public class SensorDataCompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			double lat, lon, alt, dist;
			float acc, pressure;
			long time, osm_id;
			
			// Get extra data included in the Intent
		    lat = intent.getDoubleExtra("lat", -1.0);
		    if(lat == -1.0)
		    	return;
		    lon = intent.getDoubleExtra("lon", -1.0);
		    if(lon == -1.0)
		    	return;
		    alt = intent.getDoubleExtra("alt", -1.0);
		    if(alt == -1.0)
		    	return;
		    acc = intent.getFloatExtra("acc", -1.0f);
		    if(acc == -1.0f)
		    	return;
		    pressure = intent.getFloatExtra("pres", -1.0f);
		    if(pressure == -1.0f)
		    	return;
		    
		    osm_id = intent.getLongExtra("osm_id", -1);
		    dist = intent.getDoubleExtra("osm_distance", -1.0d);
		    
		    time = intent.getLongExtra("time", 0);
		    if(time == 0)
		    	return;
		    
		    // Output raw values in raw-value-tab
		    TextView latRes = (TextView) findViewById(R.id.tv_lat_res);
		    TextView lonRes = (TextView) findViewById(R.id.tv_long_res);
		    TextView altRes = (TextView) findViewById(R.id.tv_alt_res);
		    TextView accRes = (TextView) findViewById(R.id.tv_acc_res);
		    TextView preRes = (TextView) findViewById(R.id.tv_air_pressure_res);
		    TextView osmRes = (TextView) findViewById(R.id.tv_osm_id_res);
		    TextView distRes = (TextView) findViewById(R.id.tv_osm_dist_res);
		    TextView timRes = (TextView) findViewById(R.id.tv_time_res);
		    
		    SimpleDateFormat sDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.GERMANY);
			String date = sDateFormat.format(time);
		    
		    latRes.setText(String.valueOf(lat));
		    lonRes.setText(String.valueOf(lon));
		    altRes.setText(String.valueOf(alt) + " m");
		    accRes.setText(String.valueOf(acc) + " m");
		    preRes.setText(String.valueOf(pressure) + " mBar");
		    if(osm_id > 0) {
		    	osmRes.setText(String.valueOf(osm_id));
		    } else {
		    	osmRes.setText("-");
		    }
		    if(dist > 0.0) {
		    	distRes.setText(String.valueOf(dist) + " m");
		    } else {
		    	distRes.setText("-");
		    }
		    timRes.setText(date);
		}
		
	}
	
	/**
	 * Custom receiver for sensor data from SensorService
	 * @author Nicolas Inden
	 */
	public class SensorDataPressureReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			float pressure;
			
			pressure = intent.getFloatExtra("pres", -1.0f);
		    if(pressure == -1.0f)
		    	return;
		    
		    TextView preRes = (TextView) findViewById(R.id.tv_air_pressure_res);
		    TextView preAlt = (TextView) findViewById(R.id.tv_air_altitude_res);
		    preRes.setText(String.valueOf(pressure) + " mBar");
		    double res = -((Math.pow(pressure/1013.25,1/5.255) - 1) * 288.15)/0.0065;
		    preAlt.setText(String.format("%.2f", res) + " m");
		}
		
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new HomeActivityFragment();
			Bundle args = new Bundle();
			args.putInt(HomeActivityFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class HomeActivityFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public HomeActivityFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			int section = getArguments().getInt(ARG_SECTION_NUMBER);
			View rootView;
			switch(section){
			case 1:
				rootView = inflater.inflate(R.layout.fragment_home_home, container, false);
				onCreateHome(rootView);
				break;
			case 2:
				rootView = inflater.inflate(R.layout.fragment_home_raw, container, false);
				onCreateRawView(rootView);
				break;
			case 3:
				rootView = inflater.inflate(R.layout.fragment_home_dummy, container, false);
				onCreateDiagram(rootView);
				break;
			default:
				rootView = inflater.inflate(R.layout.fragment_home_dummy, container, false);
				TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
				dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));	
			}
			
			return rootView;
		}

		private void onCreateDiagram(View rootView) {
			
		}

		private void onCreateRawView(View rootView) {
			
		}

		private void onCreateHome(View rootView) {

		}
	}
}
