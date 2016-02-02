package net.valentinc.openweathermap;

import org.codehaus.jackson.map.JsonMap***REMOVED***ngException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by ValentinC on 27/01/2016.
 */
public abstract class JsonParser {
private static String json = "{\"coord\":{\"lon\":-0.42,\"lat\":43.33},\"weather\":[{\"id\":701,\"main\":\"Mist\",\"description\":\"brume\",\"icon\":\"50n\"}],\"base\":\"cmc stations\",\"main\":{\"temp\":8.69,\"pressure\":1024,\"humidity\":93,\"temp_min\":6,\"temp_max\":11},\"wind\":{\"speed\":2.1,\"deg\":80},\"clouds\":{\"all\":90},\"dt\":1453926600,\"sys\":{\"type\":1,\"id\":5533,\"message\":0.0038,\"country\":\"FR\",\"sunrise\":1453879340,\"sunset\":1453914428},\"id\":3001617,\"name\":\"Lescar\",\"cod\":200}";

    public static Openweathermap jsonToPOJO() throws IOException, JsonMap***REMOVED***ngException {
        Openweathermap o;
        ObjectMapper mapper = new ObjectMapper();
        o = mapper.readValue(json,Openweathermap.class);
        return o;
    }
}
