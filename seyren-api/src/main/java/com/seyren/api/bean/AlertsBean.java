package com.seyren.api.bean;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import com.seyren.api.jaxrs.AlertsResource;
import com.seyren.core.domain.Alert;
import com.seyren.core.store.AlertsStore;

@Named
public class AlertsBean implements AlertsResource {

	private AlertsStore alertsStore;

	@Inject
	public AlertsBean(AlertsStore alertsStore) {
		this.alertsStore = alertsStore;
	}
	
	@Override
	public Response getAlerts(String checkId, int start, int items) {
		if (start < 0 || items < 0) {
			return Response.status(400).build();
		}
		List<Alert> alerts = alertsStore.getAlerts(checkId, start, items);
		return Response.ok(alerts).build();
	}

}
