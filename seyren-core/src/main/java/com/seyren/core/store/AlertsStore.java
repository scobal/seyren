package com.seyren.core.store;

import com.seyren.core.domain.Alert;

public interface AlertsStore {
	
	Alert createAlert(String checkId, Alert alert);
	
}
