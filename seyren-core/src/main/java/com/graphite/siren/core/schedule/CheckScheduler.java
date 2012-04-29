package com.graphite.siren.core.schedule;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.scheduling.annotation.Scheduled;

import com.graphite.siren.core.checker.TargetChecker;
import com.graphite.siren.core.domain.Alert;
import com.graphite.siren.core.domain.Check;
import com.graphite.siren.core.store.AlertsStore;
import com.graphite.siren.core.store.ChecksStore;

@Named
public class CheckScheduler {

	private ChecksStore checksStore;
	private AlertsStore alertsStore;
	private TargetChecker checker;

	@Inject
	public CheckScheduler(ChecksStore checksStore, AlertsStore alertsStore, TargetChecker checker) {
		this.checksStore = checksStore;
		this.alertsStore = alertsStore;
		this.checker = checker;
	}
	
	@Scheduled(fixedRate = 10000)
	public void performChecks() {
		List<Check> checks = checksStore.getChecks();
		for (Check check : checks) {
			if (check.isEnabled()) {
				try {
					Alert alert = checker.check(check);
					if (alert != null) {
						alertsStore.createAlert(check.getId(), alert);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// (optional) notify subscribers
		}
		// TODO: multithread this bad boy
	}
	
}
