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
			
			if (isAboveErrorThreshold(check, value)) {
                Alert alert = createAlert(check, value, AlertType.ERROR);
                check.report(alert, notificationService);

                return alert;
			}
			if (isAboveWarnThreshold(check, value)) {
				return createAlert(check, value, AlertType.WARN);
			}
			
			return null;
		} finally {
			get.releaseConnection();
		}
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

	private Alert createAlert(Check check, Float value, AlertType type) {
		return new Alert()
			.withValue(value.toString())
			.withWarn(check.getWarn())
			.withError(check.getError())
			.withFromType(AlertType.UNKNOWN)
			.withToType(type)
			.withTimestamp(new DateTime());
	}

}
