package com.graphite.siren.core.schedule;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.scheduling.annotation.Scheduled;

import com.graphite.siren.core.domain.Check;
import com.graphite.siren.core.store.AlertsStore;
import com.graphite.siren.core.store.ChecksStore;

@Named
public class CheckScheduler {

	private ChecksStore checksStore;
	private AlertsStore alertsStore;

	@Inject
	public CheckScheduler(ChecksStore checksStore, AlertsStore alertsStore) {
		this.checksStore = checksStore;
		this.alertsStore = alertsStore;
		System.out.println("cons");
	}
	
//	@Scheduled(fixedRate = 10000)
	public void performChecks() {
		List<Check> checks = checksStore.getChecks();
		// TODO: multithread this bad boy
		
		// For each check
		// Hit graphite with check.getTarget
		// If no alert needed then move on
		// If an alert is needed then create one
		// (optional) notify subscribers
		
//		alertsStore.createAlert("", null);
	}
	
}
