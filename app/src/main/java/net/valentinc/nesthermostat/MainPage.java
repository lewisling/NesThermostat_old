package net.valentinc.nesthermostat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
        //Get the current temp from the sensor to put it inside the 2 textviews
        start_timer();

        new Thread(new Runnable() {
            @Override
            public void run() {
                setTemperature();
            }
        }).start();
                //TODO Get the current temp from the weather to the textview under the logo
                //http://www.survivingwithandroid.com/2013/05/build-weather-app-json-http-android.html
        Openweathermap o = null;
        try {
            o = JsonParser.jsonToPOJO();
        } catch (IOException e) {
          //  Toast.makeText(this,"Error while using jsonToPojo : " +e,Toast.LENGTH_LONG);
            tvMeteo.setText("Error while using jsonToPojo : " + e);
            Log.d("OpenWeatherMap : ", e.toString());
        }
        //Toast.makeText(this,"JsonToPojo : " + o.getMain().getHumidity() ,Toast.LENGTH_LONG);
        if(o!=null) {
            String meteo_text = o.getName()
                    + " - " +
                    o.getMain().getTemp() + "°C - " +
                    o.getWeather().get(0).getDescription();
            tvMeteo.setText(meteo_text);
        }
    }

    private void start_timer() {
        tUpdateTemperature = new Timer("Update Temperature");
        tUpdateTemperatureTask = new TimerTask() {
            @Override
            public void run() {
                setTemperature();
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
            Log.d("DEBUG", "setTemperature in progress from RoomPage");
            temp[0] = Temperature.getCurrentTemperature();
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Erreur Réseau", Toast.LENGTH_SHORT).show();
                }
            });
        }
        UpdateIHM(String.valueOf((int) temp[0]), tvDeg);
        UpdateIHM(String.valueOf((int)((temp[0]-((int) temp[0]))*10)), tvDecDeg);
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
