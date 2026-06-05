package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Doctor;
import org.example.clinic.client.model.ScheduleEntry;
import org.example.clinic.client.session.Session;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.TablePlaceholders;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;
import org.example.clinic.client.util.RussianDays;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;


public class DoctorScheduleView implements View {

    private final MainWindow window;
    private final VBox root;

    private final MFXComboBox<Doctor> doctorBox = new MFXComboBox<>();
    private final TableView<ScheduleEntry> table = new TableView<>();
    private final MFXButton addBtn = new MFXButton("Добавить / изменить");
    private final MFXButton deleteBtn = new MFXButton("Удалить");
    private final MFXButton refreshBtn = new MFXButton("Обновить");

    private Long currentDoctorId;
    private final boolean isAdmin;

    public DoctorScheduleView(MainWindow window) {
        this.window = window;
        this.isAdmin = Session.get().getRole() != null
                && "ADMIN".equals(Session.get().getRole().name());

        Label title = new Label("Расписание врача");
        title.getStyleClass().add("card-title");

        TableColumn<ScheduleEntry, String> day = new TableColumn<>("День");
        day.setMinWidth(140);
        day.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                RussianDays.display(c.getValue().getDayOfWeek())));

        TableColumn<ScheduleEntry, LocalTime> start = new TableColumn<>("Начало");
        start.setMinWidth(100);
        start.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        TableColumn<ScheduleEntry, LocalTime> end = new TableColumn<>("Окончание");
        end.setMinWidth(100);
        end.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        TableColumn<ScheduleEntry, Integer> slot = new TableColumn<>("Слот, мин");
        slot.setMinWidth(120);
        slot.setCellValueFactory(new PropertyValueFactory<>("slotMinutes"));

        table.getColumns().addAll(day, start, end, slot);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(360);
        table.setPlaceholder(TablePlaceholders.empty(
                "Расписание не заполнено",
                "Добавьте рабочие дни и длительность слотов приёма."));

        addBtn.getStyleClass().add("button-primary");
        deleteBtn.getStyleClass().add("button-danger");
        refreshBtn.getStyleClass().add("button-outlined");

        addBtn.setOnAction(e -> openEntryDialog());
        deleteBtn.setOnAction(e -> deleteSelected());
        refreshBtn.setOnAction(e -> reloadTable());

        deleteBtn.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) ->
                deleteBtn.setDisable(sel == null));

        VBox top;
        if (isAdmin) {
            doctorBox.setFloatingText("Врач");
            doctorBox.setFloatMode(FloatMode.ABOVE);
            doctorBox.setPrefWidth(400);
            doctorBox.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Doctor d) { return d == null ? "" : d.toString(); }
                public Doctor fromString(String s) { return null; }
            });
            doctorBox.valueProperty().addListener((obs, old, sel) -> {
                currentDoctorId = sel == null ? null : sel.getId();
                reloadTable();
            });
            Async.run(() -> window.getServices().doctors().list(),
                    list -> doctorBox.setItems(FXCollections.observableArrayList(list)),
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
            top = new VBox(10, doctorBox);
        } else {
            
            
            
            top = new VBox();
            
            Async.run(() -> window.getServices().doctors().list().stream()
                            .filter(d -> d.getUserId() != null
                                    && d.getUserId().equals(Session.get().getUserId()))
                            .findFirst().orElse(null),
                    doctor -> {
                        if (doctor != null) {
                            currentDoctorId = doctor.getId();
                            reloadTable();
                        } else {
                            Dialogs.error(window.getStage().getOwner(),
                                    "Ошибка", new RuntimeException("Профиль врача не найден"));
                        }
                    },
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
        }

        HBox actions = new HBox(10, refreshBtn, addBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(20, title, top, table, actions);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));
    }

    private void reloadTable() {
        if (currentDoctorId == null) {
            table.setItems(FXCollections.observableArrayList(List.of()));
            return;
        }
        final Long id = currentDoctorId;
        Async.run(() -> window.getServices().schedules().getDoctorSchedule(id),
                list -> table.setItems(FXCollections.observableArrayList(
                        list.stream()
                                .sorted(Comparator.comparingInt(e -> e.getDayOfWeek().getValue()))
                                .toList())),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void openEntryDialog() {
        if (currentDoctorId == null) return;

        MFXComboBox<DayOfWeek> dayBox = new MFXComboBox<>();
        dayBox.setItems(FXCollections.observableArrayList(DayOfWeek.values()));
        dayBox.setFloatingText("День недели");
        dayBox.setFloatMode(FloatMode.ABOVE);
        dayBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(DayOfWeek d) { return RussianDays.display(d); }
            public DayOfWeek fromString(String s) { return null; }
        });

        MFXTextField startField = new MFXTextField();
        startField.setFloatingText("Начало (HH:mm)");
        startField.setFloatMode(FloatMode.ABOVE);
        startField.setText("09:00");

        MFXTextField endField = new MFXTextField();
        endField.setFloatingText("Окончание (HH:mm)");
        endField.setFloatMode(FloatMode.ABOVE);
        endField.setText("17:00");

        MFXTextField slotField = new MFXTextField();
        slotField.setFloatingText("Длительность слота, мин");
        slotField.setFloatMode(FloatMode.ABOVE);
        slotField.setText("30");

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(22);
        g.add(dayBox, 0, 0, 2, 1);
        g.add(startField, 0, 1);
        g.add(endField, 1, 1);
        g.add(slotField, 0, 2);

        var result = Dialogs.form(window.getStage(),
                "Расписание", "Заполните параметры рабочего дня", g);
        if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) return;

        try {
            DayOfWeek day = dayBox.getValue();
            LocalTime start = LocalTime.parse(startField.getText());
            LocalTime end = LocalTime.parse(endField.getText());
            int slot = Integer.parseInt(slotField.getText());
            if (day == null) {
                Dialogs.info(window.getStage().getOwner(), "Внимание", "Выберите день недели");
                return;
            }
            final Long id = currentDoctorId;
            Async.run(() -> window.getServices().schedules().upsert(id, day, start, end, slot),
                    saved -> reloadTable(),
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
        } catch (Exception ex) {
            Dialogs.info(window.getStage().getOwner(), "Внимание",
                    "Проверьте формат полей: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        ScheduleEntry e = table.getSelectionModel().getSelectedItem();
        if (e == null) return;
        if (!Dialogs.confirm(window.getStage(), "Удаление",
                "Удалить запись расписания на " + RussianDays.display(e.getDayOfWeek()) + "?")) return;
        Async.runVoid(() -> window.getServices().schedules().delete(e.getId()),
                this::reloadTable,
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Расписание";
    }
}
