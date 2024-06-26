//
// Name                 Benson Mugure
// Student ID           S2038770
// Programme of Study   Computing
//
package org.me.gcu.benson_mugure_s2038770;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Map;

/**
 * 
 * This class is responsible for displaying the full forecast data.
 * It receives the forecast data and location from the MainActivity and displays it in a TextView.
 * The user can go back to the MainActivity by clicking the back button.
 * 
 * Student ID: S2038770
 * 
 */
public class full_forecast extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_forecast);

        // Retrieve the observation data from the intent
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Map<String, Object> ForecastData = (Map<String, Object>) extras.getSerializable("forecast");
            String location = extras.getString("location");

            // Display the observation data
            if (ForecastData != null) {
                Log.d("Full forecast: ", ForecastData.toString());
                // Find the TextView in the layout
                TextView forecastTextView = findViewById(R.id.forecastTextView);
                TextView locationTextView = findViewById(R.id.separationText);

                // Create a StringBuilder to build the formatted string
                StringBuilder forecastText = new StringBuilder();

                // Extracting forecasts
                List<Map<String, String>> forecasts = (List<Map<String, String>>) ForecastData.get("forecasts");
                if (forecasts != null) {
                    for (Map<String, String> forecast : forecasts) {
                        String title = forecast.get("title");
                        String description = forecast.get("description");
                        if (title != null && description != null) {
                            forecastText.append(title).append(": ").append(description).append("\n\n");
                        }
                    }
                }

                // Set the formatted string to the TextView
                forecastTextView.setText(forecastText.toString());
                locationTextView.setText(location);
                locationTextView.setVisibility(View.VISIBLE); // Show the location display TextView
            }
        }

        // Set up back button click listener
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity to go back
                finish();
            }
        });
    }
}