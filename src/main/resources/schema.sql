create table if not exists calendar_events (
                                         id varchar not null,
                                         group_name varchar not null,
                                         name varchar not null,
                                         url varchar not null,
                                         date_time date not null,
                                         primary key (id)
--     UNIQUE (email)
    );