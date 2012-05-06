package com.seyren.core.schedule;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.scheduling.annotation.Scheduled;

import com.seyren.core.checker.TargetChecker;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.store.AlertsStore;
import com.seyren.core.store.ChecksStore;

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
						check.setState(alert.getToType());
						checksStore.saveCheck(check);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
