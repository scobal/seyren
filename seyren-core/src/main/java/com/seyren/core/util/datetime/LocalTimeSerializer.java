package com.seyren.core.util.datetime;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.LocalTime;

public class LocalTimeSerializer extends JsonSerializer<LocalTime> {

	@Override
	public void serialize(LocalTime time, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		gen.writeString(padWithZero(time.getHourOfDay()) + padWithZero(time.getMinuteOfHour()));
	}

	private String padWithZero(int value) {
		if (Integer.toString(value).length() == 1) {
			return "0" + value;
		}
		return "" + value;
	}

}
