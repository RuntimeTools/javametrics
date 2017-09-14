package com.ibm.javametrics.prometheus;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.JavametricsListener;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

public class Metrics {

    private Counter requests = Counter.build().name("incrementing_count").help("A test count.").register();

    // Gauges for cpu usage.
    private Gauge os_cpu_used_ratio = Gauge.build().name("os_cpu_used_ratio")
            .help("The ratio of the systems CPU that is currently used (values are 0-1)").register();
    private Gauge process_cpu_used_ratio = Gauge.build().name("process_cpu_used_ratio")
            .help("The ratio of the process CPU that is currently used (values are 0-1)").register();

    private Summary http_request_duration_microseconds = Summary.build()
            .quantile(0.5, 0.05)
            .quantile(0.9, 0.01)
            .quantile(0.99, 0.001)
            .name("http_request_duration_microseconds")
            .help("The HTTP request latencies in microseconds.")
            .labelNames("handler").register();

    // Gauges for memory usage - TODO as native memory usage is not in
    // JavaMetrics.
    /*
    static final Gauge os_resident_memory_bytes = Gauge.build().name("os_resident_memory_bytes")
            .help("OS memory size in bytes.").register();
    static final Gauge process_resident_memory_bytes = Gauge.build().name("process_resident_memory_bytes")
            .help("Resident memory size in bytes.").register();
    static final Gauge process_virtual_memory_bytes = Gauge.build().name("process_virtual_memory_bytes")
            .help("Virtual memory size in bytes.").register();
    */

    // HTTP Counters
    private Counter http_requests_total = Counter.build().labelNames("code", "handler", "method")
            .name("http_requests_total").help("Total number of HTTP requests made.").register();

    private JavametricsListener listener = this::parseData;

    public Metrics() {

        // Connect to javametrics
        Javametrics.getInstance().addListener(listener);
    }

    public void deRegister() {
        Javametrics.getInstance().removeListener(listener);
    }

    private void parseData(String pluginName, String jsonData) {
        if (pluginName.equals("api")) {
            List<String> split = splitIntoJSONObjects(jsonData);
            for (Iterator<String> iterator = split.iterator(); iterator.hasNext();) {
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
                        JsonString system = jsonObject.getJsonObject("payload").getJsonString("system");
                        JsonString process = jsonObject.getJsonObject("payload").getJsonString("process");

                        os_cpu_used_ratio.set(Double.valueOf(system.getString()));
                        process_cpu_used_ratio.set(Double.valueOf(process.getString()));
                        break;
                    default:
                        break;
                    }
                } catch (JsonException je) {
                    // Skip this object, log the exception and keep trying with
                    // the rest of the list
                    je.printStackTrace();
                }
            }
        }
    }

    private void handleHttpTopic(JsonObject jsonObject) {
        String urlStr = jsonObject.getJsonString("url").getString();
        String method = jsonObject.getJsonString("method").getString();
        String status = jsonObject.getJsonNumber("status").toString();
        long duration = jsonObject.getJsonNumber("duration").longValue();
        // duration needs to be in microseconds.
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

    private static List<String> splitIntoJSONObjects(String data) {
        List<String> strings = new ArrayList<String>();
        int index = 0;
        // Find first opening bracket
        while (index < data.length() && data.charAt(index) != '{') {
            index++;
        }
        int closingBracket = index + 1;
        int bracketCounter = 1;
        while (index < data.length() - 1 && closingBracket < data.length()) {
            // Find the matching bracket for the bracket at location 'index'
            boolean found = false;
            if (data.charAt(closingBracket) == '{') {
                bracketCounter++;
            } else if (data.charAt(closingBracket) == '}') {
                bracketCounter--;
                if (bracketCounter == 0) {
                    // found matching bracket
                    found = true;
                }
            }
            if (found) {
                strings.add(data.substring(index, closingBracket + 1));
                index = closingBracket + 1;
                // Find next opening bracket and reset counters
                while (index < data.length() && data.charAt(index) != '{') {
                    index++;
                }
                closingBracket = index + 1;
                bracketCounter = 1;
            } else {
                closingBracket++;
            }
        }
        return strings;
    }
}
