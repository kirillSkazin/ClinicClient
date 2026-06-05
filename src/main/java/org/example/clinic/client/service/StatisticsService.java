package org.example.clinic.client.service;

import org.example.clinic.client.model.Statistics;
import org.example.clinic.client.network.ServerClient;

public class StatisticsService {

    private final ServerClient client;

    public StatisticsService(ServerClient client) {
        this.client = client;
    }

    public Statistics summary() {
        return client.call("statistics.summary", null, Statistics.class);
    }
}
