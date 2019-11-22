/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.ibm.javametrics.web;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.ApiDataListener;
import com.ibm.javametrics.client.HttpDataAggregator;
import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

/**
 * Registers as a JavametricsListener to receive metrics data, processes the
 * data and sends the output to any registered emitters
 *
 */
public class DataHandler extends ApiDataListener {

    private static DataHandler instance = null;
    private Set<Emitter> emitters = new HashSet<Emitter>();

    private HttpDataAggregator aggregateHttpData;

    private static DataHandler getInstance() {
        if (instance == null) {
            instance = new DataHandler();
        }
        return instance;
    }

    protected DataHandler() {
        this.aggregateHttpData = new HttpDataAggregator();
    }

    public void addEmitter(Emitter emitter) {
        emitters.add(emitter);
        /*
         * adding as listener has the side effect of sending history which is
         * required for any newly registered emitter to see the Environment data
         */
        Javametrics.getInstance().addListener(this);
    }

    public static void registerEmitter(Emitter emitter) {
        getInstance().addEmitter(emitter);
    }

    public void removeEmitter(Emitter emitter) {
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            Javametrics.getInstance().removeListener(this);
        }
    }

    public static void deregisterEmitter(Emitter emitter) {
        getInstance().removeEmitter(emitter);
    }

    public void emit(String message) {
        emitters.forEach((emitter) -> {
            emitter.emit(message);
        });
    }

    @Override
    public void processData(List<String> jsonData) {
        for (Iterator<String> iterator = jsonData.iterator(); iterator.hasNext();) {
            String jsonStr = iterator.next();
            JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
            try {
                JsonObject jsonObject = jsonReader.readObject();
                String topicName = jsonObject.getString("topic", null);
                if (topicName != null) {
                    if (topicName.equals("http")) {
                        JsonObject payload = jsonObject.getJsonObject("payload");
                        long requestTime = payload.getJsonNumber("time").longValue();
                        long requestDuration = payload.getJsonNumber("duration").longValue();
                        String requestUrl = payload.getString("url", "");
                        String requestMethod = payload.getString("method", "");

                        synchronized (aggregateHttpData) {
                            aggregateHttpData.aggregate(requestTime, requestDuration, requestUrl, requestMethod);
                        }
                    } else {
                        emit(jsonObject.toString());
                    }
                }
            } catch (JsonException je) {
                // Skip this object, log the exception and keep trying with
                // the rest of the list
                je.printStackTrace();
            }
        }
        emitHttp();
    }

    private void emitHttp() {
        long time;
        long total;
        long longest;
        double average;
        String url;
        String method;
        StringBuilder httpUrlData;
        StringBuilder httpData;

        synchronized (aggregateHttpData) {
            time = aggregateHttpData.getTime();
            total = aggregateHttpData.getTotalHits();
            longest = aggregateHttpData.getLongest();
            average = aggregateHttpData.getAverage();
            url = aggregateHttpData.getLongestUrl();
            method = aggregateHttpData.getLongestMethod();

            if (total == 0) {
                time = System.currentTimeMillis();
            }

            // emit JSON String representing HTTP request data in the
            // format expected by the javascript
            httpData = new StringBuilder("{\"topic\":\"http\",\"payload\":{\"time\":");
            httpData.append(time);
            httpData.append(",\"total\":");
            httpData.append(total);
            httpData.append(",\"longest\":");
            httpData.append(longest);
            httpData.append(",\"average\":");
            httpData.append(average);
            httpData.append(",\"url\":\"");
            httpData.append(url);
            httpData.append("\",\"method\":\"");
            httpData.append(method);
            httpData.append("\"}}");

            // emit JSON String representing HTTP request data by URL in the
            // format expected by the javascript
            httpUrlData = new StringBuilder("{\"topic\":\"httpURLs\",\"payload\":[");
            Iterator<HttpUrlData> it = aggregateHttpData.getUrlData().iterator();
            while (it.hasNext()) {
                HttpUrlData hud = it.next();
                httpUrlData.append("{\"url\":\"");
                httpUrlData.append(hud.getMethod());
                httpUrlData.append(" ");
                httpUrlData.append(hud.getUrl());
                httpUrlData.append("\",\"hits\":");
                httpUrlData.append(hud.getHits());
                httpUrlData.append(",\"longestResponseTime\":");
                httpUrlData.append(hud.getLongestResponseTime());
                httpUrlData.append(",\"averageResponseTime\":");
                httpUrlData.append(hud.getAverageResponseTime());
                httpUrlData.append('}');
                if (it.hasNext()) {
                    httpUrlData.append(',');
                }
            }
            httpUrlData.append("]}");

            aggregateHttpData.resetSummaryData();
        }

        emit(httpData.toString());
        emit(httpUrlData.toString());
    }

}
