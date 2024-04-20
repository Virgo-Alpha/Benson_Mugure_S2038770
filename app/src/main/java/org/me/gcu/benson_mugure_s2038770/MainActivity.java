//
// Name                 Benson Mugure
// Student ID           S2038770
// Programme of Study   Computing
//

// // UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.benson_mugure_s2038770;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView forecastDisplay;
    // private TextView observationDisplay; // Add TextView for observation display
    private TextView observationDay;  // ! Below here
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        forecastDisplay = findViewById(R.id.forecastDisplay);
        // observationDisplay = findViewById(R.id.observationDisplay); // Initialize observation display TextView
        observationDay = findViewById(R.id.observationDay); // Initialize observation display TextView
        observationTime = findViewById(R.id.observationTime); // Initialize observation display TextView
        observationWeather = findViewById(R.id.observationWeather); // Initialize observation display TextView
        observationTitleTemperature = findViewById(R.id.observationTitleTemperature); // Initialize observation display TextView
        observationTemperature = findViewById(R.id.observationTemperature); // Initialize observation display TextView
        observationWindDirection = findViewById(R.id.observationWindDirection); // Initialize observation display TextView
        observationWindSpeed = findViewById(R.id.observationWindSpeed); // Initialize observation display TextView
        observationHumidity = findViewById(R.id.observationHumidity); // Initialize observation display TextView
        observationPressure = findViewById(R.id.observationPressure); // Initialize observation display TextView
        observationVisibility = findViewById(R.id.observationVisibility); // Initialize observation display TextView
        observationDate = findViewById(R.id.observationDate); // Initialize observation display TextView
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        navigateButton = findViewById(R.id.navigateButton);
        seeFullForecastButton = findViewById(R.id.seeFullForecastButton);
        locationDisplay = findViewById(R.id.locationDisplay);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        navigateButton.setOnClickListener(this);
        seeFullForecastButton.setOnClickListener(this);

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

    private void displayObservationData(Map<String, String> observation) {
        observationDay.setText(observation.get("Day"));
        observationTime.setText(observation.get("Time"));
        observationWeather.setText(observation.get("Weather"));
        observationTitleTemperature.setText(observation.get("TitleTemperature"));
        observationTemperature.setText(observation.get("Temperature"));
        observationWindDirection.setText(observation.get("Wind Direction"));
        observationWindSpeed.setText(observation.get("Wind Speed"));
        observationHumidity.setText(observation.get("Humidity"));
        observationPressure.setText(observation.get("Pressure"));
        observationVisibility.setText(observation.get("Visibility"));
        observationDate.setText(observation.get("Date"));
    }    
}
