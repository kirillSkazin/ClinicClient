package org.example.clinic.client.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.MedicalRecord;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;

import java.time.format.DateTimeFormatter;
import java.util.List;


public class PatientMedicalHistoryView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;
    private final VBox listBox;

    public PatientMedicalHistoryView(MainWindow window) {
        this.window = window;

        Label title = new Label("Медицинская карта");
        title.getStyleClass().add("card-title");

        Label sub = new Label("Заключения врачей по вашим приёмам");
        sub.getStyleClass().add("card-subtitle");

        listBox = new VBox(12);

        Async.run(() -> window.getServices().records().mine(),
                this::render,
                err -> render(List.of()));

        VBox card = new VBox(14, title, sub, listBox);
        card.getStyleClass().add("card");

        root = new VBox(20, card);
        root.setPadding(new Insets(24));
    }

    private void render(List<MedicalRecord> list) {
        listBox.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Записей пока нет.");
            empty.getStyleClass().add("label-muted");
            listBox.getChildren().add(empty);
            return;
        }
        for (MedicalRecord r : list) {
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 14;");

            Label header = new Label(r.getAppointmentDate().format(DT)
                    + "  ·  " + r.getDoctorName()
                    + " (" + r.getSpecialization() + ")");
            header.getStyleClass().add("section-title");

            card.getChildren().add(header);
            card.getChildren().add(field("Диагноз", r.getDiagnosis()));
            if (notBlank(r.getPrescription())) {
                card.getChildren().add(field("Назначения", r.getPrescription()));
            }
            if (notBlank(r.getRecommendations())) {
                card.getChildren().add(field("Рекомендации", r.getRecommendations()));
            }
            listBox.getChildren().add(card);
        }
    }

    private VBox field(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        Label v = new Label(value);
        v.setWrapText(true);
        return new VBox(2, l, v);
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
        return "Медкарта";
    }
}
