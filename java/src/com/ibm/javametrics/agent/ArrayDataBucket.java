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

public class ArrayDataBucket implements Bucket {

    private ArrayList<String> bucket;
    int size = 0;
    int cursor = 0;
    private int maxBucketSize;

    public ArrayDataBucket(int maxBucketSize) {
        this.maxBucketSize = maxBucketSize;
        bucket = new ArrayList<>(100);
    }

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
    public boolean addData(String data) {
        synchronized (bucket) {

            int newSize = size + data.length();
            /*
             * Spill data if necessary
             */
            while (newSize > maxBucketSize && cursor > 0) {
                String removed = bucket.remove(0);
                cursor--;
                newSize -= removed.length();
            }

            if (newSize > maxBucketSize) {
                return false;
            }

            bucket.add(data);
            size = newSize;
            return true;
        }
    }

    @Override
    public String getNext() {
        String data = null;
        synchronized (bucket) {
            if (cursor < bucket.size()) {
                data = bucket.get(cursor);
                cursor++;
            }
        }
        return data;
    }
}
