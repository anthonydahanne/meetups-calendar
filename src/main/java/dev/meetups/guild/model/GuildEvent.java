package dev.meetups.guild.model;

import java.time.LocalDateTime;

public record GuildEvent(String id, String slug, String title, LocalDateTime startAt, String description, String fullUrl) { }