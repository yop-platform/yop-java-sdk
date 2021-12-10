package com.yeepay.yop.sdk.utils.json.joda;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.yeepay.yop.sdk.utils.DateUtils;
import org.joda.time.DateTime;

import java.io.IOException;

public class DateTimeDeserializer extends StdScalarDeserializer<DateTime> {

    public DateTimeDeserializer() {
        super(DateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext) throws IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING) {
            String dateTimeAsString = jsonParser.getText().trim();
            return DateUtils.parseSimpleDateTime(dateTimeAsString);
        }
        throw deserializationContext.mappingException(getValueClass());
    }
}
