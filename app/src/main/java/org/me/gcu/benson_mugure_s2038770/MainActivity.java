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
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView forecastDisplay;
    private Button prevButton;
    private Button nextButton;

    private HashMap<String, String> locationCodes;
    private String currentLocation;
    private String[] forecastData;
    private int currentLocationIndex;
    private List<String> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        forecastDisplay = findViewById(R.id.forecastDisplay);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prevButton) {
            currentLocationIndex = (currentLocationIndex - 1 + locations.size()) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
        } else if (v.getId() == R.id.nextButton) {
            currentLocationIndex = (currentLocationIndex + 1) % locations.size();
            currentLocation = locations.get(currentLocationIndex);
            fetchAndDisplayForecastData(currentLocation);
        }
    }

    private void fetchAndDisplayForecastData(String location) {
        String locationCode = locationCodes.get(location);
        String url = constructForecastUrl(locationCode);
        new Thread(() -> {
            String data = fetchForecastData(url);
            forecastData = parseForecastData(data);
            runOnUiThread(this::displayForecastData);
        }).start();
    }

    private String constructForecastUrl(String locationCode) {
        return "https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/" + locationCode;
    }

    private String fetchForecastData(String url) {
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
            e.printStackTrace();
            return "";
        }
    }

    private String[] parseForecastData(String data) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(data));

            String[] forecast = new String[3];
            int eventType = parser.getEventType();
            int dayIndex = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                    String title = parser.nextText().trim();
                    // Check if title represents a forecast for a day
                    if (title.contains(":")) {
                        forecast[dayIndex++] = title;
                    }
                }
                eventType = parser.next();
            }

            return forecast;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    private void displayForecastData() {
        StringBuilder displayText = new StringBuilder();
        for (String forecast : forecastData) {
            displayText.append(forecast).append("\n");
        }
        forecastDisplay.setText(displayText.toString());

        // Set the text of the location display TextView
        TextView locationDisplay = findViewById(R.id.locationDisplay);
        locationDisplay.setText("Location: " + currentLocation);
        locationDisplay.setVisibility(View.VISIBLE); // Show the TextView
    }
}
