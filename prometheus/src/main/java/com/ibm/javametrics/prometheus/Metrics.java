package com.ibm.javametrics.prometheus;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.ApiDataListener;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.DefaultExports;

public class Metrics {

    // Gauges for cpu usage.
    private Gauge os_cpu_used_ratio = Gauge.build().name("os_cpu_used_ratio")
            .help("The ratio of the systems CPU that is currently used (values are 0-1)").register();
    private Gauge process_cpu_used_ratio = Gauge.build().name("process_cpu_used_ratio")
            .help("The ratio of the process CPU that is currently used (values are 0-1)").register();

    // Summary for http request durations.
    private Summary http_request_duration_microseconds = Summary.build().quantile(0.5, 0.05).quantile(0.9, 0.01)
            .quantile(0.99, 0.001).name("http_request_duration_microseconds")
            .help("The HTTP request latencies in microseconds.").labelNames("handler").register();

    // Counter for http request counts.
    private Counter http_requests_total = Counter.build().labelNames("code", "handler", "method")
            .name("http_requests_total").help("Total number of HTTP requests made.").register();

    private ApiDataListener listener = new ApiDataListener() {

        @Override
        public void processData(List<String> jsaonData) {
            for (Iterator<String> iterator = jsaonData.iterator(); iterator.hasNext();) {
                String jsonStr = iterator.next();
                JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
                try {
                    JsonObject jsonObject = jsonReader.readObject();
                    String topicName = jsonObject.getString("topic", null);
                    switch (topicName) {
                    case "http":
                        handleHttpTopic(jsonObject.getJsonObject("payload"));
                        break;
                    case "cpu":
                        JsonNumber system = jsonObject.getJsonObject("payload").getJsonNumber("system");
                        JsonNumber process = jsonObject.getJsonObject("payload").getJsonNumber("process");

                        os_cpu_used_ratio.set(Double.valueOf(system.doubleValue()));
                        process_cpu_used_ratio.set(Double.valueOf(process.doubleValue()));
                        break;
                    default:
                        break;
                    }
                } catch (JsonException je) {
                    // Skip this object, log the exception and keep trying with
                    // the rest of the list
                    // System.err.println("Error in json: \n" + jsonStr);
                    // je.printStackTrace();
                }
            }
        }
    };

    public Metrics() {
        // Add the default Java collectors
        DefaultExports.initialize();
        // Connect to javametrics
        Javametrics.getInstance().addListener(listener);
    }

    public void deRegister() {
        Javametrics.getInstance().removeListener(listener);
    }

    private void handleHttpTopic(JsonObject jsonObject) {
        String urlStr = jsonObject.getJsonString("url").getString();
        String method = jsonObject.getJsonString("method").getString();
        String status = jsonObject.getJsonNumber("status").toString();
        long duration = jsonObject.getJsonNumber("duration").longValue();
        // Duration needs to be in microseconds.
        duration *= 1000;

        try {
            URL url = new URL(urlStr);
            String handler = url.getPath();

            http_requests_total.labels(status, handler, method).inc();
            http_request_duration_microseconds.labels(handler).observe((double) duration);
        } catch (MalformedURLException e) {
            // Our URLs came from Liberty, they should be valid.
            // e.printStackTrace();
        }
    }
}
