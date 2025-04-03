package dev.meetups.meetup;

import dev.meetups.meetup.model.Group;
import dev.meetups.meetup.model.MeetupEvent;
import dev.meetups.meetup.model.Venue;
import dev.meetups.model.When;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.meetups.model.When.*;

@Service
public class MeetupGraphQLClient {

    private static final String MEETUP_API_BASE_URL = "https://api.meetup.com/gql";
	private final HttpSyncGraphQlClient graphQlClient;

	public MeetupGraphQLClient() {
        RestClient restClient = RestClient.create(MEETUP_API_BASE_URL);
		graphQlClient = HttpSyncGraphQlClient.builder(restClient)
				.build();
	}

	public List<MeetupEvent> retrieveAllEvents(String meetupId, When when) {
		var meetupEvents = new ArrayList<MeetupEvent>();
		String groupEventsFilter = when.equals(PAST) ? "GroupPastEventsFilter" : "GroupUpcomingEventsFilter";
		String events = when.equals(PAST) ? "pastEvents" : "upcomingEvents";
		String document =
				"""
						query GetMeetupEvents($groupName: String!, $input: ConnectionInput!, $filter: ___filter___, $sortOrder: SortOrder) {
						      groupByUrlname(urlname: $groupName) {
						        id
						        name
						        urlname
						        ___events___(input: $input, filter: $filter, sortOrder: $sortOrder) {
						          edges {
						            node {
						              id
						              title
						              dateTime
						              venue {
						                name
						                address
						              }
						              eventUrl
						              description
						            }
						          }
						          pageInfo {
						            hasNextPage
						            endCursor
						          }
						        }
						      }
						    }
				""".replace("___filter___", groupEventsFilter).replace("___events___", events);

		GroupWithEvents groupWithEvents  = graphQlClient.document(document)
				.variable("groupName", meetupId)
				.variable("sortOrder", "ASC")
				.variable("input", new Input(1000L, null))
				.retrieveSync("groupByUrlname")
				.toEntity(GroupWithEvents.class);


		Group group = new Group(groupWithEvents.name(), Long.valueOf(groupWithEvents.id()), groupWithEvents.urlname());

		EventSearch eventSearch = when.equals(PAST) ? groupWithEvents.pastEvents() : groupWithEvents.upcomingEvents();
		eventSearch.edges().forEach(edge -> {
			LocalDateTime eventTime = LocalDateTime.parse(edge.node().dateTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			long epochSecond = eventTime.toEpochSecond(ZoneOffset.of("-04:00"));
			meetupEvents.add(new MeetupEvent(-1L, edge.node().id(), edge.node().title(), -1, epochSecond * 1000,edge.node().venue(), edge.node().eventUrl(), group));
		});
		return meetupEvents;
	}

	public record Input(Long first, Long after) {}
	public record GroupWithEvents(String id, String name, String urlname, EventSearch upcomingEvents, EventSearch pastEvents){}
	public record Node(String id, String title, String dateTime, Venue venue, String description, String eventUrl) { }
	public record Edge(Node node) { }
	public record EventSearch(Collection<Edge> edges) { }


}
