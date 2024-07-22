package dev.meetups;

import dev.meetups.cncf.CncfClient;
import dev.meetups.cncf.model.CncfEvent;
import dev.meetups.meetup.MeetupClient;
import dev.meetups.meetup.model.MeetupEvent;
import dev.meetups.model.Event;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class FetchEvents {
	public static final ZoneId MONTREAL_ZONE_ID = ZoneId.of("America/Montreal");


	MeetupClient meetupClient;
	CncfClient cncfClient;

	EventRepository eventRepository;

	public FetchEvents(MeetupClient meetupClient, CncfClient cncfClient, EventRepository eventRepository, GroupsIds groupsIds) {
		this.meetupClient = meetupClient;
		this.cncfClient = cncfClient;
		this.eventRepository = eventRepository;
	}

	public void retrieveAndSaveMeetupPastEvents(Map<String, List<String>> groupsIds) {
		groupsIds.entrySet().stream()
				.map(city -> city.getValue())
				.flatMap(meetupIds -> meetupIds.stream())
				.forEach(meetupId -> {
					List<MeetupEvent> meetupEvents = meetupClient.retrieveAllPastEvents(meetupId);
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

	public void retrieveAndSaveCncfPastEvents(Map<String, List<String>> groupsIds) {
		groupsIds.entrySet().stream()
				.map(city -> city.getValue())
				.flatMap(meetupIds -> meetupIds.stream())
				.forEach(cncfMeetupId -> {
					List<CncfEvent> meetupEvents = cncfClient.retrieveAllPastEvents(cncfMeetupId);
					meetupEvents.forEach(cncfEvent -> {
						try {
							Event standardEvent = cncfEventToStandardEvent(cncfEvent);
							eventRepository.save(standardEvent);
						} catch (MalformedURLException | URISyntaxException e) {
							throw new RuntimeException(e);
						}
					});
				});
	}


	public int retrieveAndSaveMeetupFutureEvents(Map<String, List<String>> groupsIds) {
		int[] successes = {0};
		groupsIds.entrySet().stream()
				.map(city -> city.getValue())
				.flatMap(meetupIds -> meetupIds.stream())
				.forEach(meetupId -> {
					List<MeetupEvent> meetupEvents = meetupClient.retrieveAllFutureEvents(meetupId);
					meetupEvents.forEach(meetupEvent -> {
						try {
							Event standardEvent = meetupEventToStandardEvent(meetupEvent);
							eventRepository.save(standardEvent);
							successes[0]++;
						} catch (MalformedURLException | URISyntaxException e) {
							throw new RuntimeException(e);
						}
					});
				});
		return successes[0];
	}

	public void retrieveAndSaveCncfFutureEvents(Map<String, List<String>> groupsIds) {
		groupsIds.entrySet().stream()
				.map(city -> city.getValue())
				.flatMap(meetupIds -> meetupIds.stream())
				.forEach(cncfMeetupId -> {
					List<CncfEvent> meetupEvents = cncfClient.retrieveAllFutureEvents(cncfMeetupId);
					meetupEvents.forEach(cncfEvent -> {
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
