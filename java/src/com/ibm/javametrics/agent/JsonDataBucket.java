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

import java.util.ArrayList;

public class JsonDataBucket implements Bucket {

    private static final int MAX_SIZE = 4 * 1024;
    private ArrayList<String> bucket = new ArrayList<String>(100);
    int size = 0;
    int cursor = 0;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void empty() {
        synchronized (bucket) {
            bucket.clear();
            cursor = 0;
            size = 0;
        }
    }

    @Override
    public void pushData(String data) {
        synchronized (bucket) {
            bucket.add(data);
            size += data.length();

            System.err.println("pushed " + cursor + " " + bucket.size() + " " + size);
            while (size > MAX_SIZE && cursor > 0) {
                String removed = bucket.remove(0);
                cursor--;
                size -= removed.length();
                System.err.println("trimmed " + cursor + " " + bucket.size() + " " + size);
            }
        }
    }

    @Override
    public String getNext() {
        String data = null;
        synchronized (bucket) {
            if (cursor < bucket.size()) {
                data = bucket.get(cursor);
                cursor++;
                System.err.println("returned " + cursor + " " + bucket.size() + " " + size);
            }
        }
        return data;
    }
}
