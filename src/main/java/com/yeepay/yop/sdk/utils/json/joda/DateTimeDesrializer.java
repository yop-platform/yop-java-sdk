package com.yeepay.yop.sdk.utils.json.joda;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.yeepay.g3.core.yop.sdk.sample.utils.DateUtils;
import org.joda.time.DateTime;

import java.io.IOException;

public class DateTimeDesrializer extends StdScalarDeserializer<DateTime> {

    public DateTimeDesrializer() {
        super(DateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            String dateTimeAsString = jsonParser.getText().trim();
            return DateUtils.parseSimpleDateTime(dateTimeAsString);
        }
        throw deserializationContext.mappingException(getValueClass());
    }
}