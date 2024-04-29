//
// Name                 Benson Mugure
// Student ID           S2038770
// Programme of Study   Computing
//
package org.me.gcu.benson_mugure_s2038770;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * 
 * This class is responsible for displaying the help screen.
 * The user can navigate to the home screen, settings screen, or exit the application.
 * 
 * Student ID: S2038770
 * 
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);

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
                    Intent intent = new Intent(HelpActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_settings) {
                    // Handle the settings action
                    // Open SettingsActivity when settings button is clicked
                    Intent intent = new Intent(HelpActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_help) {
                    // Handle the help action
                    Intent intent = new Intent(HelpActivity.this, HelpActivity.class);
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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