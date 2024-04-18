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
            Log.d("extras: ", extras.toString());

            // Log.d("ObservationData: ", observationData.toString());

            // Display the observation data
            if (observationData != null) {
                TextView observationTextView = findViewById(R.id.observationTextView);
                observationTextView.setText("Observation Data: " + observationData.toString());
            }
        }
    }
}
