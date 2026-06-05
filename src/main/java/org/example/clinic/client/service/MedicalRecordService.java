package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.MedicalRecord;
import org.example.clinic.client.network.ServerClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordService {

    private final ServerClient client;

    public MedicalRecordService(ServerClient client) {
        this.client = client;
    }

    public MedicalRecord add(Long appointmentId, String diagnosis,
                             String prescription, String recommendations) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("appointmentId", appointmentId);
        p.put("diagnosis", diagnosis);
        p.put("prescription", prescription);
        p.put("recommendations", recommendations);
        return client.call("records.add", p, MedicalRecord.class);
    }

    public List<MedicalRecord> mine() {
        return client.call("records.mine", null, new TypeReference<>() {});
    }

    public List<MedicalRecord> byPatient(Long patientId) {
        return client.call("records.by-patient", Map.of("patientId", patientId),
                new TypeReference<>() {});
    }

    public List<MedicalRecord> myIssued() {
        return client.call("records.my-issued", null, new TypeReference<>() {});
    }
}
