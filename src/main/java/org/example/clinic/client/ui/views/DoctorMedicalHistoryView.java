package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.MedicalRecord;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

import java.time.format.DateTimeFormatter;
import java.util.List;


public class DoctorMedicalHistoryView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;
    private final VBox listBox = new VBox(12);
    private final MFXTextField search = new MFXTextField();

    private List<MedicalRecord> allRecords = List.of();

    public DoctorMedicalHistoryView(MainWindow window) {
        this.window = window;

        Label title = new Label("Выданные заключения");
        title.getStyleClass().add("card-title");

        Label sub = new Label("История медицинских записей, выданных вами по итогам приёмов");
        sub.getStyleClass().add("card-subtitle");

        search.setFloatingText("Фильтр по ФИО пациента или диагнозу");

        search.setFloatMode(FloatMode.ABOVE);
        search.setPrefWidth(360);
        search.textProperty().addListener((obs, old, val) -> renderFiltered());

        MFXButton refreshBtn = new MFXButton("Обновить");
        refreshBtn.getStyleClass().add("button-outlined");
        refreshBtn.setOnAction(e -> reload());

        HBox topBar = new HBox(18, search,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(20, title, sub, topBar, listBox);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));

        reload();
    }

    private void reload() {
        listBox.getChildren().setAll(loadingLabel());
        Async.run(() -> window.getServices().records().myIssued(),
                list -> {
                    allRecords = list == null ? List.of() : list;
                    renderFiltered();
                },
                err -> {
                    allRecords = List.of();
                    listBox.getChildren().setAll(errorLabel(err.getMessage()));
                    Dialogs.error(window.getStage().getOwner(), "Ошибка", err);
                });
    }

    private void renderFiltered() {
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        List<MedicalRecord> filtered = query.isEmpty()
                ? allRecords
                : allRecords.stream()
                        .filter(r -> matches(r, query))
                        .toList();
        renderList(filtered);
    }

    private boolean matches(MedicalRecord r, String query) {
        return contains(r.getPatientName(), query)
                || contains(r.getDiagnosis(), query);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void renderList(List<MedicalRecord> list) {
        listBox.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label(allRecords.isEmpty()
                    ? "Вы пока не выдавали заключений."
                    : "Под фильтр ничего не подошло.");
            empty.getStyleClass().add("label-muted");
            listBox.getChildren().add(empty);
            return;
        }
        for (MedicalRecord r : list) {
            listBox.getChildren().add(recordCard(r));
        }
    }

    private VBox recordCard(MedicalRecord r) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 14;");

        String patient = r.getPatientName() == null ? "пациент не указан" : r.getPatientName();
        Label header = new Label(r.getAppointmentDate().format(DT) + "  ·  " + patient);
        header.getStyleClass().add("section-title");
        card.getChildren().add(header);

        card.getChildren().add(field("Диагноз", r.getDiagnosis()));
        if (notBlank(r.getPrescription())) {
            card.getChildren().add(field("Назначения", r.getPrescription()));
        }
        if (notBlank(r.getRecommendations())) {
            card.getChildren().add(field("Рекомендации", r.getRecommendations()));
        }
        return card;
    }

    private VBox field(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        Label v = new Label(value);
        v.setWrapText(true);
        return new VBox(2, l, v);
    }

    private Label loadingLabel() {
        Label l = new Label("Загрузка...");
        l.getStyleClass().add("label-muted");
        return l;
    }

    private Label errorLabel(String message) {
        Label l = new Label("Не удалось загрузить: " + message);
        l.getStyleClass().add("label-muted");
        return l;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Выданные заключения";
    }
}
