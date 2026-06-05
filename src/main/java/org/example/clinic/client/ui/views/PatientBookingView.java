package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Doctor;
import org.example.clinic.client.model.Specialization;
import org.example.clinic.client.model.TimeSlot;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class PatientBookingView implements View {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final MainWindow window;
    private final VBox root;

    private final MFXComboBox<Specialization> specializationBox = new MFXComboBox<>();
    private final MFXComboBox<Doctor> doctorBox = new MFXComboBox<>();
    private final MFXDatePicker datePicker = new MFXDatePicker();
    private final ListView<TimeSlot> slotList = new ListView<>();
    private final MFXTextField complaint = new MFXTextField();
    private final MFXButton bookBtn = new MFXButton("Записаться");

    public PatientBookingView(MainWindow window) {
        this.window = window;

        Label title = new Label("Запись на приём");
        title.getStyleClass().add("card-title");

        specializationBox.setFloatingText("Специализация");
        specializationBox.setFloatMode(FloatMode.ABOVE);
        specializationBox.setPrefWidth(280);
        specializationBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Specialization s) { return s == null ? "" : s.getName(); }
            public Specialization fromString(String s) { return null; }
        });

        doctorBox.setFloatingText("Врач");
        doctorBox.setFloatMode(FloatMode.ABOVE);
        doctorBox.setPrefWidth(360);
        doctorBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Doctor d) { return d == null ? "" : d.getFullName(); }
            public Doctor fromString(String s) { return null; }
        });

        datePicker.setFloatingText("Дата");
        datePicker.setFloatMode(FloatMode.ABOVE);
        datePicker.setValue(LocalDate.now());

        complaint.setFloatingText("Жалоба / причина обращения");
        complaint.setFloatMode(FloatMode.ABOVE);
        complaint.setPrefWidth(620);

        bookBtn.getStyleClass().add("button-primary");
        bookBtn.setDisable(true);

        slotList.setPrefHeight(280);
        slotList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TimeSlot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item.getStartsAt().format(TIME_FMT)
                            + "  ·  " + item.getDurationMinutes() + " мин"
                            + (item.isAvailable() ? "" : "  ·  занято"));
                    setDisable(!item.isAvailable());
                }
            }
        });

        
        Label placeholder = new Label("Выберите врача и дату");
        placeholder.getStyleClass().add("label-muted");
        slotList.setPlaceholder(placeholder);

        
        Async.run(() -> window.getServices().specializations().list(),
                list -> specializationBox.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));

        specializationBox.valueProperty().addListener((obs, old, sel) -> {
            doctorBox.getItems().clear();
            doctorBox.setValue(null);
            slotList.getItems().clear();
            if (sel == null) return;
            Async.run(() -> window.getServices().doctors().bySpecialization(sel.getId()),
                    list -> doctorBox.setItems(FXCollections.observableArrayList(list)),
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
        });

        Runnable refreshSlots = () -> {
            Doctor d = doctorBox.getValue();
            LocalDate date = datePicker.getValue();
            slotList.getItems().clear();
            bookBtn.setDisable(true);
            if (d == null || date == null) {
                return;
            }
            Async.run(() -> window.getServices().schedules().availableSlots(d.getId(), date),
                    slots -> slotList.setItems(FXCollections.observableArrayList(slots)),
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
        };
        doctorBox.valueProperty().addListener((obs, old, sel) -> refreshSlots.run());
        datePicker.valueProperty().addListener((obs, old, sel) -> refreshSlots.run());

        slotList.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
                bookBtn.setDisable(sel == null || !sel.isAvailable()));

        bookBtn.setOnAction(e -> {
            TimeSlot slot = slotList.getSelectionModel().getSelectedItem();
            Doctor d = doctorBox.getValue();
            if (slot == null || d == null) return;
            bookBtn.setDisable(true);
            Async.run(() -> window.getServices().appointments().book(
                            d.getId(), slot.getStartsAt(), slot.getDurationMinutes(),
                            complaint.getText()),
                    appointment -> {
                        Dialogs.info(window.getStage().getOwner(), "Запись создана",
                                "Вы записаны на " + appointment.getStartsAt() + " к "
                                        + appointment.getDoctorName());
                        refreshSlots.run();
                        complaint.clear();
                    },
                    err -> {
                        bookBtn.setDisable(false);
                        Dialogs.error(window.getStage().getOwner(), "Не удалось записаться", err);
                    });
        });

        HBox row1 = new HBox(18, specializationBox, doctorBox, datePicker);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label slotsLabel = new Label("Доступные слоты:");
        slotsLabel.getStyleClass().add("section-title");

        HBox actions = new HBox(12, complaint,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, bookBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(22, title, row1, slotsLabel, slotList, actions);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Записаться на приём";
    }
}
