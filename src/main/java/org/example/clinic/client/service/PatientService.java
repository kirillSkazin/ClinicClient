package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.Patient;
import org.example.clinic.client.network.ServerClient;

import java.util.List;
import java.util.Map;

public class PatientService {

    private final ServerClient client;

    public PatientService(ServerClient client) {
        this.client = client;
    }

    public List<Patient> list() {
        return client.call("patients.list", null, new TypeReference<>() {});
    }

    public Patient get(Long id) {
        return client.call("patients.get", Map.of("id", id), Patient.class);
    }

    public Patient me() {
        return client.call("patients.me", null, Patient.class);
    }
}
