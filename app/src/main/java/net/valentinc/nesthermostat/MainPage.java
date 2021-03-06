package net.valentinc.nesthermostat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import net.valentinc.nesthermostat.Services.QuickstartPreferences;
import net.valentinc.nesthermostat.Services.RegistrationIntentService;
import net.valentinc.openweathermap.JsonParser;
import net.valentinc.openweathermap.Openweathermap;
import net.valentinc.server.Temperature;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainPage extends Activity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    private String SHARED_PREFS;
    private String MINUTEBYDEGREE;

    private final static int DAY_UPDATE_TIME =7;
    private String URL_PHP_MINUTEDEGREE;

    private TextView tvDeg;
    private TextView tvDecDeg;
    private TextView tvMeteo;
    private TimerTask tUpdateTemperatureTask;
    private Boolean isRunning;
    private Time last_try;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String DATE_LAST_UPDATE = getString(R.string.date_last_update);
        SHARED_PREFS    = getString(R.string.shared_prefs);
        MINUTEBYDEGREE = getString(R.string.minutebydegree);
        URL_PHP_MINUTEDEGREE = getString(R.string.URL_PHP_MINUTEDEGREE);

        /****************** RECEIVER / SERVICES *****/
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG,getString(R.string.gcm_send_message));
                } else {
                    Log.d(TAG, getString(R.string.token_error_message));
                }
            }
        };// Registering BroadcastReceiver
        registerReceiver();
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        /*********** APP ****************/
        TypefaceProvider.registerDefaultIconSets();

        setContentView(R.layout.activity_main_page);
        mContext = this.getApplicationContext();
        /**/
        SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Long date_lastUpdate = prefs.getLong(DATE_LAST_UPDATE, 0);
        if (date_lastUpdate == 0) {
            date_lastUpdate = System.currentTimeMillis();
            editor.putLong(DATE_LAST_UPDATE, date_lastUpdate);
            editor.commit();
            //retrieve Info
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        retrieveMinuteByDegreeValueFromPhp();
                    } catch (final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Erreur MinuteByDegree" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();
        }
        else if (System.currentTimeMillis() >= date_lastUpdate + DAY_UPDATE_TIME *24*60*60*1000 ) {
            //retrieve Info
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        retrieveMinuteByDegreeValueFromPhp();
                    } catch (final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Erreur MinuteByDegree" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();
        }

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
                    tvMeteo.setText(R.string.errorInfos);
                }
            });
            Log.d("OpenWeatherMap : ", e.toString());
        } catch (InterruptedException | ExecutionException e) {
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
        Timer tUpdateTemperature = new Timer("Update Temperature");
        tUpdateTemperatureTask = new TimerTask() {
            @Override
            public void run() {
                setTemperature();
                setWeather();
            }
        };
        tUpdateTemperature.schedule(tUpdateTemperatureTask, 1000, 5000);
        isRunning = true;
    }

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
        if(isRunning) {
            tUpdateTemperatureTask.cancel();
            isRunning = false;
            Log.d("onPause", "isRunning false");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver();
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

    private void retrieveMinuteByDegreeValueFromPhp() throws IOException {
        SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor editor = prefs.edit();

        HttpClient httpclient=new DefaultHttpClient();
        HttpPost httppost=new HttpPost(URL_PHP_MINUTEDEGREE);
        HttpResponse response = httpclient.execute(httppost);

        if ( response.getStatusLine().getStatusCode() == 200){
            String str =  EntityUtils.toString(response.getEntity());
            float minuteByDegree = Float.valueOf(str.substring(1));
            if (minuteByDegree > 0){
                editor.putFloat(MINUTEBYDEGREE, minuteByDegree);
                editor.commit();
            }

        }
        else {
            Log.d("MinuteByDegee", "Error : " + response.getStatusLine().getStatusCode());
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.token:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"valentindu64@hotmail.fr"});
                i.putExtra(Intent.EXTRA_SUBJECT, "GCM");
                i.putExtra(Intent.EXTRA_TEXT   , getToken());
                startActivity(Intent.createChooser(i, "Envoyer"));
                break;
        }
        return true;
    }

    public String getToken() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString("TokenGCM","");
    }
}