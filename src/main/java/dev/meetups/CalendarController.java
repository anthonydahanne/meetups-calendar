/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.meetups;

import dev.meetups.model.Event;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.meetups.EventColoring.colorFromGroupName;

/**
 * Controller for exposing {@link Event } as Full Calendar events.
 *
 * @author Andy Wilkinson
 * @author Brian Clozel
 * @author Anthony Dahanne
 */
@RestController
@RequestMapping("/calendar")
class CalendarController {

	private final EventRepository eventRepository;

	CalendarController(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	List<Map<String, Object>> releases(@RequestParam String start, @RequestParam String end, @RequestParam(name="city", required=false, defaultValue="montreal") String city) {
		return StreamSupport.stream(this.eventRepository.findAll().spliterator(), false).map((event) -> {
			Map<String, Object> formattedEvent = new HashMap<>();
			formattedEvent.put("title", event.getGroupName() + " " + event.getName());
			formattedEvent.put("allDay", true);
			formattedEvent.put("start", event.getDateTime());
			if (event.getUrl() != null) {
				formattedEvent.put("url", event.getUrl());
			}
			formattedEvent.put("backgroundColor", colorFromGroupName(event.getGroupName()));
//			if (event.getStatus() == Status.CLOSED) {
//				formattedEvent.put("backgroundColor", "#6db33f");
//			}
//			else if (event.isOverdue()) {
//				formattedEvent.put("backgroundColor", "#d14");
//			}
			return formattedEvent;
		}).collect(Collectors.toList());
	}

	@ExceptionHandler
	ResponseEntity<String> handleDateParseException(ParseException exc) {
		return ResponseEntity.badRequest().body(exc.getMessage());
	}

}
