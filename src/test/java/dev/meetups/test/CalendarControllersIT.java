package dev.meetups.test;

import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import dev.meetups.meetup.EventRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CalendarControllersIT {
    @LocalServerPort
    private int port;

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
    private EventRepository eventRepository;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @AfterEach
    public void resetDB() {
        eventRepository.deleteAll();
    }

    @Test
    @Order(1)
    public void testNoEventsYet() throws JSONException {

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/events"),
                HttpMethod.GET, entity, String.class);

        String expected = """
                {
                  "_embedded": {
                    "events": [
                    ]
                  },
                  "_links": {
                    "self": {
                      "href": "http://localhost:PORT/events"
                    },
                    "profile": {
                      "href": "http://localhost:PORT/profile/events"
                    }
                  }
                }

                """.replaceAll("PORT", String.valueOf(port));

        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    @Order(2)
    public void testPostEventAndThenRetrieve() throws JSONException {

        String jsonToPut = """
                {
                  "id": "1",
                  "groupName": "MontrealJUG",
                  "name": "OpenRewrite: Where the code fixes itself (plus all the dependencies)",
                  "url": "https://www.meetup.com/montreal-jug/events/292492289/",
                  "dateTime": "2023-04-18T17:30"
                }
                """;

        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity<String> entity = new HttpEntity<>(jsonToPut, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/events/1"),
                HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        headers.remove(HttpHeaders.CONTENT_TYPE);
        headers.add(HttpHeaders.ACCEPT, "application/json");
        entity = new HttpEntity<>(jsonToPut, headers);
        response = restTemplate.exchange(
                createURLWithPort("/events"),
                HttpMethod.GET, entity, String.class);
        String expected = """
                {
                  "_embedded": {
                    "events": [
                     {
                            "groupName": "MontrealJUG",
                            "name": "OpenRewrite: Where the code fixes itself (plus all the dependencies)",
                            "url": "https://www.meetup.com/montreal-jug/events/292492289/",
                            "dateTime": "2023-04-18T17:30:00",
                            "_links": {
                              "self": {
                                "href": "http://localhost:PORT/events/1"
                              },
                              "event": {
                                "href": "http://localhost:PORT/events/1"
                              }
                            }
                          }
                    ]
                  },
                  "_links": {
                    "self": {
                      "href": "http://localhost:PORT/events"
                    },
                    "profile": {
                      "href": "http://localhost:PORT/profile/events"
                    }
                  }
                }

                """.replaceAll("PORT", String.valueOf(port));

        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    @Order(3)
    public void testPostEventAndThenRetrieveIcal() {

        String jsonToPut = """
                {
                  "id": "2",
                  "groupName": "MontrealJUG",
                  "name": "OpenRewrite: Where the code fixes itself (plus all the dependencies)",
                  "url": "https://www.meetup.com/montreal-jug/events/292492289/",
                  "dateTime": "2023-04-18T17:30"
                }
                """;

        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity<String> entity = new HttpEntity<>(jsonToPut, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/events/2"),
                HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        headers.remove(HttpHeaders.CONTENT_TYPE);
        entity = new HttpEntity<>(jsonToPut, headers);
        response = restTemplate.exchange(
                createURLWithPort("/ical"),
                HttpMethod.GET, entity, String.class);
        String expected = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Michael Angstadt//biweekly 0.6.7//EN
                X-WR-CALNAME:Events dates
                BEGIN:VEVENT
                                
                                
                SUMMARY:MontrealJUG OpenRewrite: Where the code fixes itself (plus all the\s
                 dependencies)
                 
                 
                END:VEVENT
                END:VCALENDAR
                """;


        String actual = response.getBody()
                .replaceAll("UID.*", "")
                .replaceAll("DTSTAMP.*", "")
                .replaceAll("DTSTART.*", "")
                .replaceAll("DTEND.*", "")
                .replaceAll("\r", "");
        assertEquals(expected, actual);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
