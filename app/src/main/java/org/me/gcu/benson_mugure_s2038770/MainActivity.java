//
// Name                 Benson Mugure
// Student ID           S2038770
// Programme of Study   Computing
//

// // UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.benson_mugure_s2038770;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView forecastDisplay;
    private TextView observationDisplay; // Add TextView for observation display
    private Button prevButton;
    private Button nextButton;
    private TextView locationDisplay;

    private HashMap<String, String> locationCodes;
    private String currentLocation;
    private String[] forecastData;
    private String observationData; // Store observation data as a String

    private int currentLocationIndex;
    private List<String> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        forecastDisplay = findViewById(R.id.forecastDisplay);
        observationDisplay = findViewById(R.id.observationDisplay); // Initialize observation display TextView
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        locationDisplay = findViewById(R.id.locationDisplay);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

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

        // Fetch and display forecast data for the default location
        fetchAndDisplayForecastData(currentLocation);

        // Fetch and display observation data
        fetchAndDisplayObservationData(currentLocation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prevButton) {
            currentLocationIndex = (currentLocationIndex - 1 + locations.size()) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
            fetchAndDisplayObservationData(currentLocation);
        } else if (v.getId() == R.id.nextButton) {
            currentLocationIndex = (currentLocationIndex + 1) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
            fetchAndDisplayObservationData(currentLocation);
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

    private void fetchAndDisplayObservationData(String location) {
        String locationCode = locationCodes.get(location);
        String url = "https://weather-broker-cdn.api.bbci.co.uk/en/observation/rss/" + locationCode;
        new Thread(() -> {
            String data = fetchWeatherData(url);
            observationData = parseObservationData(data);
            runOnUiThread(() -> displayObservationData());
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

    // Loop over the channel
    // Ignore the first title and the first description
    // Get the second title from inside the image tag then loop over the item tags, extracting the title and description for each item as the 2 are linked together; also get at least one georss:point tag's data and store it
    // Find a suitable way to store the extracted info. The georss:point tag's data is the lat-lon data, i.e., map data. The second title gives the location, the other titles give forecasts for the 3 days and their corresponding descriptions give deeper forecasts. As such, I suggest storing them as 3: Location; a list of 3 dictionaries for the 3 days each with the keys for title and description; as well as the map data
    // - Perhaps get the date
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
    
        return forecastData;
    }    

    private String parseObservationData(String data) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(data));
    
            StringBuilder observation = new StringBuilder();
            int eventType = parser.getEventType();
            boolean firstTitleSkipped = false;
            boolean firstDescriptionSkipped = false;
    
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                    if (!firstTitleSkipped) {
                        firstTitleSkipped = true;
                    } else {
                        String title = parser.nextText().trim();
                        observation.append(title).append("\n");
                    }
                } else if (eventType == XmlPullParser.START_TAG && parser.getName().equals("description")) {
                    if (!firstDescriptionSkipped) {
                        firstDescriptionSkipped = true;
                    } else {
                        String description = parser.nextText().trim();
                        observation.append(description).append("\n");
                    }
                }
                eventType = parser.next();
            }
    
            return observation.toString();
        } catch (XmlPullParserException | IOException e) {
            Log.e("MainActivity", "Error parsing latest Observation data", e); // Log the error
            return "";
        }
    }        

    private void displayForecastData(Map<String, Object> forecastData) {
        StringBuilder displayText = new StringBuilder();
    
        // Extracting location
        String location = (String) forecastData.get("location");
        if (location != null) {
            displayText.append("Location: ").append(location).append("\n\n");
        }
    
        // Extracting forecasts
        List<Map<String, String>> forecasts = (List<Map<String, String>>) forecastData.get("forecasts");
        if (forecasts != null) {
            for (Map<String, String> forecast : forecasts) {
                String title = forecast.get("title");
                String description = forecast.get("description");
                if (title != null && description != null) {
                    displayText.append(title).append(": ").append(description).append("\n\n");
                }
            }
        }
    
        // Extracting georssPoint
        String georssPoint = (String) forecastData.get("georssPoint");
        if (georssPoint != null) {
            displayText.append("Georss Point: ").append(georssPoint).append("\n");
        }
    
        forecastDisplay.setText(displayText.toString());
        locationDisplay.setText(currentLocation);
        locationDisplay.setVisibility(View.VISIBLE); // Show the location display TextView
    }    

    private void displayObservationData() {
        observationDisplay.setText(observationData);
        observationDisplay.setVisibility(View.VISIBLE); // Show the observation display TextView
    }
}
