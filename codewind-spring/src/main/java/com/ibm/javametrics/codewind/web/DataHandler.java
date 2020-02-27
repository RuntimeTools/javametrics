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
package com.ibm.javametrics.codewind.web;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonReader;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.client.ApiDataListener;
import com.ibm.javametrics.client.HttpDataAggregator;

/**
 * Registers as a JavametricsListener to receive metrics data, processes the
 * data and sends the output to any registered emitters
 *
 */
// TODO: This is currently copied from dashboard version, we should find a better solution.
public class DataHandler extends ApiDataListener {

    private static DataHandler instance = null;

    // Buffer event data
    private double latestCPUEventProcess = 0.0;
    private double latestCPUEventSystem = 0.0;
    private double latestCPUEventProcessMean = 0.0;
    private double latestCPUEventSystemMean = 0.0;

    private long latestMemEventUsedHeapAfterGC =  0L;
    private long latestMemEventUsedHeapAfterGCMax =  0L;
    private long latestMemEventUsedHeap =  0L;
    private long latestMemEventUsedNative =  0L;
    private long latestMemEventUsedNativeMax =  0L;

    private double latestGCEventGCTime = 0.0;

    private HashMap<String, String> latestEnvMap = new HashMap<String, String>();

    private HttpDataAggregator aggregateHttpData;

    protected static DataHandler getInstance() {
        if (instance == null) {
            instance = new DataHandler();
            /*
             * adding as listener has the side effect of sending history which is
             * required for any newly registered emitter to see the Environment data
             */
            Javametrics.getInstance().addListener(instance);
        }
        return instance;
    }

    protected DataHandler() {
        this.aggregateHttpData = new HttpDataAggregator();
    }

    protected void finalize() {
        Javametrics.getInstance().removeListener(this);
    }

    @Override
    public void processData(List<String> jsonData) {
        for (Iterator<String> iterator = jsonData.iterator(); iterator.hasNext();) {
            String jsonStr = iterator.next();
            JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
            try {
                JsonObject jsonObject = jsonReader.readObject();
                String topicName = jsonObject.getString("topic", null);
                JsonObject payload;
                if (topicName != null) {
                    switch (topicName) {
                      case "http":
                         payload = jsonObject.getJsonObject("payload");
                         long requestTime = payload.getJsonNumber("time").longValue();
                         long requestDuration = payload.getJsonNumber("duration").longValue();
                         String requestUrl = payload.getString("url", "");
                         String requestMethod = payload.getString("method", "");

                         synchronized (aggregateHttpData) {
                             aggregateHttpData.aggregate(requestTime, requestDuration, requestUrl, requestMethod);
                         }
                         break;
                      case "cpu":
                         payload = jsonObject.getJsonObject("payload");
                         latestCPUEventProcess = payload.getJsonNumber("process").doubleValue();
                         latestCPUEventSystem = payload.getJsonNumber("system").doubleValue();
                         latestCPUEventProcessMean = payload.getJsonNumber("processMean").doubleValue();
                         latestCPUEventSystemMean = payload.getJsonNumber("systemMean").doubleValue();
                         break;
                      case "memoryPools":
                         payload = jsonObject.getJsonObject("payload");
                         latestMemEventUsedHeapAfterGC =  payload.getJsonNumber("usedHeapAfterGC").longValue();
                         latestMemEventUsedHeapAfterGCMax =  payload.getJsonNumber("usedHeapAfterGCMax").longValue();
                         latestMemEventUsedHeap =  payload.getJsonNumber("usedHeap").longValue();
                         latestMemEventUsedNative =  payload.getJsonNumber("usedNative").longValue();
                         latestMemEventUsedNativeMax =  payload.getJsonNumber("usedNativeMax").longValue();
                         break;
                      case "gc":
                         payload = jsonObject.getJsonObject("payload");
                         latestGCEventGCTime = payload.getJsonNumber("gcTime").doubleValue();
                         break;
                      case "env":
                         JsonArray envPayload = jsonObject.getJsonArray("payload");
                         for (int i=0; i < envPayload.size(); i++) {
                             JsonObject envar = envPayload.getJsonObject(i);
                             String param = envar.getJsonString("Parameter").getString();
                             String value = envar.getJsonString("Value").getString();
                             latestEnvMap.put(param, value);
                         }
                         break;
                      default:
                        // ignore and move on
                    }
                }
            } catch (JsonException je) {
                // Skip this object, log the exception and keep trying with
                // the rest of the list
                je.printStackTrace();
            }
        }
    }

    public double getLatestCPUEventProcess() {
        return latestCPUEventProcess;
    }
    public double getLatestCPUEventSystem() {
        return latestCPUEventSystem;
    }
    public double getLatestCPUEventProcessMean() {
        return latestCPUEventProcessMean;
    }
    public double getLatestCPUEventSystemMean() {
        return latestCPUEventSystemMean;
    }
    public long getLatestMemEventUsedHeapAfterGC() {
        return latestMemEventUsedHeapAfterGC;
    }
    public long getLatestMemEventUsedHeapAfterGCMax() {
        return latestMemEventUsedHeapAfterGCMax;
    }
    public long getLatestMemEventUsedHeap() {
        return latestMemEventUsedHeap;
    }
    public long getLatestMemEventUsedNative() {
        return latestMemEventUsedNative;
    }
    public long getLatestMemEventUsedNativeMax() {
        return latestMemEventUsedNativeMax;
    }
    public double getLatestGCEventGCTime() {
        return latestGCEventGCTime;
    }
    public HashMap<String, String> getLatestEnvMap() {
        return latestEnvMap;
    }
    public HttpDataAggregator getAggregateHttpData() {
        return aggregateHttpData;
    }

}
