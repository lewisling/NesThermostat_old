package net.valentinc.openweathermap;

import android.os.AsyncTask;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by ValentinC on 27/01/2016.
 */
public abstract class JsonParser {
    private static String json;
    private static final String APPID = "124db9b65e41932220cdc2392bad7ebf";
    private static String BASE_URL ="http://api.openweathermap.org/data/2.5/weather?q=Lescar,fr&units=metric&lang=fr&APPID="+APPID;

    public static Openweathermap jsonToPOJO() throws IOException, ExecutionException, InterruptedException {
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute().get();

        if(json == null)
            return null;
        Openweathermap o;
        ObjectMapper mapper = new ObjectMapper();
        o = mapper.readValue(json,Openweathermap.class);
        return o;
    }

    public static String getWeatherData() {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            con = (HttpURLConnection) ( new URL(BASE_URL)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }

    private static class JSONWeatherTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            json = getWeatherData();
            return null;
        }
    }
}
