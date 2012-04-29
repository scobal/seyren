package com.seyren.acceptancetests.checks;

import static com.github.restdriver.serverdriver.Matchers.*;
import static com.github.restdriver.serverdriver.RestServerDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static com.seyren.acceptancetests.util.SeyrenDriver.*;

import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

public class ChecksTest {
	
	@Test
	public void testGetChecksReturnsOk() {
		Response response = get(checks());
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(0)));
	}
	
    @Test
    public void testCreateCheckReturnsCreated() {
        Response response = post(checks(), body("{  }", "application/json"));
        assertThat(response, hasStatusCode(201));
        assertThat(response, hasHeader("Location"));
        deleteLocation(response.getHeader("Location").getValue());
	}
	
	private void deleteLocation(String location) {
		assertThat(get(location), hasStatusCode(200));
        delete(location);
        assertThat(get(location), hasStatusCode(404));
	}

}
