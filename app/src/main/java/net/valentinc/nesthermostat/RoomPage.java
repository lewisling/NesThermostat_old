package net.valentinc.nesthermostat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.valentinc.circularseekbar.CircularSeekBar;
import net.valentinc.server.Temperature;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by ValentinC on 19/11/2015.
 *
 */
public class RoomPage extends Activity {

    private TextView tvDeg;
    private TextView tvDecDeg;
    private ToggleButton toggleButton;
    private TextView tvTempHeater;
    private CircularSeekBar seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        toggleButton = (ToggleButton) findViewById(R.id.toggleHeaterOn);
        tvTempHeater = (TextView) findViewById(R.id.tvTempHeater);

        seekBar = (CircularSeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(30);
        seekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
        //Get the current temp from the sensor to set
        final float[] temp = new float[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                temp[0] = Temperature.getCurrentTemperature();
                UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
                UpdateIHM(String.valueOf((int) (((int) temp[0]) - temp[0])), tvDecDeg);
            }
        }).start();

        //Get the current temp from the heater to set progress bar

        final InputStream[] iss = {null};
        final String[] resultat = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iss[0] = getISFromURL(new URL("http://88.142.52.11/files/val_required_temp_file"));
                    resultat[0] = Temperature.inputStreamToString(iss[0]);
                    resultat[0] = resultat[0].substring(0, 2);
                    UpdateIHM(resultat[0], tvTempHeater);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(Integer.parseInt(resultat[0]));
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Get the on/off of the heater to instantiate the switch


    }

    public InputStream getISFromURL(URL url) {
        InputStream is = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("RoomPage", "The response is: " + response);
            if (response == 200) {
                is = conn.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    public void UpdateIHM(final String res, final TextView t) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t.setText(res);
            }
        });
    }
}