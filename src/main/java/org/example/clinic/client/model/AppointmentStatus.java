package org.example.clinic.client.model;

public enum AppointmentStatus {
    PLANNED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    MISSED;

    public String displayName() {
        return switch (this) {
            case PLANNED   -> "Запланирован";
            case CONFIRMED -> "Подтверждён";
            case COMPLETED -> "Завершён";
            case CANCELLED -> "Отменён";
            case MISSED    -> "Пропущен";
        };
    }
}
