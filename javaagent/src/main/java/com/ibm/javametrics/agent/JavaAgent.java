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
package com.ibm.javametrics.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ibm.javametrics.impl.JavametricsImpl;

public class JavaAgent implements Agent {

    private static final int MAX_BUCKET_SIZE = 2 * 1024 * 1024;
    private Map<String, Bucket> buckets = new HashMap<String, Bucket>();
    private Set<Receiver> receivers = new HashSet<Receiver>();
    private int collectionInterval = 2;
    private ScheduledExecutorService exec;

    public JavaAgent(int interval) {
        collectionInterval = interval;
        init();
    }

    public JavaAgent() {
        init();
    }

    private void init() {
        exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(this::drain, collectionInterval, collectionInterval, TimeUnit.SECONDS);
    }

    public void pushData(String type, String data) {
        synchronized (buckets) {
            Bucket bucket = buckets.get(type);
            if (bucket == null) {
                bucket = new StringDataBucket(MAX_BUCKET_SIZE);
                buckets.put(type, bucket);
            }
            if (!bucket.addData(data)) {
                // System.err.println("Javametrics: data dropped. Bucket size="
                // + bucket.getSize());
            }
        }
    }

    private void drain() {
        synchronized (buckets) {
            buckets.forEach((name, bucket) -> {
                drainBatched(name, bucket);
            });
        }
    }

    @SuppressWarnings("unused")
    private void drain(String type, Bucket bucket) {
        String data = bucket.getNext();
        while (data != null) {
            emit(type, data);
            data = bucket.getNext();
        }
    }

    private void drainBatched(String type, Bucket bucket) {
        StringBuffer sb = new StringBuffer();
        String data = bucket.getNext();
        while (data != null) {
            sb.append(data);
            data = bucket.getNext();
        }
        if (sb.length() > 0) {
            emit(type, sb.toString());
        }
    }

    private void emit(String type, String data) {
        if (data != null) {
            receivers.forEach((receiver) -> {
                receiver.receiveData(type, data);
            });
        }
    }

    @Override
    public void command(String command, String... params) {
        if (command.equals(JavametricsImpl.HISTORY_MESSAGE)) {
            // TODO call buckets to get history and persistent data
        }
    }

    @Override
    public void registerReceiver(Receiver receiver) {
        synchronized (receivers) {
            receivers.add(receiver);
        }
    }

    @Override
    public void deregisterReceiver(Receiver receiver) {
        synchronized (receivers) {
            receivers.remove(receiver);
        }
    }

}
