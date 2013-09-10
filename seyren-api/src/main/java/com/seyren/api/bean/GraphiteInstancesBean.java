/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.api.bean;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.seyren.api.jaxrs.GraphiteInstancesResource;
import com.seyren.core.domain.GraphiteInstance;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.store.GraphiteInstancesStore;

/**
 * Bean implementing the REST API's Graphite instance endpoints.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@Named
public class GraphiteInstancesBean implements GraphiteInstancesResource {
    private GraphiteInstancesStore graphiteInstancesStore;
    
    @Inject
    public GraphiteInstancesBean(GraphiteInstancesStore graphiteInstancesStore) {
        this.graphiteInstancesStore = graphiteInstancesStore;
    }

    @Override
    public Response getGraphiteInstances() {
        SeyrenResponse<GraphiteInstance> graphiteInstances = graphiteInstancesStore.getGraphiteInstances();
        return Response.ok(graphiteInstances).build();
    }

    @Override
    public Response createGraphiteInstance(GraphiteInstance graphiteInstance) {
        GraphiteInstance stored = graphiteInstancesStore.createGraphiteInstance(graphiteInstance);
        return Response.created(uri(stored.getId())).build();
    }

    @Override
    public Response getGraphiteInstance(String graphiteInstanceId) {
        GraphiteInstance graphiteInstance = graphiteInstancesStore.getGraphiteInstance(graphiteInstanceId);
        if (graphiteInstance == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(graphiteInstance).build();
    }

    @Override
    public Response updateGraphiteInstance(String graphiteInstanceId, GraphiteInstance graphiteInstance) {
        GraphiteInstance stored = graphiteInstancesStore.getGraphiteInstance(graphiteInstanceId);
        if (stored == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        stored = graphiteInstancesStore.updateGraphiteInstance(graphiteInstance);
        return Response.ok(stored).build();
    }

    @Override
    public Response deleteGraphiteInstance(String graphiteInstanceId) {
        graphiteInstancesStore.deleteGraphiteInstance(graphiteInstanceId);
        return Response.noContent().build();
    }
    
    private URI uri(String graphiteInstanceId) {
        try {
            return new URI("graphite-instances/" + graphiteInstanceId);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
}
