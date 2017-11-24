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
    static int contextCount = 0;

    private MetricsProcessor() {
        addContext();
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
    final static Pattern topicPayload = Pattern
            .compile("\\{\"topic\":\"([a-zA-Z]*)\",\"payload\":\\{\"time\":([0-9]*)(.*)}}");

    // cpu payload:
    // "system":0.10776942355889724,"process":0.008531817167556292....}}
    final static private Pattern cpuPayload = Pattern
            .compile(",\"system\":([0-9]\\.[0-9]*(E[+-][0-9]*)?),\"process\":([0-9]\\.[0-9]*(E[+-][0-9]*)?),.*");

    // gc payload:
    // "gcTime":0.123,"gcTimeMetrics":0.037585808468701146
    final static private Pattern gcPayload = Pattern
            .compile(",\"gcTime\":([0-9]\\.[0-9]*(E[+-][0-9]*)?),\"gcTimeMetrics\":.*");

    // memoryPools paylood:
    // "usedHeapAfterGC":40533936,"usedHeap":86093152,"usedNative":86955016,"usedHeapAfterGCMax":40533936,"usedNativeMax":86955016
    final static private Pattern memoryPoolsPayload = Pattern.compile(
            ",\"usedHeapAfterGC\":([0-9]*),\"usedHeap\":([0-9]*),\"usedNative\":([0-9]*),\"usedHeapAfterGCMax\":([0-9]*),\"usedNativeMax\":([0-9]*).*");

    // HTTP payload:
    // "duration":150,"url":"http://blah/example","method":"GET","status":200,"contentType":"null","header":{},"requestHeader":{}
    final static private Pattern httpPayload = Pattern
            .compile(",\"duration\":([0-9]*),\"url\":\"(.*)\",\"method\":\".*");

    @Override
    public void processData(List<String> jsonData) {

        for (Iterator<String> iterator = jsonData.iterator(); iterator.hasNext();) {
            String jsonStr = iterator.next();

            String topic;
            long timeStamp = 0;
            String payload;

            Matcher matcher = topicPayload.matcher(jsonStr);
            if (matcher.find()) {
                if (matcher.groupCount() == 3) {
                    topic = matcher.group(1);
                    timeStamp = Long.parseLong(matcher.group(2));
                    payload = matcher.group(3);
                    switch (topic) {
                    case "http":
                        aggregateHttpData(timeStamp, payload);
                        break;
                    case "gc":
                        aggregateGCData(timeStamp, payload);
                        break;
                    case "cpu":
                        aggregateCPUData(timeStamp, payload);
                        break;
                    case "memoryPools":
                        aggregateMemoryPoolsData(timeStamp, payload);
                        break;
                    case "env":
                        aggregateMemoryEnvData(timeStamp, payload);
                        break;
                    default:
                        break;
                    }

                }
            }

        }

    }

    private void aggregateMemoryEnvData(long timeStamp, String payload) {
        // TODO Auto-generated method stub

    }

    private void aggregateCPUData(long timeStamp, String payload) {
        Matcher matcher = cpuPayload.matcher(payload);
        double system;
        double process;
        if (matcher.find()) {
            if (matcher.groupCount() == 4) {
                system = Double.parseDouble(matcher.group(1));
                process = Double.parseDouble(matcher.group(3));
                Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Integer, MetricsContext> pair = it.next();
                    pair.getValue().aggregateCpu(timeStamp, system, process);
                }
            }
        }
    }

    private void aggregateGCData(long timeStamp, String payload) {
        Matcher matcher = gcPayload.matcher(payload);
        double gcTime;
        // double gcTimeMetrics;
        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                gcTime = Double.parseDouble(matcher.group(1));
                // gcTimeMetrics = Double.parseDouble(matcher.group(3));
                Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Integer, MetricsContext> pair = it.next();
                    pair.getValue().aggregateGc(timeStamp, gcTime);
                }
            }
        }
    }

    private void aggregateMemoryPoolsData(long timeStamp, String payload) {
        Matcher matcher = memoryPoolsPayload.matcher(payload);
        long usedHeapAfterGC;
        long usedHeap;
        long usedNative;
        if (matcher.find()) {
            if (matcher.groupCount() == 5) {
                usedHeapAfterGC = Long.parseLong(matcher.group(1));
                usedHeap = Long.parseLong(matcher.group(2));
                usedNative = Long.parseLong(matcher.group(3));
                Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Integer, MetricsContext> pair = it.next();
                    pair.getValue().aggregateMemoryPools(timeStamp, usedHeapAfterGC, usedHeap, usedNative);
                }
            }
        }
    }

    private void aggregateHttpData(long timeStamp, String payload) {
        Matcher matcher = httpPayload.matcher(payload);
        long duration;
        String url;
        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                duration = Long.parseLong(matcher.group(1));
                url = matcher.group(2);
                Iterator<Entry<Integer, MetricsContext>> it = metricsContexts.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Integer, MetricsContext> pair = it.next();
                    pair.getValue().aggregateHttp(timeStamp, duration, url);
                }
            }
        }
    }

    public void removeContext(int id) {
        metricsContexts.remove(id);
    }

    public MetricsData getMetricsData(int id) {
        MetricsContext context = metricsContexts.get(id);
        if (context != null) {
            return context.getMetricsData();
        }
        return null;
    }

    public MetricsData resetMetricsData(int id) {
        MetricsContext context = metricsContexts.get(id);
        if (context != null) {
            MetricsData md = context.getMetricsData();
            context.reset();
            return md;
        }
        return null;
    }

}
