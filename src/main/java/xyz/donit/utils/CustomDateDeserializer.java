package xyz.donit.utils;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.time.Instant;

/**
 * Created by felix on 1/28/17.
 */
public class CustomDateDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext ctx) throws IOException{
        Instant instant = Instant.parse(parser.getText());
        return instant;
    }
}
