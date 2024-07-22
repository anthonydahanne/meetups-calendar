package dev.meetups;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id="events")
public class EventsEndpoint {

    private final EventRepository eventRepository;
    private final FetchEvents fetchEvents;
    private final GroupsIds groupsIds;

    public EventsEndpoint(EventRepository eventRepository, FetchEvents fetchEvents, GroupsIds groupsIds) {
        this.eventRepository = eventRepository;
        this.fetchEvents = fetchEvents;
        this.groupsIds = groupsIds;
    }

    @WriteOperation
    public int retrievedEvents() {
        return fetchEvents.retrieveAndSaveMeetupFutureEvents(groupsIds.meetup);
    }

    @ReadOperation
    public Long totalEventsInDatabase() {
       return eventRepository.count();
    }

}