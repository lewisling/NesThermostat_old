package net.valentinc.server;

import android.util.Log;

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
 */
public abstract class Temperature {
    public static float getCurrentTemperature() {
        String res = null;
        InputStream is = null;
        try {
            URL url = new URL("http://valentinchatelard.ddns.net/android/getTemperature.php");
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
                // Convert the InputStream into a string
                res = inputStreamToString(is);
                res = res.substring(0, 4);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (res == null) {
                res = "-1";
            }
        }
        return Float.parseFloat(res);
    }
    public static String inputStreamToString(InputStream is) throws IOException {
        char[] buffer = new char[10];
        Reader reader = new InputStreamReader(is, "UTF-8");
        reader.read(buffer);
        return new String(buffer);
    }
}
