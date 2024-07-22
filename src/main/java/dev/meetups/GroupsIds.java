package dev.meetups;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "groups")
public class GroupsIds {

    Map<String, List<String>> meetup;
    Map<String, List<String>> cncf;

    public Map<String, List<String>> getMeetup() {
        return meetup;
    }

    public void setMeetup(Map<String, List<String>> meetup) {
        this.meetup = meetup;
    }

    public Map<String, List<String>> getCncf() {
        return cncf;
    }

    public void setCncf(Map<String, List<String>> cncf) {
        this.cncf = cncf;
    }

}