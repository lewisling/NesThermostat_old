package net.valentinc.nesthermostat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.TypefaceProvider;

import net.danlew.android.joda.JodaTimeAndroid;
import net.valentinc.seekarc.SeekArc;
import net.valentinc.server.Temperature;
import net.valentinc.ssh.SSHManager;

import org.joda.time.DateTimeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
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
    private Switch switchButton;
    private SeekArc seekArcTempHeater;
    private final float[] temp = new float[1];
    private final InputStream[] iss = new InputStream[2];
    private String[] resultat = new String[1];
    private String[] resultat1 = new String[1];
    private Timer tUpdateTemperature;
    private TimerTask tUpdateIHM;
    private TimerTask tUpdateTemperatureTask;
    private com.beardedhen.androidbootstrap.BootstrapProgressBar progressBar;
    private Boolean isRunning;
    private Time last_try;

    private String SHARED_PREFS;
    private String MINUTEBYDEGREE;

    private long last_update;
    private TextView tvLastUpdate;
    private SharedPreferences prefs;

    private SSHManager instance;
    private final static String userName = "***REMOVED***";
    private final static String password = "***REMOVED***";
    private final static String connectionIP = "***REMOVED***";
    private final static String command = "python /home/***REMOVED***/Script_python/android_heater.py ";
    private final static Double minuteToDeg = 2.15/150;
    private float minuteToDeg;
    private TextView tvRemaningTime;
    private TextView tvTempsRestant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SHARED_PREFS    = getString(R.string.shared_prefs);
        MINUTEBYDEGREE = getString(R.string.minutebydegree);
        JodaTimeAndroid.init(this);

        TypefaceProvider.registerDefaultIconSets();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_room_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        tvTempHeater = (TextView) findViewById(R.id.tvTempHeater);
        switchButton = (Switch) findViewById(R.id.toggleHeaterOn);
        tvLastUpdate = (TextView) findViewById(R.id.textViewLastUpdate);
        tvTempsRestant = (TextView) findViewById(R.id.tvTempsRestant);
        progressBar = (com.beardedhen.androidbootstrap.BootstrapProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        com.beardedhen.androidbootstrap.BootstrapButton updateButton = (com.beardedhen.androidbootstrap.BootstrapButton) findViewById(R.id.updateButton);
        tvRemaningTime = (TextView) findViewById(R.id.textViewRemainingTime);
        switchButton.setTextOff("Eteins");
        switchButton.setTextOn("Allumé");

        instance = new SSHManager(userName, password, connectionIP, "");

        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
        minuteToDeg = prefs.getFloat(MINUTEBYDEGREE, (float)0.021979);
        Log.d("MinuteByDegree",Float.toString(minuteToDeg));

        final String errorMessage = instance.connect();
        if (errorMessage != null) {
            Log.d("onClick",errorMessage);
            showError("Erreur Réseau");
        }
        String result = instance.sendCommand(commandUpdate);
        result = result.trim();
        if(!result.equals(""))
            last_update = Long.parseLong(result);
        else last_update = 0;

        if(last_update ==0){
            last_update = DateTimeUtils.currentTimeMillis();
        }
        else {
            Long millis = DateTimeUtils.currentTimeMillis() - last_update;
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = millis / daysInMilli;
            millis = millis % daysInMilli;

            long elapsedHours = millis / hoursInMilli;
            millis = millis % hoursInMilli;

            long elapsedMinutes = millis / minutesInMilli;
            millis = millis % minutesInMilli;

            long elapsedSeconds = millis / secondsInMilli;
            if(elapsedDays ==0){
                if(elapsedHours == 0) {
                    if(elapsedMinutes==0){
                        tvLastUpdate.setText(String.format("%d s",
                                elapsedSeconds
                        ));
                    }else{
                        tvLastUpdate.setText(String.format("%d min %d s",
                                elapsedMinutes, elapsedSeconds
                        ));
                    }
                }else{
                    tvLastUpdate.setText(String.format("%d h %d min %d s",
                            elapsedHours, elapsedMinutes, elapsedSeconds
                    ));
                }
            }else {
                tvLastUpdate.setText(String.format("%d J %d h %d min %d s",
                        elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
                ));
            }
            Log.d("LASTUPDATE", "LastUpdate : " + last_update);
        }


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Call heater function
                        setProgressBarUI(5);

                        setProgressBarUI(15);

                        final String result = instance.sendCommand(commandHeater + tvTempHeater.getText() + " " + switchButton.isChecked());
                        setProgressBarUI(30);
                        if (result != null && result.contains("Android_Heater.py") && result.contains("envois du signal")) { //TODO
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RoomPage.this, "Mise à jour Réussie", Toast.LENGTH_SHORT).show();
                                    progressBar.setProgress(100);
                                }
                            });
                            String command = String.format(commandLastUpdate,DateTimeUtils.currentTimeMillis());
                            instance.sendCommand(command);
                            Log.d("CommandLastUpdate",command);
                            last_update = DateTimeUtils.currentTimeMillis();

                        } else {
                            showError("Erreur Réseau");
                        }
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

        last_try = new Time();
        last_try.setToNow();

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

        start_timer();
        isRunning = false;

    }

    private void setProgressBarUI(final int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(i);
            }
        });
    }

    private void start_timer() {
        tUpdateTemperature = new Timer("Update Temperature");
        tUpdateTemperatureTask = new TimerTask() {
            @Override
            public void run() {
                setTemperature();
            }
        };
        tUpdateIHM = new TimerTask(){
            @Override
            public void run() {
                updateRemaingTime();
            }
        };
        tUpdateTemperature.schedule(tUpdateTemperatureTask,1000,5000);
        tUpdateTemperature.schedule(tUpdateIHM,1000,1000);
        isRunning = true;
    }

    private void updateRemaingTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long millis = DateTimeUtils.currentTimeMillis() - last_update;
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;

                long elapsedDays = millis / daysInMilli;
                millis = millis % daysInMilli;

                long elapsedHours = millis / hoursInMilli;
                millis = millis % hoursInMilli;

                long elapsedMinutes = millis / minutesInMilli;
                millis = millis % minutesInMilli;

                long elapsedSeconds = millis / secondsInMilli;
                if(elapsedDays ==0){
                    if(elapsedHours == 0) {
                        if(elapsedMinutes==0){
                            tvLastUpdate.setText(String.format("%d s",
                                    elapsedSeconds
                            ));
                        }else{
                            tvLastUpdate.setText(String.format("%d min %d s",
                                    elapsedMinutes, elapsedSeconds
                            ));
                        }
                    }else{
                        tvLastUpdate.setText(String.format("%d h %d min %d s",
                                elapsedHours, elapsedMinutes, elapsedSeconds
                        ));
                    }
                }else {
                    tvLastUpdate.setText(String.format("%d J %d h %d min %d s",
                            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
                    ));
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        instance.close();
        if(isRunning) {
            tUpdateTemperatureTask.cancel();
            tUpdateIHM.cancel();
            isRunning = false;
            Log.d("onPause","isRunning false");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        instance.connect();
        if(!isRunning) {
            start_timer();
            Log.d("onPause","isRunning true");
        }

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
    @Override
    protected void onStop(){
        super.onStop();
        instance.close();
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
                    switchButton.setChecked(res);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            showError("Erreur Réseau");
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
            showError("Erreur Réseau");
        }
    }

    public InputStream getISFromURL(URL url) throws IOException {
        InputStream is;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        int response = conn.getResponseCode();
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
            temp[0] = Temperature.getCurrentTemperature();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekArcTempHeater.setProgress((int) temp[0]);
                }
            });
            UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
            UpdateIHM(String.valueOf((int) ((temp[0] - ((int) temp[0])) * 10)), tvDecDeg);
            String sTemp = tvTempHeater.getText().toString();
            float tem;
            if(sTemp != "") {
                tem = (Float.parseFloat(sTemp) - temp[0]);
            }
            else{
                tem = 0.0f;
            }
            if(tem<=0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvRemaningTime.setVisibility(View.INVISIBLE);
                        tvTempsRestant.setVisibility(View.INVISIBLE);
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvRemaningTime.setVisibility(View.VISIBLE);
                        tvTempsRestant.setVisibility(View.VISIBLE);
                    }
                });
                Float fminutes = tem / minuteToDeg;
                long minutes = fminutes.longValue();
                long hoursInMinutes = 60;

                long elapsedHours = minutes / hoursInMinutes;
                minutes = minutes % hoursInMinutes;

                UpdateIHM(String.format("%d h %d min",
                        elapsedHours,
                        minutes
                ), tvRemaningTime);
            }
        } catch (final IOException e) {
            showError("Erreur Réseau ");
        }
    }

    private void showError(final String msg){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RoomPage.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
}