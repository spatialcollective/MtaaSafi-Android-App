package com.sc.mtaa_safi;

import android.location.Location;

public class Landmark {
    String name, village;
    Location loc;

    public Landmark(String landmarkName, double lon, double lat, String villageName) {
        name = landmarkName;
        village = villageName;
        loc = new Location(name);
        loc.setLatitude(lat);
        loc.setLongitude(lon);
    }
}
