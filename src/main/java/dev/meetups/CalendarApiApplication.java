package dev.meetups;

import dev.meetups.model.Event;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@SpringBootApplication
@EnableScheduling
public class CalendarApiApplication  implements RepositoryRestConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(CalendarApiApplication.class, args);
	}

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		config.exposeIdsFor(Event.class);
	}


}

