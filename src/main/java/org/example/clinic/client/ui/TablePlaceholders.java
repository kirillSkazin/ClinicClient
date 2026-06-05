package org.example.clinic.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public final class TablePlaceholders {

    private TablePlaceholders() {
    }

    public static VBox empty(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("table-placeholder-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("table-placeholder-subtitle");
        subtitleLabel.setWrapText(true);

        VBox box = new VBox(6, titleLabel, subtitleLabel);
        box.getStyleClass().add("table-placeholder");
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
