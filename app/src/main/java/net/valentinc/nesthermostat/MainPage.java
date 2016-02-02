package net.valentinc.nesthermostat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.valentinc.openweathermap.JsonParser;
import net.valentinc.openweathermap.Openweathermap;
import net.valentinc.server.Temperature;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainPage extends Activity {

    private TextView tvDeg;
    private TextView tvDecDeg;
    private TextView tvMeteo;
    private Timer tUpdateTemperature;
    private TimerTask tUpdateTemperatureTask;
    private Boolean isRunning;
    private Time last_try;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        tvMeteo = (TextView) findViewById(R.id.tvMeteo);
        ImageView imageView = (ImageView) findViewById(R.id.imageViewChambre);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RoomPage.class);
                startActivity(intent);
            }
        });
        isRunning = false;

        last_try = new Time();
        last_try.setToNow();
        //Get the current temp from the sensor to put it inside the 2 textviews
        start_timer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                setTemperature();
                setWeather();
            }
        }).start();
    }

    private void setWeather() {
        Openweathermap o = null;
        try {
            o = JsonParser.jsonToPOJO();
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMeteo.setText("Impossible de récupérer les infos.");
                }
            });
            Log.d("OpenWeatherMap : ", e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(o!=null) {
            final String meteo_text = o.getName()
                    + " - " +
                    o.getMain().getTemp() + "°C - " +
                    o.getWeather().get(0).getDescription();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMeteo.setText(meteo_text);
                }
            });
        }
    }

    private void start_timer() {
        tUpdateTemperature = new Timer("Update Temperature");
        tUpdateTemperatureTask = new TimerTask() {
            @Override
            public void run() {
                setTemperature();
                setWeather();
            }
        };
        tUpdateTemperature.schedule(tUpdateTemperatureTask,1000,5000);
        isRunning = true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(isRunning) {
            tUpdateTemperatureTask.cancel();
            isRunning = false;
            Log.d("onPause","isRunning false");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!isRunning) {
            start_timer();
            Log.d("onPause","isRunning true");
        }
    }

    private void setTemperature() {
        final float[] temp = new float[1];
        try {
            temp[0] = Temperature.getCurrentTemperature();
            UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
            UpdateIHM(String.valueOf((int)((temp[0]-((int) temp[0]))*10)), tvDecDeg);
        } catch (IOException e) {
            showError("Erreur Réseau ");
        }
    }

    public void UpdateIHM(final String res, final TextView t) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t.setText(res);
            }
        });
    }

    private void showError(final String msg){
        Time now = new Time();
        now.setToNow();
        if(Time.compare(now,last_try)>10) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainPage.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
            last_try.setToNow();
        }
    }
}
