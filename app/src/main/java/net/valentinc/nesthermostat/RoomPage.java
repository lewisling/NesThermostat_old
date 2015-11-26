package net.valentinc.nesthermostat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import net.valentinc.circularseekbar.CircularSeekBar;
import net.valentinc.server.Temperature;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    private TextView tvTempHeater;
    private CircularSeekBar seekBar;
    private ToggleButton toggleButton;
    private Button updateButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        tvTempHeater = (TextView) findViewById(R.id.tvTempHeater);
        toggleButton = (ToggleButton) findViewById(R.id.toggleHeaterOn);
        updateButton = (Button) findViewById(R.id.updateButton);

        toggleButton.setTextOff("Eteins");
        toggleButton.setTextOn("Allumé");

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Call heater function
                        try {
                            System.out.println(getISFromURL(new URL("http://88.142.52.11/android/android_heater.php?temp=" + tvTempHeater.getText() + "&activated=" + toggleButton.isChecked())).read());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        final int[] save_progress = {0};

        seekBar = (CircularSeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(30);
        seekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, final int progress, boolean fromUser) {
                if(fromUser)
                    if(progress!= save_progress[0]){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTempHeater.setText(String.valueOf(progress));
                                save_progress[0] =progress;
                            }
                        });
                    }
                else {
                        save_progress[0]=progress;
                    }
            }
            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {}
        });

        //Get the current temp from the sensor to set
        final float[] temp = new float[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                temp[0] = Temperature.getCurrentTemperature();
                UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
                UpdateIHM(String.valueOf((int)((temp[0]-((int) temp[0]))*10)), tvDecDeg);
            }
        }).start();

        //Get the current temp from the heater to set progress bar

        final InputStream[] iss = {null,null};
        final String[] resultat = new String[1];
        final String[] resultat1 = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iss[0] = getISFromURL(new URL("http://88.142.52.11/files/val_required_temp_file"));
                    resultat[0] = inputStreamToString(iss[0]);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    iss[1] = getISFromURL(new URL("http://88.142.52.11/files/is_on_heater"));
                    resultat1[0] = inputStreamToString(iss[1]);
                    final boolean res = Boolean.parseBoolean(resultat1[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleButton.setChecked(res);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    public static String inputStreamToString(InputStream is) throws IOException {
        char[] buffer = new char[10];
        Reader reader = new InputStreamReader(is, "UTF-8");
        reader.read(buffer);
        return new String(buffer);
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