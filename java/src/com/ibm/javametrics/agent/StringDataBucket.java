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

public class StringDataBucket implements Bucket {

    private static final int INITIAL_BUCKET_SIZE = 4 * 1024;
    private StringBuffer bucket = new StringBuffer(INITIAL_BUCKET_SIZE);

    private int maxBucketSize;

    public StringDataBucket(int maxBucketSize) {
        this.maxBucketSize = maxBucketSize;
    }

    @Override
    public int getSize() {
        return bucket.length();
    }

    @Override
    public void empty() {
        bucket = new StringBuffer(INITIAL_BUCKET_SIZE);
    }

    @Override
    public boolean addData(String data) {
        if ((getSize() + data.length()) > maxBucketSize) {
            return false;
        }
        bucket.append(data);
        return true;
    }

    @Override
    public String getNext() {
        String data = null;
        if (bucket.length() > 0) {
            data = bucket.toString();
            bucket = new StringBuffer(INITIAL_BUCKET_SIZE);
        }
        return data;
    }
}
