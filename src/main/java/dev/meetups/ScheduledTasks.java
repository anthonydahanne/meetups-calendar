package dev.meetups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTasks {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);
	private final GroupsIds groupsIds;
	private final FetchEvents fetchEvents;

	@Value("${autofill}")
	private Boolean autoFill;

	public ScheduledTasks(FetchEvents fetchEvents, GroupsIds groupsIds) {
		this.fetchEvents = fetchEvents;
		this.groupsIds = groupsIds;
	}
	@Scheduled(initialDelay = 1, fixedRate = 1, timeUnit = TimeUnit.DAYS)
//	@Scheduled(fixedRateString = "${scheduler.refreshRate}")
	public void retrieveMeetupEvents() {
		LOG.info("Fetching present and future events and saving them in DB");
		fetchEvents.retrieveAndSaveCncfFutureEvents(groupsIds.cncf);
		fetchEvents.retrieveAndSaveMeetupFutureEvents(groupsIds.meetup);
	}
}