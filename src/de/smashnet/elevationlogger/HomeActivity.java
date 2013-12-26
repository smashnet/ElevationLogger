package de.smashnet.elevationlogger;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements
		ActionBar.TabListener, LocationListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	LocationManager locMan;
	String provider;
	boolean gpsIsActive;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

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
		
		locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gpsIsActive = false;
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
	  if(provider != null){
		  locMan.requestLocationUpdates(provider, 400, 0, this);
		  gpsIsActive = true;
	  }
	}
	
	@Override
	public void onLocationChanged(Location location) {
		TextView latRes = (TextView) findViewById(R.id.tv_lat_res);
		TextView longRes = (TextView) findViewById(R.id.tv_long_res);
		TextView altRes = (TextView) findViewById(R.id.tv_alt_res);
		TextView accRes = (TextView) findViewById(R.id.tv_acc_res);
		
		latRes.setText(String.valueOf(location.getLatitude()));
		longRes.setText(String.valueOf(location.getLongitude()));
		altRes.setText(String.valueOf(location.getAltitude()) + "m");
		accRes.setText(String.valueOf(location.getAccuracy()) + "m");
		
		System.out.println("Lat: " + location.getLatitude());
		System.out.println("Long: " + location.getLongitude());
	    System.out.println("Alt: " + location.getAltitude());
	    System.out.println("Acc: " + location.getAccuracy());
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

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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
	
	public void onStartGps(View view) {
		LinearLayout log = (LinearLayout) findViewById(R.id.linlay_log);
		TextView ack = new TextView(this);
		
		if(gpsIsActive){
			ack.setText("GPS already active!");
			log.addView(ack);
			return;
		}else{
			ack.setText("I will! -- Starting GPS");
			log.addView(ack);
		}
		
	    provider = LocationManager.GPS_PROVIDER;
	    Location location = locMan.getLastKnownLocation(provider);
	    
	    if(provider != null){
			  locMan.requestLocationUpdates(provider, 400, 0, this);
			  gpsIsActive = true;
	    }

	    // Initialize the location fields
	    if (location != null) {
	      System.out.println("Provider " + provider + " has been selected.");
	      onLocationChanged(location);
	    } else {
	      System.out.println("No location available");
	    }
	}
	
	public void onStartAir(View view) {
		
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
			// TODO Auto-generated method stub
			
		}

		private void onCreateRawView(View rootView) {
			// TODO Auto-generated method stub
			
		}

		private void onCreateHome(View rootView) {
			// TODO Auto-generated method stub
			TextView welcome = new TextView(rootView.getContext());
			LinearLayout log = (LinearLayout) rootView.findViewById(R.id.linlay_log);
			welcome.setText("Get some nuts!");
			
			log.addView(welcome);
		}
	}
}
