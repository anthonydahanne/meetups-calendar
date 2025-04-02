package dev.meetups;

import dev.meetups.cncf.CncfClient;
import dev.meetups.cncf.model.CncfEvent;
import dev.meetups.meetup.MeetupGraphQLClient;
import dev.meetups.meetup.model.MeetupEvent;
import dev.meetups.model.Event;
import dev.meetups.model.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class FetchEvents {
	public static final ZoneId MONTREAL_ZONE_ID = ZoneId.of("America/Montreal");
	private static final Logger LOG = LoggerFactory.getLogger(FetchEvents.class);

	MeetupGraphQLClient meetupClient;
	CncfClient cncfClient;
	EventRepository eventRepository;

	public FetchEvents(MeetupGraphQLClient meetupClient, CncfClient cncfClient, EventRepository eventRepository) {
		this.meetupClient = meetupClient;
		this.cncfClient = cncfClient;
		this.eventRepository = eventRepository;
	}

	public void retrieveAndSaveMeetupEvents(Map<String, List<String>> groupsIds, When when) {
		groupsIds.values().stream()
				.flatMap(Collection::stream)
				.forEach(meetupId -> {
					List<MeetupEvent> meetupEvents = meetupClient.retrieveAllEvents(meetupId, when);
					LOG.info("Retrieved " + meetupEvents.size() + " events for " + meetupId);
					meetupEvents.forEach(meetupEvent -> {
						try {
							Event standardEvent = meetupEventToStandardEvent(meetupEvent);
							eventRepository.save(standardEvent);
						} catch (MalformedURLException | URISyntaxException e) {
							throw new RuntimeException(e);
						}
					});
				});
	}

	public void retrieveAndSaveCncfEvents(Map<String, List<String>> groupsIds, When when) {
		groupsIds.values().stream()
				.flatMap(Collection::stream)
				.forEach(cncfMeetupId -> {
					List<CncfEvent> cncfEvents = cncfClient.retrieveAllEvents(cncfMeetupId, when);
					LOG.info("Retrieved " + cncfEvents.size() + " events for " + cncfMeetupId);
					cncfEvents.forEach(cncfEvent -> {
						try {
							Event standardEvent = cncfEventToStandardEvent(cncfEvent);
							eventRepository.save(standardEvent);
						} catch (MalformedURLException | URISyntaxException e) {
							throw new RuntimeException(e);
						}
					});
				});
	}

	private Event meetupEventToStandardEvent(MeetupEvent meetupEvent) throws MalformedURLException, URISyntaxException {
		LocalDateTime startTIme = LocalDateTime.ofInstant(Instant.ofEpochMilli(meetupEvent.time()), MONTREAL_ZONE_ID);
		return new Event("m" + meetupEvent.originalEventId(), meetupEvent.group().name(), meetupEvent.name(), new URI(meetupEvent.link()).toURL(), startTIme);
	}

	private Event cncfEventToStandardEvent(CncfEvent cncfEvent) throws MalformedURLException, URISyntaxException {
		long eventId = Long.parseLong(cncfEvent.originalEventId());
		return new Event("c" + eventId,  cncfEvent.chapterTitle(), cncfEvent.title(), new URI(cncfEvent.url()).toURL(), cncfEvent.startDate());
	}

}
