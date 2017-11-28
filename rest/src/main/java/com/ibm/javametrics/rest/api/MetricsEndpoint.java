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
import java.util.Iterator;
import java.util.Map.Entry;

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

import com.ibm.javametrics.analysis.MetricsData;
import com.ibm.javametrics.analysis.MetricsProcessor;
import com.ibm.javametrics.client.HttpDataAggregator.HttpUrlData;

@Path("contexts")
public class MetricsEndpoint {

    MetricsProcessor mp = MetricsProcessor.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContexts(@Context UriInfo uriInfo) {
        Integer[] contextIds = mp.getContextIds();

        StringBuilder sb = new StringBuilder("{\"contextUris\":[");
        boolean comma = false;
        for (Integer contextId : contextIds) {
            if (comma) {
                sb.append(',');
            }
            sb.append('\"');
            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Integer.toString(contextId));
            URI uri = builder.build();
            sb.append(uri.toString());
            sb.append('\"');
            comma = true;
        }
        sb.append("]}");
        return Response.ok(sb.toString()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response newContext(@Context UriInfo uriInfo) {
        int contextId = mp.addContext();
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Integer.toString(contextId));
        URI uri = builder.build();
        return Response.created(uri).entity("{\"uri\":\"" + builder.build() + "\"}").build();
    }

    @Path("/{metricsId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics(@PathParam("metricsId") int id) {

        MetricsData metrics = mp.getMetricsData(id);
        if (metrics == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(metricsDataToJson(id, metrics)).build();
    }

    @Path("/{metricsId}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetContext(@PathParam("metricsId") int id) {

        MetricsData metrics = mp.resetMetricsData(id);
        ;
        if (metrics == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(metricsDataToJson(id, metrics)).build();
    }

    @Path("/{metricsId}")
    @DELETE
    public Response deleteContext(@PathParam("metricsId") int id) {

        if (id == 0) {
            return Response.status(Status.FORBIDDEN).build();
        }

        MetricsData metrics = mp.getMetricsData(id);
        if (metrics == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        mp.removeContext(id);

        return Response.ok().build();
    }

    private String metricsDataToJson(int contextId, MetricsData metrics) {

        StringBuilder metricsJson = new StringBuilder("{\"id\":\"");
        metricsJson.append(contextId);
        metricsJson.append("\",\"startTime\":");
        metricsJson.append(metrics.getStartTime());
        metricsJson.append(",\"endTime\":");
        metricsJson.append(metrics.getEndTime());
        metricsJson.append(",\"duration\":");
        metricsJson.append(metrics.getEndTime() - metrics.getStartTime());

        metricsJson.append(",\"cpu\":{\"systemMean\":");
        metricsJson.append(metrics.getCpuSystemMean());
        metricsJson.append(",\"systemPeak\":");
        metricsJson.append(metrics.getCpuSystemPeak());
        metricsJson.append(",\"processMean\":");
        metricsJson.append(metrics.getCpuProcessMean());
        metricsJson.append(",\"processPeak\":");
        metricsJson.append(metrics.getCpuProcessPeak());

        metricsJson.append("},\"gc\":{\"gcTime\":");
        metricsJson.append(metrics.getGcTime());
        metricsJson.append("},\"memory\":{\"usedHeapAfterGCPeak\":");
        metricsJson.append(metrics.getUsedHeapAfterGCPeak());
        metricsJson.append(",\"usedNativePeak\":");
        metricsJson.append(metrics.getUsedNativePeak());

        metricsJson.append("},\"httpUrls\":[");
        Iterator<Entry<String, HttpUrlData>> it = metrics.getUrlData().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HttpUrlData> pair = it.next();
            metricsJson.append("{\"url\":\"");
            metricsJson.append(pair.getKey());
            HttpUrlData hud = pair.getValue();
            metricsJson.append("\",\"hits\":");
            metricsJson.append(hud.getHits());
            metricsJson.append(",\"averageResponseTime\":");
            metricsJson.append(hud.getAverageResponseTime());
            metricsJson.append(",\"longestResponseTime\":");
            metricsJson.append(hud.getLongestResponseTime());
            metricsJson.append('}');
            if (it.hasNext()) {
                {
                    metricsJson.append(',');
                }

            }
        }
        metricsJson.append("]}");

        return metricsJson.toString();
    }
}
