package com.seyren.core.util.velocity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seyren.core.domain.Alert;
import com.seyren.core.domain.Check;
import com.seyren.core.domain.Subscription;
import com.seyren.core.util.config.SeyrenConfig;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Represents common denominator of {@link com.seyren.core.util.velocity.VelocityHttpHelper}
 * and {@link com.seyren.core.util.velocity.VelocityEmailHelper}.
 *
 * @author <a href="mailto:tobias.lindenmann@1und1.de">Tobias Lindenmann</a>
 */
class AbstractHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    protected final SeyrenConfig seyrenConfig;

    public AbstractHelper(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    protected VelocityContext createVelocityContext(Check check, Subscription subscription, List<Alert> alerts) {
        Objects.requireNonNull(check, "check is null");

        VelocityContext result = new VelocityContext();
        result.put("CHECK", check);
        result.put("ALERTS", alerts);
        result.put("SEYREN_URL", seyrenConfig.getBaseUrl());
        result.put("SUBSCRIPTION", subscription);
        result.put("PREVIEW", getPreviewImage(check));
        result.put("JSONMAPPER", MAPPER);
        return result;
    }

    private String getPreviewImage(Check check) {
        return "<br /><img src=" + seyrenConfig.getGraphiteUrl() + "/render/?target=" + check.getTarget() + getTimeFromUntilString(new Date()) +
                "&target=alias(dashed(color(constantLine(" + check.getWarn().toString() + "),%22yellow%22)),%22warn%20level%22)&target=alias(dashed(color(constantLine(" + check.getError().toString()
                + "),%22red%22)),%22error%20level%22)&width=500&height=225></img>";

    }

    private String getTimeFromUntilString(Date date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm_yyyyMMdd");
        cal.setTime(date);
        cal.add(Calendar.HOUR, -1);
        String from = format.format(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        String until = format.format(cal.getTime());

        return "&from=" + until.toString() + "&until=" + from.toString();
    }

    protected String getTemplateAsString(String fileName) {
        Objects.requireNonNull(fileName, "filename is null");

        try {
            // Handle the template filename as either a class path resource or an absolute path to the filesystem.
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                inputStream = new FileInputStream(fileName);
            }
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Template file could not be found on classpath at " + fileName);
        }
    }
}
