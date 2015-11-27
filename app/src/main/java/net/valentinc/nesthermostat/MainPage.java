package net.valentinc.nesthermostat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.valentinc.server.Temperature;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainPage extends Activity {

    private TextView tvDeg;
    private TextView tvDecDeg;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_page);
        tvDeg = (TextView) findViewById(R.id.tvDeg);
        tvDecDeg = (TextView) findViewById(R.id.tvDecDeg);
        imageView = (ImageView) findViewById(R.id.imageViewChambre);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RoomPage.class);
                startActivity(intent);
            }
        });

        //Get the current temp from the sensor to put it inside the 2 textviews
        final float[] temp = new float[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
        }).start();
        //Get the current temp from the weather to the textview under the logo
//http://www.survivingwithandroid.com/2013/05/build-weather-app-json-http-android.html
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
