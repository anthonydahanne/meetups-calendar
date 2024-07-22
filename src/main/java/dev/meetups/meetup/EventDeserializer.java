package dev.meetups.meetup;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import dev.meetups.meetup.model.MeetupEvent;

import java.io.IOException;
import java.time.Instant;

public class EventDeserializer extends JsonDeserializer {

    @Override
    public MeetupEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        TreeNode venueTreeNode = treeNode.get("venue");
        String venueName = "", eventName = null; Instant instant = null;
        if (venueTreeNode != null && venueTreeNode.size() > 0) {
            if (venueTreeNode.get("name") instanceof TextNode venueTextNode) {
                venueName = venueTextNode.asText();
            }
        }
        if (treeNode.get("time") instanceof LongNode timeLongNode) {
            instant = Instant.ofEpochMilli(timeLongNode.longValue());
        }
        if (treeNode.get("name") instanceof TextNode nameTextNode) {
            eventName = nameTextNode.asText();
        }
        return null;
//        return new Event(instant.getEpochSecond(), eventName, venueName);
    }
}
