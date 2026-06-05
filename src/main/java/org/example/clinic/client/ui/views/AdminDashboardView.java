package org.example.clinic.client.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.clinic.client.model.Statistics;
import org.example.clinic.client.session.Session;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.NavigationItem;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

public class AdminDashboardView implements View {

    private final MainWindow window;
    private final VBox root;
    private final FlowPane metrics = new FlowPane(14, 14);

    public AdminDashboardView(MainWindow window) {
        this.window = window;

        Label title = new Label("Панель администратора");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Здравствуйте, " + Session.get().getFullName()
                + "! Здесь сводная статистика по системе.");
        sub.getStyleClass().add("card-subtitle");
        sub.setWrapText(true);

        VBox header = new VBox(8, title, sub);
        header.getStyleClass().add("card");

        VBox metricsCard = new VBox(10, sectionLabel("Текущая статистика"), metrics);
        metricsCard.getStyleClass().add("card");

        root = new VBox(20, header, metricsCard);
        root.setPadding(new Insets(24));

        loadStats();
    }

    private void loadStats() {
        Async.run(() -> window.getServices().statistics().summary(),
                this::renderMetrics,
                err -> Dialogs.error(window.getStage().getOwner(), "Ошибка", err));
    }

    private void renderMetrics(Statistics s) {
        metrics.getChildren().setAll(
                metric("Пользователей", s.getTotalUsers()),
                metric("Врачей",        s.getTotalDoctors()),
                metric("Пациентов",     s.getTotalPatients()),
                metric("Всего приёмов", s.getTotalAppointments()),
                metric("Запланировано", s.getPlannedAppointments()),
                metric("Подтверждено",  s.getConfirmedAppointments()),
                metric("Завершено",     s.getCompletedAppointments()),
                metric("Отменено",      s.getCancelledAppointments()),
                metric("Не явились",    s.getMissedAppointments()),
                metric("Напоминаний в очереди", s.getPendingReminders())
        );
    }

    private VBox metric(String label, long value) {
        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("metric-value");
        Label l = new Label(label);
        l.getStyleClass().add("metric-label");
        l.setWrapText(true);
        VBox card = new VBox(6, v, l);
        card.getStyleClass().add("metric-card");
        return card;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Панель администратора";
    }

    public static void installInto(MainWindow window) {
        window.showAuthenticated(
                new NavigationItem("Дашборд",         () -> new AdminDashboardView(window)),
                new NavigationItem("Специализации",   () -> new AdminSpecializationsView(window)),
                new NavigationItem("Врачи",           () -> new AdminDoctorsView(window)),
                new NavigationItem("Расписания",      () -> new DoctorScheduleView(window)),
                new NavigationItem("Все приёмы",      () -> new AdminAppointmentsView(window))
        );
    }
}
