Elevation Logger
================

This Android application is part of my master thesis about gathering air pressure data to create elevation profiles while maintaining user location anonymity.

Functions
=========

1. Display GPS information:
  * Latitude
  * Longitude
  * Elevation (from GPS)
  * Accuracy
2. Display air pressure in _mbar_
3. Background service to record these values into a GPX file

GPX files are written in the directory

_ExternalPublicStorageDirectory/ElevationLog/measurements/yyMMdd-HHmm\_record.gpx_

The track in the GPX file looks like this

	...
	<trk>
    <name>ElevationLogger recording</name>
    <desc></desc>
    <trkseg>
      <trkpt lat="47.12345" lon="12.12345">
      	<ele>723.0</ele>
      	<time>2014-01-02T12:27:27Z</time>
      	<extensions>
        	<accuracy>14.0</accuracy>
        	<airpressure>933.90674</airpressure>
      	</extensions>
    	</trkpt>
		</trkseg>
	</trk>
	...

Contact
=======

* Mail: nicolas.inden@rwth-aachen.de
* Jabber: nicolas.inden@jabber.rwth-aachen.de
* GPG:
	* Key: 0xB2F8AA17
	* Fingerprint: A757 5741 FD1E 63E8 357D  48E2 3C68 AE70 B2F8 AA17
