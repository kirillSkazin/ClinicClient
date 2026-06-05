package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Appointment;
import org.example.clinic.client.model.AppointmentStatus;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.TablePlaceholders;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

import java.time.format.DateTimeFormatter;

public class AdminAppointmentsView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;
    private final TableView<Appointment> table = new TableView<>();
    private final MFXButton refreshBtn = new MFXButton("Обновить");
    private final MFXButton cancelBtn = new MFXButton("Отменить выбранную");

    public AdminAppointmentsView(MainWindow window) {
        this.window = window;

        Label title = new Label("Все приёмы");
        title.getStyleClass().add("card-title");

        TableColumn<Appointment, String> when = new TableColumn<>("Когда");
        when.setMinWidth(150);
        when.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartsAt().format(DT)));

        TableColumn<Appointment, String> doctor = new TableColumn<>("Врач");
        doctor.setMinWidth(220);
        doctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDoctorName() + " (" + c.getValue().getSpecialization() + ")"));

        TableColumn<Appointment, String> patient = new TableColumn<>("Пациент");
        patient.setMinWidth(220);
        patient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPatientName()));

        TableColumn<Appointment, AppointmentStatus> status = new TableColumn<>("Статус");
        status.setMinWidth(140);
        status.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getStatus()));
        status.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(AppointmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : new StatusChip(item));
                setText(null);
            }
        });

        TableColumn<Appointment, String> complaint = new TableColumn<>("Жалоба");
        complaint.setMinWidth(220);
        complaint.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getComplaint() == null ? "" : c.getValue().getComplaint()));

        table.getColumns().addAll(when, doctor, patient, status, complaint);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(440);
        table.setPlaceholder(TablePlaceholders.empty(
                "Приёмов пока нет",
                "Когда пациенты начнут записываться, записи появятся здесь."));

        refreshBtn.getStyleClass().add("button-outlined");
        cancelBtn.getStyleClass().add("button-danger");
        cancelBtn.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
                cancelBtn.setDisable(sel == null
                        || sel.getStatus() == AppointmentStatus.CANCELLED
                        || sel.getStatus() == AppointmentStatus.COMPLETED));

        refreshBtn.setOnAction(e -> reload());
        cancelBtn.setOnAction(e -> cancelSelected());

        HBox actions = new HBox(10, refreshBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, cancelBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, title, table, actions);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));

        reload();
    }

    private void reload() {
        Async.run(() -> window.getServices().appointments().mine(),
                list -> table.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void cancelSelected() {
        Appointment a = table.getSelectionModel().getSelectedItem();
        if (a == null) return;
        if (!Dialogs.confirm(window.getStage(), "Отмена",
                "Отменить запись на " + a.getStartsAt().format(DT) + "?")) return;
        Async.run(() -> window.getServices().appointments().cancel(a.getId()),
                upd -> reload(),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Все приёмы";
    }
}
