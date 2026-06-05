package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Specialization;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.TablePlaceholders;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

import java.util.Optional;

public class AdminSpecializationsView implements View {

    private final MainWindow window;
    private final VBox root;
    private final TableView<Specialization> table = new TableView<>();
    private final MFXButton addBtn = new MFXButton("Добавить");
    private final MFXButton editBtn = new MFXButton("Изменить");
    private final MFXButton deleteBtn = new MFXButton("Удалить");

    public AdminSpecializationsView(MainWindow window) {
        this.window = window;

        Label title = new Label("Специализации");
        title.getStyleClass().add("card-title");

        TableColumn<Specialization, String> name = new TableColumn<>("Название");
        name.setMinWidth(220);
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Specialization, String> desc = new TableColumn<>("Описание");
        desc.setMinWidth(400);
        desc.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(name, desc);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(TablePlaceholders.empty(
                "Специализаций пока нет",
                "Добавьте первую специализацию, чтобы врачи могли принимать пациентов."));

        addBtn.getStyleClass().add("button-primary");
        editBtn.getStyleClass().add("button-outlined");
        deleteBtn.getStyleClass().add("button-danger");

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            editBtn.setDisable(sel == null);
            deleteBtn.setDisable(sel == null);
        });

        addBtn.setOnAction(e -> openDialog(null));
        editBtn.setOnAction(e -> openDialog(table.getSelectionModel().getSelectedItem()));
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox actions = new HBox(10, addBtn, editBtn,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, title, table, actions);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));

        reload();
    }

    private void reload() {
        Async.run(() -> window.getServices().specializations().list(),
                list -> table.setItems(FXCollections.observableArrayList(list)),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void openDialog(Specialization edit) {
        MFXTextField name = new MFXTextField();
        name.setFloatingText("Название");
        name.setFloatMode(FloatMode.ABOVE);
        name.setPrefWidth(360);
        TextArea desc = new TextArea();
        desc.setPromptText("Краткое описание специализации");
        desc.setPrefRowCount(3);
        Label descLabel = new Label("Описание");
        descLabel.getStyleClass().add("field-label");
        VBox descBox = new VBox(4, descLabel, desc);

        if (edit != null) {
            name.setText(edit.getName());
            desc.setText(edit.getDescription());
        }
        VBox box = new VBox(20, name, descBox);
        box.setPrefWidth(420);

        Optional<javafx.scene.control.ButtonType> r = Dialogs.form(window.getStage(),
                edit == null ? "Новая специализация" : "Редактирование",
                null, box);
        if (r.isEmpty() || r.get() != javafx.scene.control.ButtonType.OK) return;
        if (name.getText() == null || name.getText().isBlank()) {
            Dialogs.info(window.getStage().getOwner(), "Внимание", "Название обязательно");
            return;
        }
        Async.run(() -> {
                    if (edit == null) {
                        return window.getServices().specializations().create(name.getText(), desc.getText());
                    } else {
                        return window.getServices().specializations().update(edit.getId(),
                                name.getText(), desc.getText());
                    }
                },
                saved -> reload(),
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void deleteSelected() {
        Specialization s = table.getSelectionModel().getSelectedItem();
        if (s == null) return;
        if (!Dialogs.confirm(window.getStage(), "Удаление",
                "Удалить специализацию \"" + s.getName() + "\"?")) return;
        Async.runVoid(() -> window.getServices().specializations().delete(s.getId()),
                this::reload,
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Специализации";
    }
}
