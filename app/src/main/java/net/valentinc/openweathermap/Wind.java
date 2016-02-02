
package net.valentinc.openweathermap;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Wind {

    private Double speed;
    private Integer deg;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The speed
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * 
     * @param speed
     *     The speed
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * 
     * @return
     *     The deg
     */
    public Integer getDeg() {
        return deg;
    }

    /**
     * 
     * @param deg
     *     The deg
     */
    public void setDeg(Integer deg) {
        this.deg = deg;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
