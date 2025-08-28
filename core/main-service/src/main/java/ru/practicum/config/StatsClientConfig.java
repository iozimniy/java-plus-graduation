package ru.practicum.config;

import lombok.Data;

@Data
public class StatsClientConfig {

    //@Value("${stats.client.host}")
    private String host;

    //@Value("${stats.client.port}")
    private String port;
}
