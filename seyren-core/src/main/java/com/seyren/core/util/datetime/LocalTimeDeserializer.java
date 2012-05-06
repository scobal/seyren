package com.seyren.core.util.datetime;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.LocalTime;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

	@Override
	public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		String raw = parser.getText();
		int hours = Integer.valueOf(raw.substring(0, 2));
		int mins = Integer.valueOf(raw.substring(2));
		return new LocalTime(hours, mins);
	}
	
}
