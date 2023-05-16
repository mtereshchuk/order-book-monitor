package com.mtereshchuk.assignment.provider.binance.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author mtereshchuk
 */
public record OrderBook(long lastUpdateId, List<Entry> bids, List<Entry> asks) {
    @JsonDeserialize(using = EntryDeserializer.class)
    public record Entry(BigDecimal price, BigDecimal size) {
        public static final Predicate<Entry> REMOVED_PREDICATE =
                entry -> entry.size.compareTo(BigDecimal.ZERO) == 0;
    }

    private static class EntryDeserializer extends JsonDeserializer<Entry> {
        @Override
        public Entry deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            var price = new BigDecimal(node.get(0).asText());
            var size = new BigDecimal(node.get(1).asText());
            return new Entry(price, size);
        }
    }
}
