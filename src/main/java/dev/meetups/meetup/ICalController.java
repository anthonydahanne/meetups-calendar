/*
 * Copyright 2016-2022 the original author or authors.
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

package dev.meetups.meetup;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Date;
import java.util.stream.StreamSupport;

/**
 * Controller for exposing {@link Event Events} as an iCalendar-format download.
 *
 * @author Andy Wilkinson
 */
@RestController
@RequestMapping("/ical")
class ICalController {

	private final EventRepository eventRepository;

	ICalController(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@RequestMapping(produces = "text/calendar")
	String calendar() {
		ICalendar calendar = new ICalendar();
		calendar.setExperimentalProperty("X-WR-CALNAME", "Events dates");
		StreamSupport.stream(this.eventRepository.findAll().spliterator(), false).map(this::createEvent).forEach(calendar::addEvent);
		return Biweekly.write(calendar).go();
	}

	private VEvent createEvent(Event release) {
		VEvent event = new VEvent();
		event.setSummary(release.getGroupName() + " " + release.getName());
		event.setDateStart(Date.from(release.getDateTime().atZone(ZoneId.systemDefault()).toInstant()));
		event.setDateEnd(Date.from(release.getDateTime().atZone(ZoneId.systemDefault()).toInstant()));
		return event;
	}

}
