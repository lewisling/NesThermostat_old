package net.valentinc.nesthermostat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_page);

        //Get the current temp from the sensor to put it inside the 2 textviews

        //Get the current temp from the weather to the textview under the logo

        // on click listener for imageview to go to the new activity

        // TODO : add a logo to see directly if heater is on or not
    }
}
