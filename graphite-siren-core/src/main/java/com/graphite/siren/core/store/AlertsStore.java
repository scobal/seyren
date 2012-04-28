package com.graphite.siren.core.store;

import com.graphite.siren.core.domain.Alert;

public interface AlertsStore {

	Alert createAlert(String checkId);
	
}
