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
package com.ibm.javametrics.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.javametrics.client.ApiDataListener;

public class MetricsProcessor extends ApiDataListener {

    private Map<Integer, MetricsContext> metricsContexts = new HashMap<Integer, MetricsContext>();
    private String environment = "[]";
    static int contextCount = 0;

    private MetricsProcessor() {
    }

    public int addContext() {
        metricsContexts.put(contextCount, new MetricsContext());
        return contextCount++;
    }

    private static MetricsProcessor instance = new MetricsProcessor();

    public static MetricsProcessor getInstance() {
        return instance;
    }

    // {"topic":"topicname","payload":{"time":1510316403108....}}
    final static Pattern topicPayload = Pattern.compile("\\{\"topic\":\"([a-zA-Z]*)\",\"payload\":(.*)}");

    final static Pattern timePattern = Pattern
            .compile("\\{\"topic\":\"([a-zA-Z]*)\",\"payload\":\\{\"time\":([0-9]*)(.*)}}");

    // cpu payload:
    // "system":0.10776942355889724,"process":0.008531817167556292....}}
    final static private Pattern cpuPayload = Pattern.compile(
            "\\{\"time\":([0-9]*),\"system\":([0-9]\\.[0-9]*(E[+-][0-9]*)?),\"process\":([0-9]\\.[0-9]*(E[+-][0-9]*)?),.*");

    // gc payload:
    // "gcTime":0.123
    final static private Pattern gcPayload = Pattern
            .compile("\\{\"time\":([0-9]*),\"gcTime\":([0-9]\\.[0-9]*(E[+-][0-9]*)?)");

    // memoryPools paylood:
    // "usedHeapAfterGC":40533936,"usedHeap":86093152,"usedNative":86955016,"usedHeapAfterGCMax":40533936,"usedNativeMax":86955016
    final static private Pattern memoryPoolsPayload = Pattern.compile(
            "\\{\"time\":([0-9]*),\"usedHeapAfterGC\":([0-9]*),\"usedHeap\":([0-9]*),\"usedNative\":([0-9]*),\"usedHeapAfterGCMax\":([0-9]*),\"usedNativeMax\":([0-9]*).*");

    // HTTP payload:
    // "duration":150,"url":"http://blah/example","method":"GET","status":200,"contentType":"null","header":{},"requestHeader":{}
    final static private Pattern httpPayload = Pattern
          .compile("\\{\"time\":([0-9]*),\"duration\":([0-9]*),\"url\":\"(.*)\",\"method\":\"(.*)\",\"status\".*");

    @Override
    public void processData(List<String> jsonData) {

        for (Iterator<String> iterator = jsonData.iterator(); iterator.hasNext();) {
            String jsonStr = iterator.next();

            String topic;
            String payload;

            Matcher matcher = topicPayload.matcher(jsonStr);
            if ((matcher.find()) && (matcher.groupCount() == 2)) {
                topic = matcher.group(1);
                payload = matcher.group(2);
                switch (topic) {
                case "http":
                    aggregateHttpData(payload);
                    break;
                case "gc":
                    aggregateGCData(payload);
                    break;
                case "cpu":
                    aggregateCPUData(payload);
                    break;
                case "memoryPools":
                    aggregateMemoryPoolsData(payload);
                    break;
                case "env":
                    aggregateEnvData(payload);
                    break;
                default:
                    break;
                }
            }

        }

    }

    private void aggregateEnvData(String payload) {
        environment = payload;
    }

    private void aggregateCPUData(String payload) {

        Matcher matcher = cpuPayload.matcher(payload);
        long timeStamp;
        double system;
        double process;
        if ((matcher.find()) && (matcher.groupCount() == 5)) {
            timeStamp = Long.parseLong(matcher.group(1));
            system = Double.parseDouble(matcher.group(2));
            process = Double.parseDouble(matcher.group(4));
            Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, MetricsContext> pair = it.next();
                pair.getValue().aggregateCpu(timeStamp, system, process);
            }
        }
    }

    private void aggregateGCData(String payload) {
        Matcher matcher = gcPayload.matcher(payload);
        long timeStamp = 0;
        double gcTime;
        // double gcTimeMetrics;
        if ((matcher.find()) && (matcher.groupCount() == 3)) {
            timeStamp = Long.parseLong(matcher.group(1));
            gcTime = Double.parseDouble(matcher.group(2));
            // gcTimeMetrics = Double.parseDouble(matcher.group(4);
            Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, MetricsContext> pair = it.next();
                pair.getValue().aggregateGc(timeStamp, gcTime);
            }
        }
    }

    private void aggregateMemoryPoolsData(String payload) {
        Matcher matcher = memoryPoolsPayload.matcher(payload);
        long timeStamp = 0;
        long usedHeapAfterGC;
        long usedHeap;
        long usedNative;
        if ((matcher.find()) && (matcher.groupCount() == 6)) {
            timeStamp = Long.parseLong(matcher.group(1));
            usedHeapAfterGC = Long.parseLong(matcher.group(2));
            usedHeap = Long.parseLong(matcher.group(3));
            usedNative = Long.parseLong(matcher.group(4));
            Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, MetricsContext> pair = it.next();
                pair.getValue().aggregateMemoryPools(timeStamp, usedHeapAfterGC, usedHeap, usedNative);
            }
        }
    }

    private void aggregateHttpData(String payload) {
        Matcher matcher = httpPayload.matcher(payload);
        long timeStamp = 0;
        long duration;
        String url;
        String method;
        if ((matcher.find()) && (matcher.groupCount() == 4)) {
            timeStamp = Long.parseLong(matcher.group(1));
            duration = Long.parseLong(matcher.group(2));
            url = matcher.group(3);
            method = matcher.group(4);
            Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, MetricsContext> pair = it.next();
                pair.getValue().aggregateHttp(timeStamp, duration, url, method);
            }
        }
    }

    public boolean removeContext(int id) {
        return (metricsContexts.remove(id) != null);
    }

    public MetricsData getMetricsData(int id) {
        MetricsContext context = metricsContexts.get(id);
        if (context != null) {
            return context.getMetricsData();
        }
        return null;
    }

    public boolean resetMetricsData(int id) {
        MetricsContext context = metricsContexts.get(id);
        if (context != null) {
            context.reset();
            return true;
        }
        return false;
    }

    public Integer[] getContextIds() {
        return metricsContexts.keySet().toArray(new Integer[0]);
    }

    public String getEnvironment() {
        return environment;
    }

}
