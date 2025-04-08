package dev.meetups.guild;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.meetups.guild.model.GuildEvent;
import dev.meetups.model.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static dev.meetups.model.When.PAST;

@Service
public class GuildClient {

	private static final Logger LOG = LoggerFactory.getLogger(GuildClient.class);
	private static final String GUILD_API_BASE_URL = "https://guild.host/api/next/";
	private final RestClient restClient;

	public GuildClient() {
		restClient = RestClient.create();
	}

	public List<GuildEvent> retrieveAllEvents(String slug, When when) {
		String status = when.equals(PAST) ? "/past" : "";
		GuildResult result = restClient.get()
				.uri(GUILD_API_BASE_URL +  slug + "/events" + status)
				.retrieve()
				.body(GuildResult.class);
        	return result.events.edges.stream().map(edge -> edge.node).map(node -> new GuildEvent(node.id, node.slug, generateTitle(node.name, node.presentations), node.startAt, node.description, node.fullUrl)).toList();
	}

	private String generateTitle(String name, Presentations presentations) {
		return name + String.join(" - ", presentations.edges.stream().flatMap(presentation -> presentations.edges.stream()).map(edge -> edge.node.title).toList());
	}

	public record Node(String id, String slug, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") LocalDateTime startAt, String name, String description, String fullUrl, Presentations presentations) { }
	public record Presentations(List<PresentationEdge> edges) { }
	public record PresentationEdge(PresentationNode node) { }
	public record PresentationNode(String id, String slug, String title) { }
	public record Edge(Node node) { }
	public record GuildResult(Events events) { }
	public record Events(List<Edge> edges) { }

}
