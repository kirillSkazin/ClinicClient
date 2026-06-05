package org.example.clinic.client.ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Appointment;
import org.example.clinic.client.session.Session;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.NavigationItem;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDashboardView implements View {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final MainWindow window;
    private final VBox root;

    public PatientDashboardView(MainWindow window) {
        this.window = window;

        Label hello = new Label("Здравствуйте, " + Session.get().getFullName() + "!");
        hello.getStyleClass().add("card-title");
        Label sub = new Label("Это ваш личный кабинет. Здесь вы можете записаться на приём, "
                + "посмотреть историю и медицинскую карту.");
        sub.getStyleClass().add("card-subtitle");
        sub.setWrapText(true);

        VBox welcome = new VBox(8, hello, sub);
        welcome.getStyleClass().add("card");

        Label upcomingTitle = new Label("Ваши предстоящие приёмы");
        upcomingTitle.getStyleClass().add("section-title");

        VBox upcomingBox = new VBox(8);
        upcomingBox.getStyleClass().add("card");
        upcomingBox.setMaxWidth(Double.MAX_VALUE);

        Label loading = new Label("Загрузка...");
        loading.getStyleClass().add("label-muted");
        upcomingBox.getChildren().setAll(upcomingTitle, loading);

        Async.run(
                () -> window.getServices().appointments().upcomingForPatient(),
                list -> renderUpcoming(upcomingBox, list, upcomingTitle),
                err -> upcomingBox.getChildren().setAll(upcomingTitle, error(err.getMessage())));

        root = new VBox(20, welcome, upcomingBox);
        root.setPadding(new Insets(24));
    }

    private void renderUpcoming(VBox box, List<Appointment> list, Label title) {
        box.getChildren().clear();
        box.getChildren().add(title);
        if (list.isEmpty()) {
            Label empty = new Label("У вас пока нет запланированных приёмов.");
            empty.getStyleClass().add("label-muted");
            box.getChildren().add(empty);
            return;
        }
        for (Appointment a : list) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 4, 10, 4));

            Label when = new Label(a.getStartsAt().format(DT));
            when.getStyleClass().add("section-title");
            when.setMinWidth(150);

            Label doctor = new Label(a.getDoctorName() + "  ·  " + a.getSpecialization());
            doctor.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(doctor, Priority.ALWAYS);

            Label room = new Label("Кабинет: "
                    + (a.getRoomNumber() == null ? "—" : a.getRoomNumber()));
            room.getStyleClass().add("label-muted");

            row.getChildren().addAll(when, doctor, room, new StatusChip(a.getStatus()));
            box.getChildren().add(row);
            Region divider = new Region();
            divider.getStyleClass().add("divider");
            box.getChildren().add(divider);
        }
    }

    private Label error(String message) {
        Label l = new Label("Не удалось загрузить: " + message);
        l.getStyleClass().add("label-muted");
        return l;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Главная";
    }

    public static void installInto(MainWindow window) {
        window.showAuthenticated(
                new NavigationItem("Главная",       () -> new PatientDashboardView(window)),
                new NavigationItem("Записаться",    () -> new PatientBookingView(window)),
                new NavigationItem("Мои приёмы",    () -> new PatientAppointmentsView(window)),
                new NavigationItem("Медкарта",      () -> new PatientMedicalHistoryView(window))
        );
    }
}
