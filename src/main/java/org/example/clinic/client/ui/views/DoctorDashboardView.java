package org.example.clinic.client.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.clinic.client.session.Session;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.NavigationItem;
import org.example.clinic.client.ui.View;

public class DoctorDashboardView implements View {

    private final VBox root;

    public DoctorDashboardView() {
        Label title = new Label("Здравствуйте, " + Session.get().getFullName() + "!");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Управляйте расписанием своих приёмов и медицинскими записями.");
        sub.getStyleClass().add("card-subtitle");
        sub.setWrapText(true);

        VBox card = new VBox(8, title, sub);
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
        return "Главная";
    }

    public static void installInto(MainWindow window) {
        window.showAuthenticated(
                new NavigationItem("Главная",             DoctorDashboardView::new),
                new NavigationItem("Приёмы на сегодня",   () -> new DoctorAppointmentsView(window)),
                new NavigationItem("Моё расписание",      () -> new DoctorScheduleView(window)),
                new NavigationItem("Выданные заключения", () -> new DoctorMedicalHistoryView(window))
        );
    }
}
