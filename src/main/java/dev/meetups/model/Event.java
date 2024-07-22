package dev.meetups.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URL;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
public class Event {
    public Event() {
    }

    public Event(String id, String groupName, String name, URL url, LocalDateTime dateTime) {
        this.id = id;
        this.groupName = groupName;
        this.name = name;
        this.url = url;
        this.dateTime = dateTime;
    }

    @Id
    private String id;

    @NotBlank(message = "groupName of the event is mandatory")
    private String groupName;

    @NotBlank(message = "name of the event is mandatory")
    private String name;

    @NotNull(message = "url to the event is mandatory")
    private URL url;

    @NotNull(message = "Full date and time of (local timezone) of the event is mandatory")
    private LocalDateTime dateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String categoryName) {
        this.groupName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}