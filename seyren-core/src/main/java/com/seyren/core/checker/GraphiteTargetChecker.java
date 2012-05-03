package com.seyren.core.checker;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import com.seyren.core.domain.Alert;
import com.seyren.core.domain.AlertType;
import com.seyren.core.domain.Check;
import com.seyren.core.service.NotificationService;
import com.seyren.core.value.GraphiteConfig;

@Named
public class GraphiteTargetChecker implements TargetChecker {

	private HttpClient client;
	private NotificationService notificationService;
	private final GraphiteConfig graphiteConfig;

	@Inject
	public GraphiteTargetChecker(NotificationService notificationService, GraphiteConfig graphiteConfig) {
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		this.notificationService = notificationService;
		this.graphiteConfig = graphiteConfig;
	}

	@Override
	public Alert check(Check check) throws Exception {

		GetMethod get = new GetMethod(String.format(graphiteConfig.getUri(), new DateTime().getMillis(), check.getTarget()));

		try {

			client.executeMethod(get);
			Float value = getValue(get);

			// Always create an alert
			Alert alert = createAlert(check, value);

			// Is the alert all OK
			if (alert.getFromType() == AlertType.OK && alert.getToType() == AlertType.OK) {
				return null;
			}

			// Only notify if the alert has changed state
			if (alert.getFromType() != alert.getToType()) {
				check.report(alert, notificationService);
			}

			return alert;

		} finally {

			get.releaseConnection();
		}
	}

	private Alert createAlert(Check check, Float value) {
		AlertType currentState = getCurrentState(check);

		if (isAboveErrorThreshold(check, value)) {
			
			return alert(check, value, currentState, AlertType.ERROR);

		} else if (isAboveWarnThreshold(check, value)) {
			
			return alert(check, value, currentState, AlertType.WARN);

		} else {
			
			return alert(check, value, currentState, AlertType.OK);
			
		}
	}

	private AlertType getCurrentState(Check check) {
		AlertType fromType = AlertType.OK;
		if (check.getAlerts().size() > 0) {
			fromType = check.getAlerts().get(check.getAlerts().size() - 1).getToType();
		}
		return fromType;
	}

	private Float getValue(GetMethod get) throws Exception {
		JsonNode tree = new ObjectMapper().readTree(get.getResponseBodyAsString());
		JsonNode points = tree.get(0).get("datapoints");

		// Loop through the datapoints in reverse order until we find the latest non-null value
		for (int i = points.size() - 1; i >= 0; i--) {
			String value = points.get(i).get(0).asText();
			if (!value.equals("null")) {
				return Float.valueOf(value);
			}
		}

		throw new Exception("Could not find a valid datapoint for uri: " + get);
	}

	private boolean isAboveWarnThreshold(Check check, Float value) {
		return value >= Float.valueOf(check.getWarn());
	}

	private boolean isAboveErrorThreshold(Check check, Float value) {
		return value >= Float.valueOf(check.getError());
	}

	private Alert alert(Check check, Float value, AlertType from, AlertType to) {
		return new Alert()
				.withValue(value.toString())
				.withWarn(check.getWarn())
				.withError(check.getError())
				.withFromType(from)
				.withToType(to)
				.withTimestamp(new DateTime());
	}

}
