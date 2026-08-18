package erg.com.nioshheatindex;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Loads hourly forecast data from the current NWS API without retaining an Activity on a worker. */
class ProcessXML {
    private static final int TIMEOUT_MS = 15_000;
    private final HeatIndexActivity activity;
    private final double latitude;
    private final double longitude;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Future<?> request;

    ProcessXML(HeatIndexActivity activity, double latitude, double longitude) {
        this.activity = activity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    void execute() {
        request = executor.submit(() -> {
            ParsedDataSetWeather data = null;
            try {
                String pointsUrl = String.format(Locale.US,
                        "https://api.weather.gov/points/%.5f,%.5f", latitude, longitude);
                JSONObject points = getJson(pointsUrl);
                JSONObject pointProperties = points.getJSONObject("properties");
                String hourlyUrl = pointProperties.getString("forecastHourly");

                JSONObject forecast = getJson(hourlyUrl);
                data = parseForecast(forecast, pointProperties);
            } catch (Exception ignored) {
                // The UI receives an empty result and presents the existing outage state.
            }

            ParsedDataSetWeather result = data;
            mainHandler.post(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    activity.callBackData(result);
                }
            });
        });
    }

    void cancel() {
        if (request != null) {
            request.cancel(true);
        }
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private JSONObject getJson(String endpoint) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/geo+json, application/json");
        connection.setRequestProperty("User-Agent", "OSHA NIOSH Heat Safety Tool");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new java.io.IOException("NWS returned HTTP " + status);
            }
            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Weather request cancelled");
                    }
                    body.append(line);
                }
                return new JSONObject(body.toString());
            }
        } finally {
            connection.disconnect();
        }
    }

    private ParsedDataSetWeather parseForecast(JSONObject forecast, JSONObject pointProperties) throws Exception {
        ParsedDataSetWeather data = new ParsedDataSetWeather();
        JSONObject relativeLocation = pointProperties.optJSONObject("relativeLocation");
        if (relativeLocation != null) {
            JSONObject locationProperties = relativeLocation.optJSONObject("properties");
            if (locationProperties != null) {
                String city = locationProperties.optString("city", "");
                String state = locationProperties.optString("state", "");
                if (!city.isEmpty() || !state.isEmpty()) {
                    data.addlocation(city + (city.isEmpty() || state.isEmpty() ? "" : ", ") + state);
                }
            }
        }
        JSONArray periods = forecast.getJSONObject("properties").getJSONArray("periods");
        int count = Math.min(periods.length(), 24);
        for (int i = 0; i < count; i++) {
            JSONObject period = periods.optJSONObject(i);
            if (period == null || period.isNull("temperature")) {
                continue;
            }
            JSONObject humidity = period.optJSONObject("relativeHumidity");
            String startTime = period.optString("startTime", "");
            if (humidity == null || humidity.isNull("value") || startTime.isEmpty()) {
                continue;
            }
            data.addtemperature(String.valueOf(period.optInt("temperature")));
            data.addhumidity(String.valueOf(humidity.optInt("value")));
            data.addmaxtime(startTime);
        }
        return data;
    }
}
