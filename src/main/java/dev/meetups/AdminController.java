package dev.meetups;

import dev.meetups.model.Event;
import dev.meetups.model.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.meetups.model.When.PAST;
import static dev.meetups.model.When.UPCOMING;

@RestController
@RequestMapping("/admin")
public class AdminController {
	private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);
	private final FetchEvents fetchEvents;
	private final GroupsIds groupsIds;
	private final EventRepository eventRepository;
	private final RestClient restClient;

	public AdminController(FetchEvents fetchEvents, GroupsIds groupsIds, EventRepository eventRepository) {
		this.fetchEvents = fetchEvents;
		this.groupsIds = groupsIds;
		this.eventRepository = eventRepository;
		HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
		this.restClient = RestClient.builder().requestFactory(new JdkClientHttpRequestFactory(httpClient)).build();
	}

	@PutMapping(path = "/refresh/{when}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void refreshDb(@PathVariable(value = "when", required = false) When when) {
		LOG.info("Fetching " + when + " events and saving them in DB");
		if (when.equals(PAST)) {
			fetchEvents.retrieveAndSaveMeetupEvents(groupsIds.meetup, PAST);
			fetchEvents.retrieveAndSaveCncfEvents(groupsIds.cncf, PAST);
			fetchEvents.retrieveAndSaveGuildEvents(groupsIds.guild, PAST);

		} else if (when.equals(UPCOMING)) {
			fetchEvents.retrieveAndSaveMeetupEvents(groupsIds.meetup, UPCOMING);
			fetchEvents.retrieveAndSaveCncfEvents(groupsIds.cncf, UPCOMING);
			fetchEvents.retrieveAndSaveGuildEvents(groupsIds.guild, UPCOMING);
		}
	}

	@DeleteMapping(path = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void clear() {
		eventRepository.deleteAll();
	}

	@DeleteMapping(path = "/cleanDuplicates", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void clean() {

		Map<String, List<Event>> eventsByGroupNameAndDateTime =
				Streamable.of(eventRepository.findAll())
						.stream()
						.collect(Collectors.groupingBy(event -> event.getGroupName() + "_" + event.getDateTime().toLocalDate().toString()));

		eventsByGroupNameAndDateTime.values().forEach(events -> {
			if (events.size() > 1) {
				cleanDuplicates(events);
			}
			cleanInvalidEvents(events);
		});

	}

	private void cleanInvalidEvents(List<Event> events) {
		events.forEach(event -> {
			if (event.getId().startsWith("m") && !isNumeric(event.getId().substring(1))) {
				// it looks like a placeholder for an event series in meetup.com
				// sometimes they're valid, sometimes they're not, let's check and clean
				if (!isValidEvent(event)) {
					LOG.warn("Removing this non valid event: {}", event);
					eventRepository.delete(event);
				}
			}
		});
	}


	private void cleanDuplicates(List<Event> events) {
		Optional<Event> validEvent = events.stream()
				.filter(this::isValidEvent)
				.findFirst();

		// If no valid event is found, fall back to the first event
		Event eventToKeep = validEvent.orElse(events.getFirst());

		// Remove the event to keep from the list and delete the rest
		events.remove(eventToKeep);

		events.forEach(entity -> {
			LOG.warn("Removing this non valid and duplicate event: {}", entity);
			eventRepository.delete(entity);
		});
	}

	private boolean isValidEvent(Event event) {
		ResponseEntity<String> result;
		try {
			result = restClient.get()
					.uri(String.valueOf(event.getUrl()))
					.retrieve()
					.toEntity(String.class);
		} catch (Exception e) {
			return false;
		}
		return result.getStatusCode().is2xxSuccessful();
	}

	public static boolean isNumeric(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}


	static class HttpErrorException extends Exception {
	}

}
