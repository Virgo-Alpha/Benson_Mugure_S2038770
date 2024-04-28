package org.me.gcu.benson_mugure_s2038770;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceFragmentCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button saveButton;
    private ToggleButton darkModeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Find the ImageButton for the settings icon
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        // Find the DrawerLayout
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        // Set click listener for the settings button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the navigation drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Handle the home action
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    // Handle the settings action
                    // Open SettingsActivity when settings button is clicked
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_help) {
                    // Handle the help action
                    Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_exit) {
                    // Handle the exit action
                    showExitDialog();
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

        });

        timePicker = findViewById(R.id.timePicker);
        saveButton = findViewById(R.id.saveButton);

        // Load saved time
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int savedHour = preferences.getInt("refresh_hour", 8);
        int savedMinute = preferences.getInt("refresh_minute", 0);
        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Save selected time
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("refresh_hour", hour);
                editor.putInt("refresh_minute", minute);
                editor.apply();

                Toast.makeText(SettingsActivity.this, "Time saved: " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize views
        Button backButton = findViewById(R.id.backButton);
        darkModeToggle = findViewById(R.id.darkModeToggle);

        // Set click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the SettingsActivity and go back to MainActivity
            }
        });

        // Set listener for the dark mode toggle
        darkModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state of dark mode here
                // You can use SharedPreferences to save the state
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("darkMode", isChecked);
                editor.apply();

                // Apply dark mode immediately
                if (isChecked) {
                    applyDarkMode();
                } else {
                    applyLightMode();
                }
            }
        });

        // Set initial state of dark mode toggle based on saved preference
        boolean darkModeEnabled = preferences.getBoolean("darkMode", false);
        darkModeToggle.setChecked(darkModeEnabled);
    }

    private void applyDarkMode() {
        // Apply dark mode using setLocalNightMode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void applyLightMode() {
        // Apply light mode using setLocalNightMode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit the application
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}

