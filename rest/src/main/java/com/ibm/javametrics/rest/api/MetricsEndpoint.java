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

import com.ibm.javametrics.analysis.MetricsData;
import com.ibm.javametrics.analysis.MetricsProcessor;

@Path("collections")
public class MetricsEndpoint {

    MetricsProcessor mp = MetricsProcessor.getInstance();

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

    @Path("/{metricsId}")
    @DELETE
    public Response deleteContext(@PathParam("metricsId") int id) {

        boolean found = mp.removeContext(id);
        if (!found) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

}
