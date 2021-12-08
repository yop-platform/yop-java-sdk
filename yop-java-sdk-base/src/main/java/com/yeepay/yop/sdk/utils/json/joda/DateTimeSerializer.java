package com.yeepay.yop.sdk.utils.json.joda;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.yeepay.yop.sdk.utils.DateUtils;
import org.joda.time.DateTime;

import java.io.IOException;

public class DateTimeSerializer extends StdScalarSerializer<DateTime> {

    public DateTimeSerializer() {
        super(DateTime.class);
    }

    @Override
    public void serialize(DateTime dateTime,
                          JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException {
        String dateTimeAsString = DateUtils.formatSimpleDateTime(dateTime);
        jsonGenerator.writeString(dateTimeAsString);
    }
}
