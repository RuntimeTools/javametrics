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
package com.ibm.javametrics.rest.api;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import com.ibm.javametrics.analysis.MetricsData;
import com.ibm.javametrics.analysis.MetricsProcessor;

@Path("collections")
public class MetricsEndpoint {

    MetricsProcessor mp = MetricsProcessor.getInstance();
    final String METRICSFILE_PREFIX = getTempDir() + "/javametrics-collection";

    // Get a list of running metric collections
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContexts(@Context UriInfo uriInfo) {
        Integer[] contextIds = mp.getContextIds();

        StringBuilder sb = new StringBuilder("{\"collectionUris\":[");
        boolean comma = false;
        for (Integer contextId : contextIds) {
            if (comma) {
                sb.append(',');
            }
            sb.append('\"');
            UriBuilder builder = UriBuilder.fromPath(uriInfo.getPath());
            builder.path(Integer.toString(contextId));
            URI uri = builder.build();
            sb.append(uri.toString());
            sb.append('\"');
            comma = true;
        }
        sb.append("]}");
        return Response.ok(sb.toString()).build();
    }

    // Begin collecting a new set of metrics
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response newContext(@Context UriInfo uriInfo) {
        if (mp.getContextIds().length > 9) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        int contextId = mp.addContext();
        UriBuilder builder = UriBuilder.fromPath(uriInfo.getPath());
        builder.path(Integer.toString(contextId));
        URI uri = builder.build();
        return Response.status(Status.CREATED).header("Location", uri).entity("{\"uri\":\"" + uri + "\"}").build();
    }

    // Return the metrics collected so far by ID {metricsId}
    @Path("/{metricsId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics(@PathParam("metricsId") int id) {

        MetricsData metrics = mp.getMetricsData(id);
        if (metrics == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(metrics.toJson(id)).build();
    }

    // Reset the metrics collected by {metricsId}
    @Path("/{metricsId}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetContext(@PathParam("metricsId") int id) {

        boolean found = mp.resetMetricsData(id);
        if (!found) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    // Delete the collection by {metricsId}
    @Path("/{metricsId}")
    @DELETE
    public Response deleteContext(@PathParam("metricsId") int id) {

        boolean found = mp.removeContext(id);
        if (!found) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    // Start collecting a timed run of metrics for {seconds}.
    // Returns the collection path that includes an ID which can be used to retrieve the summary
    @Path("/{seconds}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response newTimedContext(@Context UriInfo uriInfo, @PathParam("seconds") int seconds) {
        if (mp.getContextIds().length > 9) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        int contextId = mp.addContext();
        String metricsFilePath =  METRICSFILE_PREFIX + String.valueOf(contextId);
        URI uri = getCollectionIDFromURIInfo(uriInfo, contextId);

        // Start timing the collection recording
        Timer timer = new Timer();
        System.out.println("JavaMetrics: Started metrics collection #"+contextId);
        TimerTask collectionTimerTask = createCollectionTimer(timer, metricsFilePath, contextId);
        timer.schedule(collectionTimerTask, seconds * 1000);

        return Response.status(Status.CREATED).header("Location", uri).entity("{\"uri\":\"" + uri + "\"}").build();
    }

    private URI getCollectionIDFromURIInfo(UriInfo uriInfo, int contextId) {
        // remove parameters from url
        String urlPath = uriInfo.getPath();
        UriBuilder builder = UriBuilder.fromPath(urlPath.substring(0,urlPath.indexOf("/")));
        builder.path(Integer.toString(contextId));
        return builder.build();
    }

    // Fetch the recorded stashed metrics and remove the temp file if it exists
    @Path("/{metricsId}/stashed")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStashedMetrics(@PathParam("metricsId") int id) {
        try {
            if (!mp.removeContext(id)) {
                return Response.status(Status.NOT_FOUND).build();
            }
            String metricsFilePath = METRICSFILE_PREFIX+String.valueOf(id);
            File tempFile = new File(metricsFilePath);
            if (!tempFile.exists() || tempFile.isDirectory()) {
                return Response.status(Status.NOT_FOUND).build();
            }
            String data = new String(Files.readAllBytes(Paths.get(metricsFilePath)));
            tempFile.delete();
            return Response.ok(data).build();
		} catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

     // Return the metrics collected so far by ID {metricsId}
     @Path("/features")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response getFeatures(@Context UriInfo uriInfo) {
        JsonObjectBuilder featureList = Json.createObjectBuilder();
        featureList.add("timedMetrics",true);
        return Response.ok(featureList.build()).build();
     }

    private String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }


    private TimerTask createCollectionTimer(Timer timer, String metricsFilePath, int contextId) {
        return new TimerTask() {
            public void run() {
                Thread timerThread = collectionTimerThread(timer,  metricsFilePath,  contextId);
                timerThread.start();
            }
        };
    }

    private Thread collectionTimerThread(Timer timer, String metricsFilePath, int contextId) {
        return new Thread(new Runnable() {
            public void run() {
                try (FileWriter tempFile = new FileWriter(metricsFilePath)) {
                    MetricsData metrics = mp.getMetricsData(contextId);
                    if (metrics == null) {
                        System.out.println("JavaMetrics: Unable to locate the running collection");
                    } else {
                        tempFile.write(metrics.toJson(contextId));
                        System.out.println("JavaMetrics: Stashed collection # "+contextId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                timer.cancel();
            }
        });
    }
}
