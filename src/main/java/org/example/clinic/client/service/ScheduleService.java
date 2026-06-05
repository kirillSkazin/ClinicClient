package org.example.clinic.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.clinic.client.model.ScheduleEntry;
import org.example.clinic.client.model.TimeSlot;
import org.example.clinic.client.network.ServerClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleService {

    private final ServerClient client;

    public ScheduleService(ServerClient client) {
        this.client = client;
    }

    public List<ScheduleEntry> getDoctorSchedule(Long doctorId) {
        return client.call("schedules.get", Map.of("doctorId", doctorId),
                new TypeReference<>() {});
    }

    public ScheduleEntry upsert(Long doctorId, DayOfWeek dayOfWeek,
                                LocalTime startTime, LocalTime endTime, int slotMinutes) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("doctorId", doctorId);
        p.put("dayOfWeek", dayOfWeek);
        p.put("startTime", startTime.toString());
        p.put("endTime", endTime.toString());
        p.put("slotMinutes", slotMinutes);
        return client.call("schedules.upsert", p, ScheduleEntry.class);
    }

    public void delete(Long entryId) {
        client.callRaw("schedules.delete", Map.of("id", entryId));
    }

    public List<TimeSlot> availableSlots(Long doctorId, LocalDate date) {
        return client.call("schedules.available-slots",
                Map.of("doctorId", doctorId, "date", date.toString()),
                new TypeReference<>() {});
    }
}
