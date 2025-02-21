package dev.meetups;

import dev.meetups.cncf.model.CncfEvent;
import dev.meetups.cncf.model.CncfResult;
import dev.meetups.meetup.model.Group;
import dev.meetups.meetup.model.MeetupEvent;
import dev.meetups.meetup.model.Venue;
import dev.meetups.model.Event;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@SpringBootApplication
@EnableScheduling
@Configuration
@RegisterReflectionForBinding({MeetupEvent.class, Group.class, Venue.class, Event.class, CncfEvent.class, CncfResult.class })
public class CalendarApiApplication  implements RepositoryRestConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(CalendarApiApplication.class, args);
	}

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		config.exposeIdsFor(Event.class);
	}

}

