package net.valentinc.nesthermostat;

import android.app.Activity;
import android.os.Bundle;
import net.valentinc.CircularSeekBar.CircularSeekBar;

/**
 * Created by ValentinC on 19/11/2015.
 */
public class RoomPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_page);

        CircularSeekBar seekbar = (CircularSeekBar) findViewById(R.id.circularSeekBar1);
        seekbar.setOnSeekBarChangeListener(new CircleSeekBarListener());

        //Get the current temp from the sensor to set ...

        //Get the current temp from the heater to set progress bar
        //seekbar.setProgress(50);

        // Get the on/off of the heater to instantiate the switch


    }

    private class CircleSeekBarListener implements CircularSeekBar.OnCircularSeekBarChangeListener {
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            // TODO to instantiate the 2 textviews

            // and send to the server

            // meaning that turn on the heater (be carreful at the switch - call a function for that)

        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
    }
}