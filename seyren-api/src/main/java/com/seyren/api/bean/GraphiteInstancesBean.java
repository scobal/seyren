/* 
 * Copyright (c) 2013 Expedia, Inc. All rights reserved.
 */
package com.seyren.api.bean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import com.seyren.api.jaxrs.GraphiteInstancesResource;
import com.seyren.core.domain.GraphiteInstance;
import com.seyren.core.domain.SeyrenResponse;
import com.seyren.core.store.GraphiteInstancesStore;

/**
 * @author Willie Wheeler (wwheeler@expedia.com)
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
		SeyrenResponse<GraphiteInstance> insts = graphiteInstancesStore.getGraphiteInstances();
		return Response.ok(insts).build();
	}
}
