package dev.meetups;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id="events")
public class EventsActuatorEndpoint {

    private final EventRepository eventRepository;

    public EventsActuatorEndpoint(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

//    @WriteOperation
//    public int retrievedEvents() {
//        return fetchEvents.retrieveAndSaveMeetupFutureEvents(groupsIds.meetup);
//    }

    @ReadOperation
    public Long totalEventsInDatabase() {
       return eventRepository.count();
    }

}