/*******************************************************************************
 * Copyright 2018 IBM Corp.
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
package com.ibm.javametrics.spring.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.ibm.javametrics.Javametrics;
import com.ibm.javametrics.analysis.MetricsData;
import com.ibm.javametrics.analysis.MetricsProcessor;

@RestController
@RequestMapping("/javametrics/api/v1")
class JavametricsRestController {

    private static boolean initialized = false;

    /*
     * Initialize the Metrics Processor
     */
    private void init() {
        initialized = true;
        Javametrics.getInstance().addListener(MetricsProcessor.getInstance());
    }

    MetricsProcessor mp = MetricsProcessor.getInstance();

    @RequestMapping(produces = "application/json", path = "/collections", method = RequestMethod.GET)
    public ResponseEntity<?> getCollections() {
        Integer[] contextIds = mp.getContextIds();

        StringBuilder sb = new StringBuilder("{\"collectionUris\":[");
        boolean comma = false;
        for (Integer contextId : contextIds) {
            if (comma) {
                sb.append(',');
            }
            sb.append("\"collections/");
            sb.append(contextId);
            sb.append('\"');
            comma = true;
        }
        sb.append("]}");

        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    @RequestMapping(produces = "application/json", path = "/collections", method = RequestMethod.POST)
    public ResponseEntity<?> createCollection() {
        if (!initialized) {
            init();
        }

        if (mp.getContextIds().length > 9) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        int contextId = mp.addContext();

        UriComponents uriComponents = UriComponentsBuilder.fromPath("collections/{id}").buildAndExpand(contextId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        String json = new String("{\"uri\":\"" + uriComponents.getPath() + "\"}");
        return new ResponseEntity<>(json, headers, HttpStatus.CREATED);
    }

    @RequestMapping(produces = "application/json", path = "/collections/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getCollection(@PathVariable("id") int id) {
        MetricsData metrics = mp.getMetricsData(id);
        if (metrics == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(metrics.toJson(id), HttpStatus.OK);
    }

    @RequestMapping(produces = "application/json", path = "/collections/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> resetCollection(@PathVariable("id") int id) {
        if (!mp.resetMetricsData(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(produces = "application/json", path = "/collections/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCollection(@PathVariable("id") int id) {
        if (!mp.removeContext(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(produces = "application/json", path = "/environment", method = RequestMethod.GET)
    public ResponseEntity<?> getEnvironment() {
        return new ResponseEntity<>("{\"environment\":" + mp.getEnvironment() + "}", HttpStatus.OK);
    }
}