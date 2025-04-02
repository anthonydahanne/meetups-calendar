package dev.meetups.cncf;

import dev.meetups.cncf.model.CncfEvent;
import dev.meetups.cncf.model.CncfResult;
import dev.meetups.model.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import static dev.meetups.model.When.PAST;

@Service
public class CncfClient {

    private static final Logger LOG = LoggerFactory.getLogger(CncfClient.class);

    private final RestClient restClient;
    private final static String CNCF_API_BASE_URL = "https://community.cncf.io";

    public CncfClient() {
        restClient = RestClient.create();
    }

    public List<CncfEvent> retrieveAllEvents(String chapterId, When when) {
        String status = when.equals(PAST) ? "Completed" : "Live";
        CncfResult result = restClient.get()
                .uri(CNCF_API_BASE_URL + "/api/event_slim/?chapter=" + chapterId + "&status="+ status +"&page_size=100&include_cohosted_events=true&visible_on_parent_chapter_only=true&order=-start_date&fields=title,start_date,event_type_title,cropped_picture_url,cropped_banner_url,url,cohost_registration_url,description,description_short,id,venue_name,venue_address,venue_zip_code,venue_city,chapter_title")
                .retrieve()
                .body(CncfResult.class);
        LOG.info("Successfully retrieved " + result.results().size() + " events for chapter " + chapterId);
        return result.results();
    }

}
