package dev.meetups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static dev.meetups.model.When.PAST;

@Component
public class StartUpAutoFill {

	private static final Logger LOG = LoggerFactory.getLogger(CalendarApiApplication.class);
	private final GroupsIds groupsIds;
	EventRepository eventRepository;
	private final FetchEvents fetchEvents;

	@Value("${autofill}")
	private Boolean autoFill;

	public StartUpAutoFill(EventRepository eventRepository, FetchEvents fetchEvents, GroupsIds groupsIds) {
		this.eventRepository = eventRepository;
		this.fetchEvents = fetchEvents;
		this.groupsIds = groupsIds;
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		if (eventRepository.count() > 0) {
			LOG.info("Database already created; skipping init");
		} else if (autoFill) {
			LOG.info("Fetching past events and saving them in DB");
			fetchEvents.retrieveAndSaveCncfEvents(groupsIds.cncf, PAST);
			fetchEvents.retrieveAndSaveMeetupEvents(groupsIds.meetup, PAST);
		}
	}
}
