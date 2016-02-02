
package net.valentinc.openweathermap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Main {

    private Double temp;
    private Integer pressure;
    private Integer humidity;
    private Integer tempMin;
    private Integer tempMax;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The temp
     */
    public Double getTemp() {
        return temp;
    }

    /**
     * 
     * @param temp
     *     The temp
     */
    public void setTemp(Double temp) {
        this.temp = temp;
    }

    /**
     * 
     * @return
     *     The pressure
     */
    public Integer getPressure() {
        return pressure;
    }

    /**
     * 
     * @param pressure
     *     The pressure
     */
    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    /**
     * 
     * @return
     *     The humidity
     */
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * 
     * @param humidity
     *     The humidity
     */
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * 
     * @return
     *     The tempMin
     */
    public Integer getTempMin() {
        return tempMin;
    }

    /**
     * 
     * @param tempMin
     *     The temp_min
     */
    public void setTempMin(Integer tempMin) {
        this.tempMin = tempMin;
    }

    /**
     * 
     * @return
     *     The tempMax
     */
    public Integer getTempMax() {
        return tempMax;
    }

    /**
     * 
     * @param tempMax
     *     The temp_max
     */
    public void setTempMax(Integer tempMax) {
        this.tempMax = tempMax;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString(){
        return "";
    }

}
