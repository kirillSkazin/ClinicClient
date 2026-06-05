package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.Appointment;
import org.example.clinic.client.network.ServerClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentService {

    private final ServerClient client;

    public AppointmentService(ServerClient client) {
        this.client = client;
    }

    public Appointment book(Long doctorId, LocalDateTime startsAt,
                             Integer durationMinutes, String complaint) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("doctorId", doctorId);
        p.put("startsAt", startsAt.toString());
        p.put("durationMinutes", durationMinutes == null ? 30 : durationMinutes);
        p.put("complaint", complaint);
        return client.call("appointments.book", p, Appointment.class);
    }

    public Appointment cancel(Long appointmentId) {
        return client.call("appointments.cancel", Map.of("id", appointmentId), Appointment.class);
    }

    public Appointment confirm(Long appointmentId) {
        return client.call("appointments.confirm", Map.of("id", appointmentId), Appointment.class);
    }

    public Appointment complete(Long appointmentId, String notes) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", appointmentId);
        p.put("notes", notes);
        return client.call("appointments.complete", p, Appointment.class);
    }

    public Appointment markMissed(Long appointmentId) {
        return client.call("appointments.miss", Map.of("id", appointmentId), Appointment.class);
    }

    public Appointment reschedule(Long appointmentId, LocalDateTime startsAt, Integer durationMinutes) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", appointmentId);
        p.put("startsAt", startsAt.toString());
        if (durationMinutes != null) p.put("durationMinutes", durationMinutes);
        return client.call("appointments.reschedule", p, Appointment.class);
    }

    public List<Appointment> mine() {
        return client.call("appointments.mine", null, new TypeReference<>() {});
    }

    public List<Appointment> upcomingForPatient() {
        return client.call("appointments.upcoming", null, new TypeReference<>() {});
    }

    public List<Appointment> doctorSchedule(Long doctorId, LocalDateTime from, LocalDateTime to) {
        Map<String, Object> p = new LinkedHashMap<>();
        if (doctorId != null) p.put("doctorId", doctorId);
        if (from != null) p.put("from", from.toString());
        if (to != null) p.put("to", to.toString());
        return client.call("appointments.doctor-schedule", p, new TypeReference<>() {});
    }
}
