package dev.meetups.meetup;

import dev.meetups.meetup.model.MeetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class MeetupClient {

    private static final Logger LOG = LoggerFactory.getLogger(MeetupClient.class);
    private final RestClient restClient;
    private static final String MEETUP_API_BASE_URL = "https://api.meetup.com";

    public MeetupClient() {
        restClient = RestClient.create();
    }

    public List<MeetupEvent> retrieveAllFutureEvents(String meetupId) {
        return retrieveMeetupEvents(meetupId, "");
    }

    public List<MeetupEvent> retrieveAllPastEvents(String meetupId) {
        var status = "?status=past";
        return retrieveMeetupEvents(meetupId, status);
    }

    private List<MeetupEvent> retrieveMeetupEvents(String meetupId, String status) {
        List<MeetupEvent> result = Arrays.asList(restClient.get()
                .uri(MEETUP_API_BASE_URL + "/" + meetupId + "/events" + status )
                .retrieve()
                .body(MeetupEvent[].class));
		LOG.info("Successfully retrieved {} events for meetup {}", result.size(), meetupId);
        return result;
    }
}
