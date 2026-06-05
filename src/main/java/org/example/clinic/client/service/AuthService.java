package org.example.clinic.client.service;

import org.example.clinic.client.model.LoginResponse;
import org.example.clinic.client.network.ServerClient;
import org.example.clinic.client.session.Session;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {

    private final ServerClient client;

    public AuthService(ServerClient client) {
        this.client = client;
    }

    public LoginResponse login(String username, String password) {
        Map<String, Object> payload = Map.of(
                "username", username,
                "password", password);
        LoginResponse resp = client.call("auth.login", payload, LoginResponse.class);
        Session.get().setUser(resp.getToken(), resp.getUserId(),
                resp.getUsername(), resp.getFullName(), resp.getRole());
        return resp;
    }

    public LoginResponse registerPatient(String username, String password, String email,
                                         String fullName, String phone,
                                         LocalDate birthDate, String address,
                                         String insuranceNumber) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("email", email);
        payload.put("fullName", fullName);
        payload.put("phone", phone);
        payload.put("birthDate", birthDate);
        payload.put("address", address);
        payload.put("insuranceNumber", insuranceNumber);
        LoginResponse resp = client.call("auth.register-patient", payload, LoginResponse.class);
        Session.get().setUser(resp.getToken(), resp.getUserId(),
                resp.getUsername(), resp.getFullName(), resp.getRole());
        return resp;
    }

    public void logout() {
        Session.get().clear();
    }
}
