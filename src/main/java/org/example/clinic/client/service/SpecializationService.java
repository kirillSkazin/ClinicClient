package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.Specialization;
import org.example.clinic.client.network.ServerClient;

import java.util.List;
import java.util.Map;

public class SpecializationService {

    private final ServerClient client;

    public SpecializationService(ServerClient client) {
        this.client = client;
    }

    public List<Specialization> list() {
        return client.call("specializations.list", null, new TypeReference<>() {});
    }

    public Specialization create(String name, String description) {
        return client.call("specializations.create",
                Map.of("name", name, "description", description == null ? "" : description),
                Specialization.class);
    }

    public Specialization update(Long id, String name, String description) {
        return client.call("specializations.update",
                Map.of("id", id, "name", name, "description", description == null ? "" : description),
                Specialization.class);
    }

    public void delete(Long id) {
        client.callRaw("specializations.delete", Map.of("id", id));
    }
}
