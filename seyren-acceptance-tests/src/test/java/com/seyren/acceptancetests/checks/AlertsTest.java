package com.seyren.acceptancetests.checks;

import static com.github.restdriver.serverdriver.Matchers.*;
import static com.github.restdriver.serverdriver.RestServerDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static com.seyren.acceptancetests.util.SeyrenDriver.*;

import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

public class AlertsTest {

	@Test
	public void testGetAlertsReturnsOk() {
		Response response = get(alerts("1"));
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(0)));
	}
	
}
