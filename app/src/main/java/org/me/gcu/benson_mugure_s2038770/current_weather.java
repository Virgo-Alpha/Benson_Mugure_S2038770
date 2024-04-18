package org.me.gcu.benson_mugure_s2038770;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class current_weather extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        // Retrieve the observation data from the intent
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Map<String, String> observationData = (Map<String, String>) extras.getSerializable("observation");
            String location = extras.getString("location");

            // Display the observation data
            if (observationData != null) {
                // Find the TextView in the layout
                TextView observationTextView = findViewById(R.id.observationTextView);
                TextView locationTextView = findViewById(R.id.separationText);
                
                // Create a StringBuilder to build the formatted string
                StringBuilder observationText = new StringBuilder();

                // Iterate over the observation data map and append key-value pairs to the StringBuilder
                for (Map.Entry<String, String> entry : observationData.entrySet()) {
                    observationText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }

                // Set the formatted string to the TextView
                observationTextView.setText(observationText.toString());
                locationTextView.setText(location);
            }
        }
    }
}
