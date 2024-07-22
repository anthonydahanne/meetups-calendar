package dev.meetups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static dev.meetups.AdminController.When.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
	private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);
	private final FetchEvents fetchEvents;
	private final GroupsIds groupsIds;
	private final EventRepository eventRepository;

	public AdminController(FetchEvents fetchEvents, GroupsIds groupsIds, EventRepository eventRepository) {
		this.fetchEvents = fetchEvents;
		this.groupsIds = groupsIds;
		this.eventRepository = eventRepository;
	}

	@PutMapping(path = "/refresh/{when}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void refreshDb(@PathVariable(value = "when",required = false)  When when) {
		if (when.equals(PAST)) {
			LOG.info("Fetching past events and saving them in DB");
			fetchEvents.retrieveAndSaveMeetupPastEvents(groupsIds.meetup);
		} else if(when.equals(UPCOMING)) {
			LOG.info("Fetching present and future events and saving them in DB");
			fetchEvents.retrieveAndSaveMeetupFutureEvents(groupsIds.meetup);
		}
	}

	@DeleteMapping(path="/clear",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void clear() {
		eventRepository.deleteAll();
	}
	enum When {
		PAST,
		UPCOMING
	}

}
