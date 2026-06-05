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
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Doctor;
import org.example.clinic.client.model.Specialization;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.TablePlaceholders;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

import java.util.Optional;

public class AdminDoctorsView implements View {

    private static final double DOCTOR_FORM_FIELD_WIDTH = 420;
    private static final double DOCTOR_FORM_COLUMN_GAP = 28;

    private final MainWindow window;
    private final VBox root;
    private final TableView<Doctor> table = new TableView<>();
    private final MFXTextField search = new MFXTextField();
    private final MFXButton searchBtn = new MFXButton("Найти");
    private final MFXButton refreshBtn = new MFXButton("Обновить");
    private final MFXButton addBtn = new MFXButton("Добавить врача");
    private final MFXButton editBtn = new MFXButton("Изменить");
    private final MFXButton deleteBtn = new MFXButton("Деактивировать");

    public AdminDoctorsView(MainWindow window) {
        this.window = window;

        Label title = new Label("Врачи");
        title.getStyleClass().add("card-title");

        TableColumn<Doctor, String> fullName = new TableColumn<>("ФИО");
        fullName.setMinWidth(240);
        fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Doctor, String> spec = new TableColumn<>("Специализация");
        spec.setMinWidth(180);
        spec.setCellValueFactory(new PropertyValueFactory<>("specializationName"));

        TableColumn<Doctor, String> room = new TableColumn<>("Кабинет");
        room.setMinWidth(80);
        room.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));

        TableColumn<Doctor, Integer> exp = new TableColumn<>("Стаж");
        exp.setMinWidth(80);
        exp.setCellValueFactory(new PropertyValueFactory<>("experienceYears"));

        TableColumn<Doctor, String> email = new TableColumn<>("Email");
        email.setMinWidth(220);
        email.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Doctor, Boolean> active = new TableColumn<>("Активен");
        active.setMinWidth(80);
        active.setCellValueFactory(new PropertyValueFactory<>("active"));

        table.getColumns().addAll(fullName, spec, room, exp, email, active);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(TablePlaceholders.empty(
                "Врачи не найдены",
                "Добавьте врача или измените поисковый запрос."));

        search.setFloatingText("Поиск по ФИО");
        search.setFloatMode(FloatMode.ABOVE);
        search.setPrefWidth(280);
        searchBtn.getStyleClass().add("button-outlined");
        refreshBtn.getStyleClass().add("button-outlined");
        addBtn.getStyleClass().add("button-primary");
        editBtn.getStyleClass().add("button-outlined");
        deleteBtn.getStyleClass().add("button-danger");

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            editBtn.setDisable(sel == null);
            deleteBtn.setDisable(sel == null);
        });

        searchBtn.setOnAction(e -> doSearch());
        search.setOnAction(e -> doSearch());
        refreshBtn.setOnAction(e -> reload());
        addBtn.setOnAction(e -> openDialog(null));
        editBtn.setOnAction(e -> openDialog(table.getSelectionModel().getSelectedItem()));
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox topRow = new HBox(10, search, searchBtn, refreshBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, addBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox actionsRow = new HBox(10, editBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, deleteBtn);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(20, title, topRow, table, actionsRow);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));

        reload();
    }

    private void reload() {
        Async.run(() -> window.getServices().doctors().list(),
                list -> table.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void doSearch() {
        String q = search.getText();
        Async.run(() -> window.getServices().doctors().search(q),
                list -> table.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void openDialog(Doctor edit) {
        MFXTextField username = floating("Логин");
        MFXTextField password = floating("Пароль" + (edit == null ? "" : " (оставьте пустым, чтобы не менять)"));
        MFXTextField email = floating("Email");
        MFXTextField fullName = floating("ФИО");
        MFXTextField phone = floating("Телефон");
        MFXTextField roomNumber = floating("Кабинет");
        MFXTextField experience = floating("Стаж (лет)");
        TextArea bio = new TextArea();
        bio.setPromptText("Краткая биография / опыт");
        bio.setPrefRowCount(3);
        bio.setPrefWidth(DOCTOR_FORM_FIELD_WIDTH * 2 + DOCTOR_FORM_COLUMN_GAP);
        Label bioLabel = new Label("Биография");
        bioLabel.getStyleClass().add("field-label");
        VBox bioBox = new VBox(4, bioLabel, bio);
        bioBox.setPrefWidth(DOCTOR_FORM_FIELD_WIDTH * 2 + DOCTOR_FORM_COLUMN_GAP);

        MFXComboBox<Specialization> spec = new MFXComboBox<>();
        spec.setFloatingText("Специализация");
        spec.setFloatMode(FloatMode.ABOVE);
        spec.setPrefWidth(DOCTOR_FORM_FIELD_WIDTH);
        spec.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Specialization s) { return s == null ? "" : s.getName(); }
            public Specialization fromString(String s) { return null; }
        });
        Async.run(() -> window.getServices().specializations().list(),
                list -> {
                    spec.setItems(FXCollections.observableArrayList(list));
                    if (edit != null) {
                        list.stream().filter(s -> s.getId().equals(edit.getSpecializationId()))
                                .findFirst().ifPresent(spec::setValue);
                    }
                },
                err -> {   });

        if (edit != null) {
            username.setText(edit.getUserId() == null ? "" : "");
            username.setDisable(true);
            email.setText(edit.getEmail());
            fullName.setText(edit.getFullName());
            phone.setText(edit.getPhone());
            roomNumber.setText(edit.getRoomNumber());
            if (edit.getExperienceYears() != null) experience.setText(edit.getExperienceYears().toString());
            bio.setText(edit.getBio());
        }

        GridPane g = new GridPane();
        g.setHgap(DOCTOR_FORM_COLUMN_GAP);
        g.setVgap(22);
        g.setAlignment(Pos.CENTER);
        g.add(username, 0, 0);    g.add(password, 1, 0);
        g.add(email, 0, 1);       g.add(fullName, 1, 1);
        g.add(phone, 0, 2);       g.add(spec, 1, 2);
        g.add(roomNumber, 0, 3);  g.add(experience, 1, 3);
        g.add(bioBox, 0, 4, 2, 1);

        Optional<javafx.scene.control.ButtonType> r = Dialogs.form(window.getStage(),
                edit == null ? "Новый врач" : "Редактирование", null, g);
        if (r.isEmpty() || r.get() != javafx.scene.control.ButtonType.OK) return;

        try {
            Integer expYears = experience.getText() == null || experience.getText().isBlank()
                    ? null : Integer.parseInt(experience.getText());
            Long specId = spec.getValue() == null ? null : spec.getValue().getId();
            Async.run(() -> {
                        if (edit == null) {
                            return window.getServices().doctors().create(
                                    username.getText(), password.getText(), email.getText(),
                                    fullName.getText(), phone.getText(), specId,
                                    roomNumber.getText(), expYears, bio.getText());
                        } else {
                            return window.getServices().doctors().update(edit.getId(),
                                    email.getText(), fullName.getText(), phone.getText(),
                                    specId, roomNumber.getText(), expYears, bio.getText());
                        }
                    },
                    saved -> reload(),
                    err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
        } catch (NumberFormatException nfe) {
            Dialogs.info(window.getStage().getOwner(), "Внимание",
                    "Стаж должен быть числом");
        }
    }

    private static MFXTextField floating(String label) {
        MFXTextField f = new MFXTextField();
        f.setFloatingText(label);
        f.setFloatMode(FloatMode.ABOVE);
        f.setPrefWidth(DOCTOR_FORM_FIELD_WIDTH);
        return f;
    }

    private void deleteSelected() {
        Doctor d = table.getSelectionModel().getSelectedItem();
        if (d == null) return;
        if (!Dialogs.confirm(window.getStage(), "Деактивация",
                "Деактивировать врача " + d.getFullName() + "?")) return;
        Async.runVoid(() -> window.getServices().doctors().delete(d.getId()),
                this::reload,
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Врачи";
    }
}
