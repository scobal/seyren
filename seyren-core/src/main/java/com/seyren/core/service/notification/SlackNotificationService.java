/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.service.notification;

import static com.google.common.collect.Iterables.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.domain.SubscriptionType;
import com.seyren.core.exception.NotificationFailedException;
import com.seyren.core.util.config.SeyrenConfig;

@Named
public class SlackNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationService.class);

    private final SeyrenConfig seyrenConfig;
    private final String baseUrl;

    @Inject
    public SlackNotificationService(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = "https://slack.com";
    }

    protected SlackNotificationService(SeyrenConfig seyrenConfig, String baseUrl) {
        this.seyrenConfig = seyrenConfig;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendNotification(Check check, Subscription subscription, List<Alert> alerts) throws NotificationFailedException {
        String token = seyrenConfig.getSlackToken();
        String channel = subscription.getTarget();
        String username = seyrenConfig.getSlackUsername();
        String iconUrl = seyrenConfig.getSlackIconUrl();

        List<String> emojis = Lists.newArrayList(
                Splitter.on(',').omitEmptyStrings().trimResults().split(seyrenConfig.getSlackEmojis())
        );

        String url = String.format("%s/api/chat.postMessage", baseUrl);
        HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
        HttpPost post = new HttpPost(url);
        post.addHeader("accept", "application/json");

        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
        parameters.add(new BasicNameValuePair("token", token));
        parameters.add(new BasicNameValuePair("channel", StringUtils.removeEnd(channel, "!")));
        parameters.add(new BasicNameValuePair("text", check.getState().toString()));
        parameters.add(new BasicNameValuePair("username", check.getName()));
        parameters.add(new BasicNameValuePair("icon_url", iconUrl));
        parameters.add(new BasicNameValuePair("attachments", formatAttachment(check, alerts)));

        try {
            post.setEntity(new UrlEncodedFormEntity(parameters));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("> parameters: {}", parameters);
            }
            HttpResponse response = client.execute(post);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("> parameters: {}", parameters);
                LOGGER.debug("Status: {}, Body: {}", response.getStatusLine(), new BasicResponseHandler().handleResponse(response));
            }
        } catch (Exception e) {
            LOGGER.warn("Error posting to Slack", e);
        } finally {
            post.releaseConnection();
            HttpClientUtils.closeQuietly(client);
        }

    }

    @Override
    public boolean canHandle(SubscriptionType subscriptionType) {
        return subscriptionType == SubscriptionType.SLACK;
    }

    private String formatAttachment(Check check, List<Alert> alerts){
        String name = check.getName();
        String state = check.getState().toString();
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String color;
        String actions = "";
        String dashUrl = "";
        String dashText = "";
        String runText = "";
        String runUrl = "";
        String titletext = String.format("Check <%s|*%s*> has entered its %s state.", url, name, state);
        String message = " ";
        if (!state.equals("OK")) {
            message = String.format("%s", check.getDescription()
                .replaceAll("<br/><br/>Last synced by iWatchman.*$","")
                .replaceAll("<br/><br/>Created by iWatchman.*$","")
                .replaceAll("<br/>","\n")
                .replaceAll("<b>","*")
                .replaceAll("</b>","*")
                .replaceAll("<.*>","")
                .replaceAll("Please fully define the feature '.*' in Platform Metadata.","")
                .replaceAll("Please fully define the feature '.*' in .",""));
        }

        if (message.contains("http://grafana.je-labs.com")){
            String[] parts = message.split("\\s+");
            for (String part: parts){
                if (part.contains("http://grafana.je-labs.com")){
                    dashUrl = part;
                }
            }
            message = message.replace(dashUrl, "");
            message = message.replace("Dashboard:", "");
            message = message.replace("Dashboards:", "");
        }

        if (message.toLowerCase().contains("runbook.md")) {
            String[] parts = message.split("\\s+");
            for (String part: parts){
                if (part.toLowerCase().contains("runbook.md")){
                    runUrl = part;
                }
            }
            message = message.replace(runUrl, "");
            message = message.replace("Runbook:", "");           
        }

        if (!dashUrl.isEmpty()){
            dashText = String.format("{\"type\": \"button\", \"text\": \"Dashboard :chart_with_downwards_trend:\", \"url\": \"%s\" }", dashUrl);
        }
        if (!runUrl.isEmpty()){
            runText = String.format("{\"type\": \"button\", \"text\": \"Runbook :book:\", \"url\": \"%s\" }", runUrl);
        }
        if (!dashText.isEmpty() || !runText.isEmpty()){
            actions = String.format("\"actions\": [ %s, %s ]", dashText, runText);
        }

        String text = String.format("\"text\":\"%s\"", message);
        //Slack colors can be good, warning, danger
        //Seyren gives us OK, WARN, ERROR
        //I'd use switch but it's only good for Java 1.7 and above

        if (state.equals("OK")) {
            color = "\"color\": \"good\"";
        } else if (state.equals("WARN")) {
            color = "\"color\": \"warning\"";
        } else if (state.equals("ERROR")) {
            color = "\"color\": \"danger\"";
        } else {
            color = "\"color\": \"#000088\"";
        }

        String fallback = String.format("\"fallback\":\"%s\"", titletext);
        String title = String.format("\"title\":\"%s\"", titletext);
        String attachment = String.format("[{%s, %s, %s, %s, %s}]", fallback, text, color, title, actions );
        return attachment;
    }


    //If all of the info is to be in the attachment the below function is redundant
    private String formatContent(List<String> emojis, Check check, Subscription subscription, List<Alert> alerts) {
        String url = String.format("%s/#/checks/%s", seyrenConfig.getBaseUrl(), check.getId());
        String alertsString = Joiner.on("\n").join(transform(alerts, new Function<Alert, String>() {
            @Override
            public String apply(Alert input) {
                return String.format("%s = %s (%s to %s)", input.getTarget(), input.getValue().toString(), input.getFromType(), input.getToType());
            }
        }));

        String channel = subscription.getTarget().contains("!") ? "<!channel>" : "";

        String description;
        if (StringUtils.isNotBlank(check.getDescription()) && check.getState().ordinal() != 1) {
            description = String.format("\n>%s", check.getDescription().replaceAll("<br/><br/>Last synced by iWatchman.*$","").replaceAll("<br/><br/>Created by iWatchman.*$","").replaceAll("<br/>","\n").replaceAll("\n",">\n").replaceAll("<b>","*").replaceAll("</b>","*"));
        } else {
            description = "";
        }

        final String state = check.getState().toString();

        return String.format("%s Check <%s|*%s*> has entered its %s state %s %s",
                Iterables.get(emojis, check.getState().ordinal(), ""),
                url,
                check.getName(),
		state,
		description,
                channel
        );
    }
}
