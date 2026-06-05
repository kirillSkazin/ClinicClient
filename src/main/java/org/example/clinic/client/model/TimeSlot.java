package org.example.clinic.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeSlot {
    private LocalDateTime startsAt;
    private int durationMinutes;
    private boolean available;

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return startsAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " (" + durationMinutes + " мин)"
                + (available ? "" : " — занято");
    }
}
