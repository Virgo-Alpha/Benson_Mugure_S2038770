//
// Name                 Benson Mugure
// Student ID           S2038770
// Programme of Study   Computing
//

// // UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.benson_mugure_s2038770;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnMapReadyCallback {
    private TextView forecastDisplay;
    // private TextView observationDisplay; // Add TextView for observation display
    private TextView observationDay;
    private TextView observationTime;
    private TextView observationWeather;
    private TextView observationTitleTemperature;
    private TextView observationTemperature;
    private TextView observationWindDirection;
    private TextView observationWindSpeed;
    private TextView observationHumidity;
    private TextView observationPressure;
    private TextView observationVisibility;
    private TextView observationDate;

    private TextView currentWeatherTextView;
    private Button prevButton;
    private Button nextButton;
    private Button navigateButton;
    private Button seeFullForecastButton;
    private TextView locationDisplay;

    private HashMap<String, String> locationCodes;
    private String currentLocation;
    private Map<String, Object> forecastData;
    private Map<String, String> observationData;

    private int currentLocationIndex;
    private List<String> locations;

    private GoogleMap mMap;
    private String georssPoint; // Georss point from your data

    private NetworkChangeReceiver networkChangeReceiver;

    private HashMap<String, Integer> weatherIcons = new HashMap<>();

    private static final long DEFAULT_MORNING_TIME = 8 * 60 * 60 * 1000; // 08:00 in milliseconds
    private static final long DEFAULT_EVENING_TIME = 20 * 60 * 60 * 1000; // 20:00 in milliseconds

    private long morningUpdateTime;
    private long eveningUpdateTime;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable dataRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndUpdateData();
            handler.postDelayed(this, 60000); // Check every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default update times
        setDefaultUpdateTimes();

        // Start checking and updating data
        startDataRefreshScheduler();

        // Register the network change receiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, filter);

        // Check if there is internet and if there isn't, display a bagel and a message
        if (networkChangeReceiver.isInternetConnected(this)) {
            setContentView(R.layout.activity_no_internet);
        } else {
            // setContentView(R.layout.activity_main);
            Log.d("Internet Connection", "INTERNET!");
        

            // Check the current device orientation
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_main_landscape);
            } else {
                // Log.d("Landscape saved data: ", savedInstanceState.toString());
                setContentView(R.layout.activity_main);
            }

            // Apply dark mode if enabled
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean darkModeEnabled = sharedPreferences.getBoolean("darkMode", false);
            if (darkModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Initialize icons
            weatherIcons.put("Sunny", R.drawable.day_clear);
            weatherIcons.put("Clear Sky", R.drawable.night_clear);
            weatherIcons.put("Sunny Intervals", R.drawable.day_clear);
            weatherIcons.put("Light Cloud", R.drawable.day_partial_cloud);
            weatherIcons.put("Thick Cloud", R.drawable.cloudy);
            weatherIcons.put("Overcast", R.drawable.overcast);
            weatherIcons.put("Rain", R.drawable.day_rain);
            weatherIcons.put("Thundery Showers", R.drawable.day_rain_thunder);
            weatherIcons.put("Light Rain", R.drawable.sleet);
            weatherIcons.put("Light Rain Showers", R.drawable.sleet);
            weatherIcons.put("Drizzle", R.drawable.sleet);
            weatherIcons.put("Snow", R.drawable.day_snow);
            weatherIcons.put("Snow and Thunderstorms", R.drawable.day_snow_thunder);
            weatherIcons.put("Fog", R.drawable.fog);
            weatherIcons.put("Thunder", R.drawable.thunder);
            weatherIcons.put("Mist", R.drawable.mist);
            weatherIcons.put("Tornado", R.drawable.tornado);
            weatherIcons.put("Wind", R.drawable.wind);

            // Initialize location codes
            locationCodes = new HashMap<>();
            locationCodes.put("Glasgow", "2648579");
            locationCodes.put("London", "2643743");
            locationCodes.put("NewYork", "5128581");
            locationCodes.put("Oman", "287286");
            locationCodes.put("Mauritius", "934154");
            locationCodes.put("Bangladesh", "1185241");

            // Initialize locations
            locations = new ArrayList<>(locationCodes.keySet());

            // Set default location and index
            currentLocation = locations.get(0);
            currentLocationIndex = 0;

            // Initialize UI components

            // Find the ImageButton for the settings icon
            ImageButton settingsButton = findViewById(R.id.settingsButton);

            // Set click listener for the settings button
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open SettingsActivity when settings button is clicked
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });
            
            observationTitleTemperature = findViewById(R.id.observationTitleTemperature); // Initialize observation display TextView
            currentWeatherTextView = findViewById(R.id.currentWeatherTextView);
            prevButton = findViewById(R.id.prevButton);
            nextButton = findViewById(R.id.nextButton);
            navigateButton = findViewById(R.id.navigateButton);
            seeFullForecastButton = findViewById(R.id.seeFullForecastButton);
            locationDisplay = findViewById(R.id.locationDisplay);

            prevButton.setOnClickListener(this);
            nextButton.setOnClickListener(this);
            navigateButton.setOnClickListener(this);
            seeFullForecastButton.setOnClickListener(this);

            // Fetch and display forecast data for the default location
            fetchAndDisplayForecastData(currentLocation);

            // Fetch and display observation data
            fetchAndDisplayObservationData(currentLocation);

            // Initialize georssPoint (this should be retrieved from your forecastData)
            georssPoint = "23.7104 90.4074"; // Example georssPoint

            // Call fetchForecastData with a callback
            fetchForecastData(currentLocation, new ForecastDataCallback() {
                public void onForecastDataReceived(Map<String, Object> forecast) {
                    setgeoRssPoint(forecast);
                }
            });

            // Find the map fragment or create a new one if it doesn't exist
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commit();
            }

            // Initialize the map when it's ready
            mapFragment.getMapAsync(this);
        }

        startInternetCheck();

    }

    private void setDefaultUpdateTimes() {
        // Get the SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if default times are already set
        if (!preferences.contains("morning_time") || !preferences.contains("evening_time")) {
            // Set default morning and evening update times
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("morning_time", DEFAULT_MORNING_TIME);
            editor.putLong("evening_time", DEFAULT_EVENING_TIME);
            editor.apply();
        }

        // Retrieve the update times
        morningUpdateTime = preferences.getLong("morning_time", DEFAULT_MORNING_TIME);
        eveningUpdateTime = preferences.getLong("evening_time", DEFAULT_EVENING_TIME);
    }

    // ! Method to refresh data based on selected time
    private void refreshDataAtSelectedTime(long updateTimeMillis) {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = calendar.getTimeInMillis();

        // Calculate the time difference
        long timeDifference = updateTimeMillis - currentTimeMillis;

        // Check if the time difference is negative
        if (timeDifference < 0) {
            // If the time difference is negative, add 24 hours to the time difference
            timeDifference += 24 * 60 * 60 * 1000;
        }

        // Schedule the refresh task
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Refresh the data
                fetchAndDisplayForecastData(currentLocation);
                fetchAndDisplayObservationData(currentLocation);
                fetchForecastData(currentLocation, new ForecastDataCallback() {
                    @Override
                    public void onForecastDataReceived(Map<String, Object> forecast) {
                        // Update georssPoint and map when new forecast data is received
                        setGeoRssPointAndMap(forecast);
                    }
                });

                // Schedule the next refresh
                refreshDataAtSelectedTime(updateTimeMillis);
            }
        }, timeDifference);
    }

    // Method to check if it's time to refresh data
    private void checkAndUpdateData() {
        Calendar now = Calendar.getInstance();
        long currentTimeMillis = now.getTimeInMillis();

        if (currentTimeMillis >= morningUpdateTime && currentTimeMillis < morningUpdateTime + 60000) {
            // It's morning update time (+/- 1 minute for accuracy)
            refreshDataAtSelectedTime(morningUpdateTime);
        } else if (currentTimeMillis >= eveningUpdateTime && currentTimeMillis < eveningUpdateTime + 60000) {
            // It's evening update time (+/- 1 minute for accuracy)
            refreshDataAtSelectedTime(eveningUpdateTime);
        }
    }

    // Method to start data refresh scheduler
    private void startDataRefreshScheduler() {
        handler.post(dataRefreshRunnable); // Start the scheduler
    }

    // Method to stop data refresh scheduler
    private void stopDataRefreshScheduler() {
        handler.removeCallbacks(dataRefreshRunnable); // Stop the scheduler
    }

    private class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternetConnected(context)) {
                Log.d("Internet Connection", "No INTERNET!");
                // Internet connection is lost, handle accordingly
                setContentView(R.layout.activity_no_internet);
            }
        }

        private boolean isInternetConnected(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
        stopInternetCheck();
        stopDataRefreshScheduler();
    }

    private void startInternetCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkInternetAndDisplayLayout();
                handler.postDelayed(this, 5000); // Check every 5 seconds
            }
        }, 5000); // Start after 5 seconds
    }

    private void stopInternetCheck() {
        handler.removeCallbacksAndMessages(null);
    }

    private void checkInternetAndDisplayLayout() {
        if (networkChangeReceiver.isInternetConnected(this)) {
            setContentView(R.layout.activity_no_internet);
        }

        // Implement method to reload automatically if internet has returned
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (georssPoint != null) {
            // Split the georssPoint into latitude and longitude
            String[] latLng = georssPoint.split(" ");
            if (latLng.length == 2) {
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);

                // Add a marker at the specified coordinates
                LatLng location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(location).title("Marker in Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12)); // Zoom level 12
            }
        }
    }

    private String setgeoRssPoint(Map<String, Object> forecast) {
        georssPoint = forecast.get("georssPoint").toString();
        return forecast.get("georssPoint").toString();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_landscape);
        } else {
            setContentView(R.layout.activity_main);
        }

        // Initialize UI components
        observationTitleTemperature = findViewById(R.id.observationTitleTemperature); // Initialize observation display TextView
        currentWeatherTextView = findViewById(R.id.currentWeatherTextView);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        navigateButton = findViewById(R.id.navigateButton);
        seeFullForecastButton = findViewById(R.id.seeFullForecastButton);
        locationDisplay = findViewById(R.id.locationDisplay);

        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        navigateButton.setOnClickListener(this);
        seeFullForecastButton.setOnClickListener(this);

        // Fetch and display forecast data for the default location
        fetchAndDisplayForecastData(currentLocation);

        // Fetch and display observation data
        fetchAndDisplayObservationData(currentLocation);

        // Initialize georssPoint (this should be retrieved from your forecastData)
        georssPoint = "23.7104 90.4074"; // Example georssPoint

        // Call fetchForecastData with a callback
        fetchForecastData(currentLocation, new ForecastDataCallback() {
            public void onForecastDataReceived(Map<String, Object> forecast) {
                setgeoRssPoint(forecast);
                Log.d("forecast for GeoRSS Point", georssPoint);
            }
        });

        // The map fragment already exists, no need to replace it
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        // Set the OnMapReadyCallback before committing the fragment transaction
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Here you can customize the map if needed
                mMap = googleMap;
                Log.d("Map Fragment: ", "The map is ready!");
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();

        // Initialize the map when it's ready
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prevButton) {
            currentLocationIndex = (currentLocationIndex - 1 + locations.size()) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
            fetchAndDisplayObservationData(currentLocation);
            fetchForecastData(currentLocation, new ForecastDataCallback() {
                @Override
                public void onForecastDataReceived(Map<String, Object> forecast) {
                    // Update georssPoint and map when new forecast data is received
                    setGeoRssPointAndMap(forecast);
                }
            });
        } else if (v.getId() == R.id.nextButton) {
            currentLocationIndex = (currentLocationIndex + 1) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
            fetchAndDisplayObservationData(currentLocation);
            fetchForecastData(currentLocation, new ForecastDataCallback() {
                @Override
                public void onForecastDataReceived(Map<String, Object> forecast) {
                    // Update georssPoint and map when new forecast data is received
                    setGeoRssPointAndMap(forecast);
                }
            });
        } else if (v.getId() == R.id.navigateButton) {
            currentLocation = locations.get(currentLocationIndex);
            // Call fetchObservationData with a callback
            fetchObservationData(currentLocation, new ObservationDataCallback() {
                @Override
                public void onObservationDataReceived(Map<String, String> observation) {
                    navigateWithObservation(observation, currentLocation);
                }
            });
        } else if (v.getId() == R.id.seeFullForecastButton) {
            currentLocation = locations.get(currentLocationIndex);
            // Call fetchForecastData with a callback
            fetchForecastData(currentLocation, new ForecastDataCallback() {
                @Override
                public void onForecastDataReceived(Map<String, Object> forecast) {
                    navigateWithForecast(forecast, currentLocation);
                }
            });
        }
    }

    // navigate to a new activity when the navigate button is clicked
    public void navigateWithObservation(Map<String, String> observation, String location) {
        Intent intent = new Intent(this, current_weather.class);
        // Pass the observation data to the new activity
        intent.putExtra("observation", (Serializable) observation);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    // navigate to a new activity when the see full forecast button is clicked
    public void navigateWithForecast(Map<String, Object> forecast, String location) {
        Intent intent = new Intent(this, full_forecast.class);
        // Pass the forecast data to the new activity
        intent.putExtra("forecast", (Serializable) forecast);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    private void setGeoRssPointAndMap(Map<String, Object> forecast) {
        // Set the new georssPoint
        if (forecast.containsKey("georssPoint")) {
            Object georssObj = forecast.get("georssPoint");
            if (georssObj instanceof String) {
                georssPoint = (String) georssObj;
                
                // Update the map with the new georssPoint on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMapWithNewPoint(georssPoint);
                    }
                });
            } else {
                Log.e("TAG", "georssPoint is not a String");
            }
        } else {
            Log.e("TAG", "forecast does not contain georssPoint key");
        }
    }    
    
    private void updateMapWithNewPoint(String newGeorssPoint) {
        // Check if map is initialized
        if (mMap != null) {
            // Split the georssPoint into latitude and longitude
            String[] latLng = newGeorssPoint.split(" ");
            if (latLng.length == 2) {
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);
    
                // Clear existing markers
                mMap.clear();
    
                // Add a new marker at the specified coordinates
                LatLng location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(location).title("Marker in Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12)); // Zoom level 12
            }
        }
    }        

    private void fetchAndDisplayForecastData(String location) {
        String locationCode = locationCodes.get(location);
        String url = constructForecastUrl(locationCode);
        new Thread(() -> {
            String data = fetchWeatherData(url);
            Map<String, Object> forecastData = parseForecastData(data);
            runOnUiThread(() -> displayForecastData(forecastData));
        }).start();
    }

    private void fetchObservationData(String location, ObservationDataCallback callback) {
        String locationCode = locationCodes.get(location);
        String url = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + locationCode;

        new Thread(() -> {
            String data = fetchWeatherData(url);
            Map<String, String> observation = parseObservationData(data);
            // Pass the observation data to the callback
            callback.onObservationDataReceived(observation);
        }).start();
    }

    // Define a callback interface
    interface ObservationDataCallback {
        void onObservationDataReceived(Map<String, String> observation);
    }

    private void fetchForecastData(String location, ForecastDataCallback callback) {
        String locationCode = locationCodes.get(location);
        String url = constructForecastUrl(locationCode);

        new Thread(() -> {
            String data = fetchWeatherData(url);
            Map<String, Object> forecast = parseForecastData(data);
            // Pass the forecast data to the callback
            callback.onForecastDataReceived(forecast);
        }).start();
    }

    // Define a callback interface
    interface ForecastDataCallback {
        void onForecastDataReceived(Map<String, Object> forecast);
    }

    private void fetchAndDisplayObservationData(String location) {
        String locationCode = locationCodes.get(location);
        String url = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + locationCode;
        new Thread(() -> {
            String data = fetchWeatherData(url);
            Map<String, String> observation = parseObservationData(data);
            runOnUiThread(() -> displayObservationData(observation));
        }).start();
    }

    private String constructForecastUrl(String locationCode) {
        return "https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + locationCode;
    }

    private String fetchWeatherData(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        } catch (IOException e) {
            Log.e("MainActivity", "Error fetching Weather data", e); // Log the error
            return "";
        }
    }

    private Map<String, Object> parseForecastData(String data) {
        Map<String, Object> forecastData = new HashMap<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(data));

            int eventType = parser.getEventType();
            String location = null;
            List<Map<String, String>> forecasts = new ArrayList<>();
            String georssPoint = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("title")) {
                        if (location == null) {
                            location = parser.nextText().trim();
                        } else {
                            String title = parser.nextText().trim();
                            // Assuming title and description are linked together for each item
                            Map<String, String> forecast = new HashMap<>();
                            forecast.put("title", title);
                            forecasts.add(forecast);
                        }
                    } else if (tagName.equals("description") && location != null) {
                        String description = parser.nextText().trim();
                        // Add description to the last forecast in the list
                        if (!forecasts.isEmpty()) {
                            forecasts.get(forecasts.size() - 1).put("description", description);
                        }
                    } else if (tagName.equals("georss:point") && georssPoint == null) {
                        georssPoint = parser.nextText().trim();
                    }
                }

                eventType = parser.next();
            }

            // Store the extracted information
            forecastData.put("location", location);
            forecastData.put("forecasts", forecasts);
            forecastData.put("georssPoint", georssPoint);

        } catch (XmlPullParserException | IOException e) {
            Log.e("MainActivity", "Error parsing forecast data", e); // Log the error
        }

        Log.d("Forecast: ", forecastData.toString());
        // The above logs: {location=BBC Weather - Forecast for  Dhaka, BD, georssPoint=23.7104 90.4074, forecasts=[{title=BBC Weather - Forecast for  Dhaka, BD}, {description=Maximum Temperature: 36°C (97°F), Minimum Temperature: 25°C (78°F), Wind Direction: South Westerly, Wind Speed: 11mph, Visibility: Good, Pressure: 1003mb, Humidity: 49%, UV Risk: 8, Pollution: -- , Sunrise: 05:35 BDT, Sunset: 18:21 BDT, title=Today: Drizzle, Minimum Temperature: 25°C (78°F) Maximum Temperature: 36°C (97°F)}, {description=Maximum Temperature: 36°C (97°F), Minimum Temperature: 25°C (76°F), Wind Direction: Southerly, Wind Speed: 9mph, Visibility: Good, Pressure: 1003mb, Humidity: 63%, UV Risk: 8, Pollution: -- , Sunrise: 05:34 BDT, Sunset: 18:21 BDT, title=Thursday: Sunny, Minimum Temperature: 25°C (76°F) Maximum Temperature: 36°C (97°F)}, {description=Maximum Temperature: 37°C (99°F), Minimum Temperature: 25°C (77°F), Wind Direction: Southerly, Wind Speed: 9mph, Visibility: Moderate, Pressure: 1000mb, Humidity: 59%, UV Risk: 9, Pollution: -- , Sunrise: 05:34 BDT, Sunset: 18:22 BDT, title=Friday: Sunny, Minimum Temperature: 25°C (77°F) Maximum Temperature: 37°C (99°F)}]}
        return forecastData;
    }

    private Map<String, String> parseObservationData(String data) {
        Map<String, String> observation = new HashMap<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(data));

            int eventType = parser.getEventType();
            String title = null;
            String description = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                    title = parser.nextText().trim();
                } else if (eventType == XmlPullParser.START_TAG && parser.getName().equals("description")) {
                    description = parser.nextText().trim();
                } else if (eventType == XmlPullParser.START_TAG && parser.getName().equals("pubDate")) {
                    String pubDate = parser.nextText().trim();
                    observation.put("Date", pubDate);
                }
                eventType = parser.next();
            }

            if (title != null && description != null) {
                // Extract elements from the title
                String[] titleParts = title.split(" - ");
                if (titleParts.length >= 2) {
                    observation.put("Day", titleParts[0]);
                    observation.put("Time", titleParts[1].substring(0, titleParts[1].indexOf(':') + 7));
                    observation.put("Weather", titleParts[1].substring(titleParts[1].indexOf(':') + 9, titleParts[1].indexOf(',')));

                    // Extract temperature from title
                    String temp = titleParts[1].substring(titleParts[1].indexOf(',') + 1);

                    observation.put("TitleTemperature", temp);
                }

                // Extract elements from the description
                String[] descParts = description.split(",");
                for (String part : descParts) {
                    String[] keyValue = part.trim().split(":");
                    if (keyValue.length == 2) {
                        observation.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }

            Log.d("Observation: ", observation.toString());
            // The above logs: {Weather=Not available, TitleTemperature= 35°C (95°F), Temperature=35°C (95°F), Wind Speed=2mph, Humidity=55%, Time=15:00 BDT, Visibility=Moderate, Day=Wednesday, Pressure=1004mb, Date=Wed, 17 Apr 2024 09:00:00 GMT, Wind Direction=South South Easterly}
            return observation;
        } catch (XmlPullParserException | IOException e) {
            Log.e("MainActivity", "Error parsing latest Observation data", e); // Log the error
            return new HashMap<>();
        }
    }

    private void displayForecastData(Map<String, Object> forecastData) {
        StringBuilder displayText = new StringBuilder();

        // Extracting location
        String location = (String) forecastData.get("location");
        if (location != null) {
            displayText.append("Location: ").append(location).append("\n\n");
        }

        LinearLayout forecastContainer = findViewById(R.id.forecastContainer);

        // Clear existing views from forecastContainer
        forecastContainer.removeAllViews();

        List<Map<String, String>> forecasts = (List<Map<String, String>>) forecastData.get("forecasts");

        // Create a structured data model to hold forecast information
        Map<String, List<String>> forecastInfo = new HashMap<>();
        forecastInfo.put("days", new ArrayList<>());
        forecastInfo.put("weather", new ArrayList<>());
        forecastInfo.put("temperature", new ArrayList<>());

        if (forecasts != null && forecasts.size() >= 4) {
            for (int i = 1; i < 4; i++) { // Start from index 1 to skip the first forecast
                Map<String, String> forecast = forecasts.get(i);
                if (forecast != null) {
                    String title = forecast.get("title");
                    if (title != null) {
                        String[] titleParts = title.split(",");
                        if (titleParts.length >= 2) {
                            // Extract Day and Weather Forecast
                            String[] dayWeatherParts = titleParts[0].split(":");
                            String day = "";
                            String weatherForecast = "";
                            if (dayWeatherParts.length == 2) {
                                day = dayWeatherParts[0].trim(); // Extract Day
                                if (day.length() >= 6 && !day.equals("Tonight")) {
                                    day = day.substring(0, 3);
                                }
                                weatherForecast = dayWeatherParts[1].trim(); // Extract Weather Forecast
                            }

                            // Extract Min and Max Temperature
                            String[] tempParts = titleParts[1].split("F"); // Split at the degree symbol

                            String minTemp = "";
                            String maxTemp = "";

                            // Check if the word "Temperature" is in the parts of tempParts
                            // If it is, then extract the min temperature from the first and max temperature from the second part
                            if (tempParts[0].contains("Temperature")) {
                                minTemp = tempParts[0].substring(tempParts[0].indexOf(":") + 1, tempParts[0].indexOf(":") + 6).trim(); // Extract Min Temperature

                            }
                            if (tempParts[1].contains("Temperature")) {
                                maxTemp = tempParts[1].substring(tempParts[1].indexOf(":") + 1, tempParts[1].indexOf(":") + 6).trim(); // Extract Max Temperature
                            }

                            // Add the extracted data to the forecastInfo map
                            forecastInfo.get("days").add(day);
                            forecastInfo.get("weather").add(weatherForecast);
                            forecastInfo.get("temperature").add(minTemp + "/" + maxTemp);

                        }
                    }
                }
            }
        }
        Log.d("forecastInfo now", forecastInfo.toString());
        // The above logs: {weather=[Clear Sky, Sunny, Sunny], temperature=[28°C/, 26°C/39°C, 26°C/39°C], days=[Tonight, Sunday, Monday]}


        if (forecastInfo.get("days") != null && forecastInfo.get("weather") != null && forecastInfo.get("temperature") != null) {
            int numForecasts = Math.min(forecastInfo.get("days").size(), Math.min(forecastInfo.get("weather").size(), forecastInfo.get("temperature").size()));
    
            for (int i = 0; i < numForecasts; i++) {
                // Create a new LinearLayout for each forecast item
                LinearLayout forecastItemLayout = new LinearLayout(this);
                forecastItemLayout.setOrientation(LinearLayout.VERTICAL);
                forecastItemLayout.setPadding(20, 20, 30, 20);
                forecastItemLayout.setBackground(getDrawable(R.drawable.border_black));

                // Create TextViews for day, weather, and temperature
                TextView dayTextView = new TextView(this);
                dayTextView.setText(forecastInfo.get("days").get(i));
                dayTextView.setTextSize(20);
                dayTextView.setTextColor(Color.BLACK);
                dayTextView.setTypeface(null, Typeface.BOLD);
                forecastItemLayout.addView(dayTextView);

                // ! Make icons instead here
//                TextView weatherTextView = new TextView(this);
//                weatherTextView.setText(forecastInfo.get("weather").get(i));
//                weatherTextView.setTextSize(16);
//                weatherTextView.setTextColor(Color.BLACK);
//                forecastItemLayout.addView(weatherTextView);

                ImageView weatherIconView = new ImageView(this);

                // Use the weatherIcons HashMap to set the icon
                Integer iconRes = weatherIcons.get(forecastInfo.get("weather").get(i));
                if (iconRes != null) {
                    weatherIconView.setImageResource(iconRes);
                }
                weatherIconView.setMaxWidth(10);
                weatherIconView.setMaxHeight(10);
                forecastItemLayout.addView(weatherIconView);

                // Set the max width and max height
                int maxWidthDp = 80;
                int maxHeightDp = 50;
                int maxWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidthDp, getResources().getDisplayMetrics());
                int maxHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeightDp, getResources().getDisplayMetrics());

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(maxWidthPx, maxHeightPx);
                weatherIconView.setLayoutParams(layoutParams);

                TextView tempTextView = new TextView(this);
                tempTextView.setText(forecastInfo.get("temperature").get(i));
                tempTextView.setTextSize(16);
                tempTextView.setTextColor(Color.BLACK);
                forecastItemLayout.addView(tempTextView);

                // Add the forecast item layout to the main forecastContainer
                forecastContainer.addView(forecastItemLayout);

            }
        } else {
            Log.e("MainActivity", "Forecast data is null or empty");
        }
    

        // ! Extracting georssPoint
        String georssPoint = (String) forecastData.get("georssPoint");
        if (georssPoint != null) {
            displayText.append("Georss Point: ").append(georssPoint).append("\n");
        }

        // forecastDisplay.setText(displayText.toString());
        locationDisplay.setText(currentLocation);
        locationDisplay.setVisibility(View.VISIBLE); // Show the location display TextView
    }

        private void displayObservationData (Map < String, String > observation){
            // Set Current Weather with Date
            TextView currentWeatherTextView = findViewById(R.id.currentWeatherTextView);

            // Display Temperature
            String titleTemperature = observation.get("TitleTemperature");
            if (titleTemperature != null && titleTemperature.contains("°C")) {
                // Extract temperature in Celsius
                String temperatureCelsius = titleTemperature.substring(0, titleTemperature.indexOf("°C") + 2);
                TextView temperatureTextView = findViewById(R.id.observationTitleTemperature);
                temperatureTextView.setText(temperatureCelsius);
            }

            // Display Weather Icon
            String weather = observation.get("Weather");
            if (weather != null && !weather.equals("Not available")) {
                // TextView weatherTextView = findViewById(R.id.observationWeather);
                // weatherTextView.setText(weather);

                ImageView weatherIconView = findViewById(R.id.weatherIcon);

                // Use the weatherIcons HashMap to set the icon
                Integer iconRes = weatherIcons.get(weather);
                if (iconRes != null) {
                    weatherIconView.setImageResource(iconRes);
                }
            }

            // Remove time from the day
            String day = observation.get("Date");
            if (day != null) {
                int colonIndex = day.indexOf(':');
                if (colonIndex != -1) {
                    day = day.substring(0, colonIndex - 2); // Remove time
                    currentWeatherTextView.setText("Current Weather: " + day);
                }
            }
        }
    }
