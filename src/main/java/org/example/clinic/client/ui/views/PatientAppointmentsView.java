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


public class PatientAppointmentsView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;
    private final TableView<Appointment> table = new TableView<>();
    private final MFXButton cancelBtn = new MFXButton("Отменить");
    private final MFXButton refreshBtn = new MFXButton("Обновить");

    public PatientAppointmentsView(MainWindow window) {
        this.window = window;

        Label title = new Label("Мои записи");
        title.getStyleClass().add("card-title");

        TableColumn<Appointment, String> when = new TableColumn<>("Когда");
        when.setMinWidth(150);
        when.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartsAt().format(DT)));

        TableColumn<Appointment, String> doctor = new TableColumn<>("Врач");
        doctor.setMinWidth(220);
        doctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDoctorName() + " (" + c.getValue().getSpecialization() + ")"));

        TableColumn<Appointment, String> room = new TableColumn<>("Кабинет");
        room.setMinWidth(80);
        room.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRoomNumber() == null ? "—" : c.getValue().getRoomNumber()));

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

        table.getColumns().addAll(when, doctor, room, status, complaint);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(TablePlaceholders.empty(
                "Записей пока нет",
                "Выберите врача и свободное время на экране записи."));

        cancelBtn.getStyleClass().add("button-danger");
        cancelBtn.setDisable(true);
        cancelBtn.setOnAction(e -> cancelSelected());

        refreshBtn.getStyleClass().add("button-outlined");
        refreshBtn.setOnAction(e -> reload());

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
                cancelBtn.setDisable(sel == null
                        || sel.getStatus() == AppointmentStatus.CANCELLED
                        || sel.getStatus() == AppointmentStatus.COMPLETED));

        HBox actions = new HBox(12, refreshBtn, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, cancelBtn);
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
        if (!Dialogs.confirm(window.getStage(), "Отмена записи",
                "Вы уверены, что хотите отменить запись на " + a.getStartsAt().format(DT) + "?")) {
            return;
        }
        cancelBtn.setDisable(true);
        Async.run(() -> window.getServices().appointments().cancel(a.getId()),
                upd -> reload(),
                err -> {
                    cancelBtn.setDisable(false);
                    Dialogs.error(window.getStage().getOwner(), "Ошибка", err);
                });
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Мои приёмы";
    }
}
