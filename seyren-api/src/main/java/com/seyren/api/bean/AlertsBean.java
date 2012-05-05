package com.seyren.api.bean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import com.seyren.api.jaxrs.AlertsResource;
import com.seyren.core.store.AlertsStore;

@Named
public class AlertsBean implements AlertsResource {

	private AlertsStore alertsStore;

	@Inject
	public AlertsBean(AlertsStore alertsStore) {
		this.alertsStore = alertsStore;
	}
	
	@Override
	public Response getAlerts(String checkId) {
		return Response.ok(alertsStore.getAlerts(checkId)).build();
	}

}
