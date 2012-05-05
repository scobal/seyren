package com.seyren.core.store;

import java.util.List;

import com.seyren.core.domain.Alert;

public interface AlertsStore {
	
	Alert createAlert(String checkId, Alert alert);
	
	List<Alert> getAlerts(String checkId);
	
}
