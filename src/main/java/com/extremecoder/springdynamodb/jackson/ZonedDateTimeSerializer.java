package com.extremecoder.springdynamodb.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

	public ZonedDateTimeSerializer() {
		super(ZonedDateTime.class);
	}

	@Override
	public void serialize(ZonedDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonGenerationException {
		jgen.writeString(value.toString());
	}

}
