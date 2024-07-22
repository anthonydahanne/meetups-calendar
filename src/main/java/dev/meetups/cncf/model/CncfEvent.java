package dev.meetups.cncf.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CncfEvent(@JsonProperty("id") String originalEventId,
						String title,
						@JsonProperty("chapter_title") String chapterTitle,
						@JsonProperty("venue_address") String venueAddress,
						@JsonProperty("venue_city") String venueCity,
						@JsonProperty("venue_zip_code") String venueZipCode,
						@JsonProperty("venue_name") String venueName,
						String url,
						@JsonProperty("start_date") LocalDateTime startDate,
						@JsonProperty("end_date") LocalDateTime endDate
){}
