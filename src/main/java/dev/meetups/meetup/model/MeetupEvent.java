package dev.meetups.meetup.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MeetupEvent(long created, @JsonProperty("id") String originalEventId, String name, int rsvpLimit, long time, Venue venue, String link, Group group){}
