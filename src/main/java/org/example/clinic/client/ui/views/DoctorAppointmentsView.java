package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class DoctorAppointmentsView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;
    private final MFXDatePicker fromDate = new MFXDatePicker();
    private final MFXDatePicker toDate = new MFXDatePicker();
    private final TableView<Appointment> table = new TableView<>();
    private final MFXButton refreshBtn = new MFXButton("Обновить");
    private final MFXButton confirmBtn = new MFXButton("Подтвердить");
    private final MFXButton completeBtn = new MFXButton("Завершить + заключение");
    private final MFXButton missBtn = new MFXButton("Неявка");
    private final MFXButton cancelBtn = new MFXButton("Отменить");

    public DoctorAppointmentsView(MainWindow window) {
        this.window = window;

        Label title = new Label("Приёмы");
        title.getStyleClass().add("card-title");

        fromDate.setValue(LocalDate.now());
        toDate.setValue(LocalDate.now().plusDays(7));
        fromDate.setFloatingText("С");
        fromDate.setFloatMode(FloatMode.ABOVE);
        toDate.setFloatingText("По");
        toDate.setFloatMode(FloatMode.ABOVE);

        TableColumn<Appointment, String> when = new TableColumn<>("Когда");
        when.setMinWidth(150);
        when.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStartsAt().format(DT)));

        TableColumn<Appointment, String> patient = new TableColumn<>("Пациент");
        patient.setMinWidth(220);
        patient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPatientName()
                        + (c.getValue().getPatientPhone() == null ? ""
                            : " · " + c.getValue().getPatientPhone())));

        TableColumn<Appointment, String> complaint = new TableColumn<>("Жалоба");
        complaint.setMinWidth(240);
        complaint.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getComplaint() == null ? "" : c.getValue().getComplaint()));

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

        table.getColumns().addAll(when, patient, complaint, status);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(440);
        table.setPlaceholder(TablePlaceholders.empty(
                "Приёмов за выбранный период нет",
                "Измените даты или дождитесь новых записей пациентов."));

        styleAction(refreshBtn, "button-outlined");
        styleAction(confirmBtn, "button-primary");
        styleAction(completeBtn, "button-primary");
        styleAction(missBtn, "button-outlined");
        styleAction(cancelBtn, "button-danger");

        refreshBtn.setOnAction(e -> reload());
        confirmBtn.setOnAction(e -> changeStatus(this::confirm));
        completeBtn.setOnAction(e -> completeWithRecord());
        missBtn.setOnAction(e -> changeStatus(this::miss));
        cancelBtn.setOnAction(e -> changeStatus(this::cancelStatus));

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateActions(sel));
        updateActions(null);

        HBox topBar = new HBox(18, fromDate, toDate, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        HBox actionsRow = new HBox(10, confirmBtn, completeBtn, missBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, cancelBtn);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(20, title, topBar, table, actionsRow);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));

        reload();
    }

    private void styleAction(MFXButton b, String cls) {
        b.getStyleClass().add(cls);
    }

    
    private static VBox labeled(String label, javafx.scene.Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        VBox box = new VBox(4, l, control);
        return box;
    }

    private void updateActions(Appointment a) {
        boolean hasSel = a != null;
        confirmBtn.setDisable(!hasSel || a.getStatus() != AppointmentStatus.PLANNED);
        completeBtn.setDisable(!hasSel
                || a.getStatus() == AppointmentStatus.CANCELLED
                || a.getStatus() == AppointmentStatus.COMPLETED);
        missBtn.setDisable(!hasSel
                || (a.getStatus() != AppointmentStatus.PLANNED
                    && a.getStatus() != AppointmentStatus.CONFIRMED));
        cancelBtn.setDisable(!hasSel
                || a.getStatus() == AppointmentStatus.CANCELLED
                || a.getStatus() == AppointmentStatus.COMPLETED);
    }

    private void reload() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
        LocalDateTime toDt = to == null ? null : to.atTime(23, 59);
        Async.run(() -> window.getServices().appointments().doctorSchedule(null, fromDt, toDt),
                list -> table.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void changeStatus(java.util.function.Function<Appointment, Appointment> action) {
        Appointment a = table.getSelectionModel().getSelectedItem();
        if (a == null) return;
        Async.run(() -> action.apply(a),
                upd -> reload(),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private Appointment confirm(Appointment a) {
        return window.getServices().appointments().confirm(a.getId());
    }
    private Appointment miss(Appointment a) {
        return window.getServices().appointments().markMissed(a.getId());
    }
    private Appointment cancelStatus(Appointment a) {
        return window.getServices().appointments().cancel(a.getId());
    }

    private void completeWithRecord() {
        Appointment a = table.getSelectionModel().getSelectedItem();
        if (a == null) return;

        TextArea diagnosis = new TextArea();
        diagnosis.setPromptText("Например: ОРВИ, фарингит");
        diagnosis.setPrefRowCount(2);
        TextArea prescription = new TextArea();
        prescription.setPromptText("Лекарства, дозировка");
        prescription.setPrefRowCount(2);
        TextArea recommendations = new TextArea();
        recommendations.setPromptText("Режим, повторный приём и т. п.");
        recommendations.setPrefRowCount(2);
        MFXTextField notes = new MFXTextField();
        notes.setFloatingText("Заметки к приёму (опционально)");
        notes.setFloatMode(FloatMode.INLINE);
        notes.setPrefWidth(420);

        VBox content = new VBox(18,
                labeled("Диагноз *", diagnosis),
                labeled("Назначения", prescription),
                labeled("Рекомендации", recommendations),
                notes);
        content.setPrefWidth(520);

        Optional<javafx.scene.control.ButtonType> result = Dialogs.form(window.getStage(),
                "Завершение приёма", "Введите данные медицинского заключения",
                content);
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }
        if (diagnosis.getText() == null || diagnosis.getText().isBlank()) {
            Dialogs.info(window.getStage().getOwner(), "Внимание",
                    "Диагноз обязателен для медицинской записи.");
            return;
        }
        Async.run(() -> {
                    window.getServices().appointments().complete(a.getId(), notes.getText());
                    return window.getServices().records().add(a.getId(),
                            diagnosis.getText(), prescription.getText(), recommendations.getText());
                },
                rec -> {
                    reload();
                    Dialogs.info(window.getStage().getOwner(),
                            "Готово", "Приём завершён, медицинская запись сохранена.");
                },
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Приёмы";
    }
}
