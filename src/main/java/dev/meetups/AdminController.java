package dev.meetups;

import dev.meetups.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.meetups.AdminController.When.PAST;
import static dev.meetups.AdminController.When.UPCOMING;

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
		this.restClient = RestClient.create();
	}

	@PutMapping(path = "/refresh/{when}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void refreshDb(@PathVariable(value = "when", required = false) When when) {
		if (when.equals(PAST)) {
			LOG.info("Fetching past events and saving them in DB");
			fetchEvents.retrieveAndSaveMeetupPastEvents(groupsIds.meetup);
		} else if (when.equals(UPCOMING)) {
			LOG.info("Fetching present and future events and saving them in DB");
			fetchEvents.retrieveAndSaveMeetupFutureEvents(groupsIds.meetup);
		}
	}

	@DeleteMapping(path = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void clear() {
		eventRepository.deleteAll();
	}

	@DeleteMapping(path = "/cleanDuplicates", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void cleanDuplicates() {

		Map<String, List<Event>> eventsByGroupNameAndDateTime =
				Streamable.of(eventRepository.findAll())
						.stream()
						.collect(Collectors.groupingBy(event -> event.getGroupName() + "_" + event.getDateTime().toLocalDate().toString()));

		eventsByGroupNameAndDateTime.values().forEach(events -> {
			if (events.size() > 1) {
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
		});

	}

	private boolean isValidEvent(Event event) {
		AtomicBoolean isValid = new AtomicBoolean(true);
		restClient.get()
				.uri(String.valueOf(event.getUrl()))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					try {
						throw new HttpErrorException();
					} catch (HttpErrorException e) {
						isValid.set(false);
					}
				});
		return isValid.get();
	}


	static class HttpErrorException extends Exception {
	}

	enum When {
		PAST,
		UPCOMING
	}

}
