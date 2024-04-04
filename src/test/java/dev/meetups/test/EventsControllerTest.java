package dev.meetups.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import dev.meetups.meetup.Event;
import dev.meetups.meetup.EventRepository;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventsControllerTest {

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


	@LocalServerPort
	private Integer port;

	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			"postgres:15-alpine"
	).waitingFor(Wait.forListeningPorts(5432));

	@BeforeAll
	static void beforeAll() {
		postgres.start();
	}

	@AfterAll
	static void afterAll() {
		postgres.stop();
	}

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	EventRepository eventRepository;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost:" + port;
		eventRepository.deleteAll();
	}

	@Test
	void shouldGetAllEvents() throws URISyntaxException, MalformedURLException {

		List<Event> events = List.of(
				new Event("1",
						"MontrealJUG",
						"OpenRewrite: Where the code fixes itself (plus all the dependencies)",
						new URI("https://www.meetup.com/montreal-jug/events/292492289/").toURL(),
						LocalDateTime.parse("2024-03-18T17:30", formatter)
				)
		);
		eventRepository.saveAll(events);

		given()
				.contentType(ContentType.JSON)
				.when()
				.get("/events")
				.then()
				.statusCode(200)
				.body("_embedded.events", hasSize(1));
	}
}