package org.example.clinic.client.ui.views;

import javafx.scene.control.Label;
import org.example.clinic.client.model.AppointmentStatus;


public class StatusChip extends Label {

    public StatusChip(AppointmentStatus status) {
        super(status.displayName());
        getStyleClass().setAll("status-chip", "status-" + status.name());
    }
}
