package erg.com.nioshheatindex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParsedDataSetWeather {
    private final List<String> locations = new ArrayList<>();
    private final List<String> temperatures = new ArrayList<>();
    private final List<String> humidities = new ArrayList<>();
    private final List<String> times = new ArrayList<>();

    public void addlocation(String value) {
        locations.add(value);
    }

    public void addtemperature(String value) {
        temperatures.add(value);
    }

    public void addhumidity(String value) {
        humidities.add(value);
    }

    public List<String> getlocation() {
        return Collections.unmodifiableList(locations);
    }

    public List<String> gettemperature() {
        return Collections.unmodifiableList(temperatures);
    }

    public List<String> gethumidity() {
        return Collections.unmodifiableList(humidities);
    }

    public void addmaxtime(String value) {
        times.add(value);
    }

    public List<String> getmaxtime() {
        return Collections.unmodifiableList(times);
    }

    @Override
    public String toString() {
        return temperatures + " " + humidities;
    }
}
