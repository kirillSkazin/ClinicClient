package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.Doctor;
import org.example.clinic.client.network.ServerClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DoctorService {

    private final ServerClient client;

    public DoctorService(ServerClient client) {
        this.client = client;
    }

    public List<Doctor> list() {
        return client.call("doctors.list", null, new TypeReference<>() {});
    }

    public List<Doctor> search(String query) {
        return client.call("doctors.search", Map.of("query", query == null ? "" : query),
                new TypeReference<>() {});
    }

    public List<Doctor> bySpecialization(Long specializationId) {
        return client.call("doctors.by-specialization",
                Map.of("specializationId", specializationId),
                new TypeReference<>() {});
    }

    public Doctor get(Long id) {
        return client.call("doctors.get", Map.of("id", id), Doctor.class);
    }

    public Doctor create(String username, String password, String email, String fullName,
                         String phone, Long specializationId, String roomNumber,
                         Integer experienceYears, String bio) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("username", username);
        p.put("password", password);
        p.put("email", email);
        p.put("fullName", fullName);
        p.put("phone", phone);
        p.put("specializationId", specializationId);
        p.put("roomNumber", roomNumber);
        p.put("experienceYears", experienceYears);
        p.put("bio", bio);
        return client.call("doctors.create", p, Doctor.class);
    }

    public Doctor update(Long id, String email, String fullName, String phone,
                         Long specializationId, String roomNumber,
                         Integer experienceYears, String bio) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", email);
        data.put("fullName", fullName);
        data.put("phone", phone);
        data.put("specializationId", specializationId);
        data.put("roomNumber", roomNumber);
        data.put("experienceYears", experienceYears);
        data.put("bio", bio);
        return client.call("doctors.update",
                Map.of("id", id, "data", data),
                Doctor.class);
    }

    public void delete(Long id) {
        client.callRaw("doctors.delete", Map.of("id", id));
    }
}
