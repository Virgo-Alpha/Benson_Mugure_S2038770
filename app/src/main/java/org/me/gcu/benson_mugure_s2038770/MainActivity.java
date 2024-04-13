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
// Add import statements
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener
{
    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private String url1="";
    private String urlSource="https://weather-broker-cdn.api.bbci.co.uk/en/forecast/rss/3day/2643123";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        // More Code goes here
    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    } //

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        private String url;

        public Task(String aurl)
        {
            url = aurl;
        }

        // ! Pullparse the XML data from the BBC Weather RSS feed
        @Override
        public void run() {
            try {
                URL aurl = new URL(url);
                URLConnection yc = aurl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                // Initialize XML parser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(in);

                // Variables to store forecast information
                String todayForecast = "";
                String tomorrowForecast = "";
                String dayAfterTomorrowForecast = "";

                // Start parsing
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                        String title = parser.nextText();
                        if (title.startsWith("Today:")) {
                            todayForecast = title;
                        } else if (title.startsWith("Sunday:")) {
                            tomorrowForecast = title;
                        } else if (title.startsWith("Monday:")) {
                            dayAfterTomorrowForecast = title;
                        }
                    }
                    eventType = parser.next();
                }
                in.close();

                // Format and display the forecast information in the UI
                final String formattedForecast = "Today: " + todayForecast.substring(todayForecast.indexOf(":") + 2) + "\n"
                        + "Tomorrow: " + tomorrowForecast.substring(tomorrowForecast.indexOf(":") + 2) + "\n"
                        + "Day after Tomorrow: " + dayAfterTomorrowForecast.substring(dayAfterTomorrowForecast.indexOf(":") + 2);

                // Update UI
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("UI thread", "I am the UI thread");
                        rawDataDisplay.setText(formattedForecast);
                    }
                });
            } catch (Exception e) {
                Log.e("MyTag", "Error occurred: " + e.getMessage());
            }
        }


    }

}