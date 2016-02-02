package net.valentinc.nesthermostat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.valentinc.seekarc.SeekArc;
import net.valentinc.server.Temperature;
import net.valentinc.ssh.SSHManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ValentinC on 19/11/2015.
 *
 */
public class RoomPage extends Activity {

    private TextView tvDeg;
    private TextView tvDecDeg;
    private TextView tvTempHeater;
    private SeekArc seekBar;
    private ToggleButton toggleButton;
    private SeekArc seekArcTempHeater;
    private final float[] temp = new float[1];
    private final InputStream[] iss = new InputStream[2];
    private String[] resultat = new String[1];
    private String[] resultat1 = new String[1];
    private Timer tUpdateTemperature;
    private TimerTask tUpdateTemperatureTask;

    private SSHManager instance;
    private String userName ="";
    private String password ="";
    private String connectionIP ="";
    private String command = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        tvTempHeater = (TextView) findViewById(R.id.tvTempHeater);
        toggleButton = (ToggleButton) findViewById(R.id.toggleHeaterOn);
        Button updateButton = (Button) findViewById(R.id.updateButton);

        toggleButton.setTextOff("Eteins");
        toggleButton.setTextOn("Allumé");

        //TODO : check if connection lost
        instance = new SSHManager(userName, password, connectionIP, "");

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Call heater function
                        final String errorMessage = instance.connect();

                        if (errorMessage != null) {
                            System.out.println(errorMessage);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RoomPage.this, "Erreur : " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        final String result = instance.sendCommand(command + tvTempHeater.getText() + " " + toggleButton.isChecked());
                        if (result.contains("Android_Heater.py") && result.contains("envois du signal")) { //TODO
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RoomPage.this, "Mise à jour Réussie", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RoomPage.this, "Erreur : " + result, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        instance.close();
                    }
                }).start();
            }
        });

        final int[] save_progress = {0};

        seekBar = (SeekArc) findViewById(R.id.seekArc2);
        seekBar.setArcRotation(-120);
        seekBar.setSweepAngle(240);
        seekBar.setTouchInSide(false);
        seekBar.setmMax(30);

        seekBar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, final int progress, boolean fromUser) {
                if (fromUser)
                    if (progress != save_progress[0]) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTempHeater.setText(String.valueOf(progress));
                                save_progress[0] = progress;
                            }
                        });
                    } else {
                        save_progress[0] = progress;
                    }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });
        seekBar.setProgressWidth(30);

        seekArcTempHeater = (SeekArc) findViewById(R.id.seekArc);
        seekArcTempHeater.setmMax(30);
        seekArcTempHeater.setSweepAngle(240);
        seekArcTempHeater.setArcRotation(-120);
        seekArcTempHeater.setTouchInSide(true);
        seekArcTempHeater.setEnabled(false);

        //Get the current temp from the sensor to set
        new Thread(new Runnable() {
            @Override
            public void run() {
                setTemperature();
            }
        }).start();

        tUpdateTemperature = new Timer("Update Temperature");
        tUpdateTemperatureTask = new TimerTask() {
            @Override
            public void run() {
                setTemperature();
            }
        };
        tUpdateTemperature.schedule(tUpdateTemperatureTask,1000,5000);
        //Get the current temp from the heater to set progress bar
        new Thread(new Runnable() {
            @Override
            public void run() {
                setRequieredTemp();
            }
        }).start();
        // Get the on/off of the heater to instantiate the switch
        new Thread(new Runnable() {
            @Override
            public void run() {
                setSwitchOnOff();
            }
        }).start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        tUpdateTemperatureTask.cancel();
    }

    @Override
    protected void onResume(){
        super.onResume();
        tUpdateTemperatureTask.run();

        new Thread(new Runnable() {
            @Override
            public void run() {
                setTemperature();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setSwitchOnOff();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setRequieredTemp();
            }
        }).start();
    }

    private void setSwitchOnOff() {
        try {
            iss[1] = getISFromURL(new URL("http://***REMOVED***/files/activated_file"));
            resultat1[0] = inputStreamToString(iss[1]);
            iss[1].close();
            final boolean res;
            res = resultat1[0].substring(0, 1).equals("t") || resultat1[0].substring(0, 1).equals("T");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleButton.setChecked(res);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RoomPage.this, "Erreur Réseau " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setRequieredTemp() {
        try {
            iss[0] = getISFromURL(new URL("http://***REMOVED***/files/val_required_temp_file"));
            resultat[0] = inputStreamToString(iss[0]);
            iss[0].close();
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
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RoomPage.this, "Erreur Réseau " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public InputStream getISFromURL(URL url) throws IOException {
        InputStream is;
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
        else {
            throw new IOException("Serveur Injoignable");
        }
        return is;
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        char[] buffer = new char[1000];
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

    public void setTemperature(){
        try {
            Log.d("DEBUG","setTemperature in progress from RoomPage");
            temp[0] = Temperature.getCurrentTemperature();
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RoomPage.this, "Erreur Réseau " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekArcTempHeater.setProgress((int) temp[0]);
            }
        });
        UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
        UpdateIHM(String.valueOf((int) ((temp[0] - ((int) temp[0])) * 10)), tvDecDeg);
    }
}