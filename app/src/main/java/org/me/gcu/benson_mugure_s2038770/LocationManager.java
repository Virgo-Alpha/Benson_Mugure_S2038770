package org.me.gcu.benson_mugure_s2038770;

import java.util.HashMap;

public class LocationManager {
    private static final HashMap<String, String> locationMap = new HashMap<>();

    static {
        // Initialize location map
        locationMap.put("Glasgow", "2648579");
        locationMap.put("London", "2643743");
        locationMap.put("NewYork", "5128581");
        locationMap.put("Oman", "287286");
        locationMap.put("Mauritius", "934154");
        locationMap.put("Bangladesh", "1185241");
        // Add other locations similarly
    }

    public static String getLocationId(String locationName) {
        return locationMap.get(locationName);
    }
}
