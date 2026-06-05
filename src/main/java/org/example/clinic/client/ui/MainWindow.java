package org.example.clinic.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.clinic.client.config.ClientConfig;
import org.example.clinic.client.service.Services;
import org.example.clinic.client.session.Session;
import org.example.clinic.client.ui.views.AdminDashboardView;
import org.example.clinic.client.ui.views.DoctorDashboardView;
import org.example.clinic.client.ui.views.LoginView;
import org.example.clinic.client.ui.views.PatientDashboardView;


public class MainWindow {

    private final Stage stage;
    private final Services services;

    private final BorderPane root;
    private final HBox topBar;
    private final Label titleLabel;
    private final Label userLabel;
    private final HBox topActions;
    private final VBox sideMenu;
    private final BorderPane center;

    public MainWindow(Stage stage, Services services) {
        this.stage = stage;
        this.services = services;

        titleLabel = new Label();
        titleLabel.getStyleClass().add("app-bar-title");

        userLabel = new Label();
        userLabel.getStyleClass().add("app-bar-user");

        topActions = new HBox(8);
        topActions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar = new HBox(12, titleLabel, spacer, userLabel, topActions);
        topBar.setPadding(new Insets(14, 24, 14, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("app-bar");

        sideMenu = new VBox(6);
        sideMenu.setPadding(new Insets(16, 8, 16, 8));
        sideMenu.getStyleClass().add("side-menu");
        sideMenu.setMinWidth(220);

        center = new BorderPane();
        center.getStyleClass().add("content-area");

        root = new BorderPane();
        root.setCenter(center);
        root.getStyleClass().add("app-root");

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(ClientConfig.get().getString("ui.title", "Запись к врачу"));
        stage.setMinWidth(960);
        stage.setMinHeight(640);
    }

    public Stage getStage() {
        return stage;
    }

    public Services getServices() {
        return services;
    }

    

    public void showFullScreen(View view) {
        root.setTop(null);
        root.setLeft(null);
        sideMenu.getChildren().clear();
        userLabel.setText("");
        topActions.getChildren().clear();
        center.setCenter(view.getRoot());
        titleLabel.setText(view.getTitle());
    }

    

    public void showAuthenticated(NavigationItem... items) {
        root.setTop(topBar);
        root.setLeft(sideMenu);

        userLabel.setText(Session.get().getFullName() + " (" + Session.get().getRole() + ")");

        topActions.getChildren().setAll(makeLogoutButton());

        sideMenu.getChildren().clear();
        for (NavigationItem item : items) {
            Button btn = makeMenuButton(item.title());
            btn.setOnAction(e -> {
                setActiveMenu(btn);
                setView(item.viewSupplier().get());
            });
            sideMenu.getChildren().add(btn);
        }
        
        if (items.length > 0) {
            Button first = (Button) sideMenu.getChildren().get(0);
            setActiveMenu(first);
            setView(items[0].viewSupplier().get());
        }
    }

    public void showLogin() {
        Session.get().clear();
        showFullScreen(new LoginView(this));
    }

    public void showRoleHome() {
        switch (Session.get().getRole()) {
            case PATIENT -> PatientDashboardView.installInto(this);
            case DOCTOR  -> DoctorDashboardView.installInto(this);
            case ADMIN   -> AdminDashboardView.installInto(this);
        }
    }

    private void setView(View view) {
        ScrollPane scroll = new ScrollPane(view.getRoot());
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("content-scroll");
        center.setCenter(scroll);
        titleLabel.setText(view.getTitle());
    }

    private Button makeMenuButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.getStyleClass().add("menu-button");
        return b;
    }

    private void setActiveMenu(Button button) {
        for (var n : sideMenu.getChildren()) {
            n.getStyleClass().remove("menu-button-active");
        }
        if (!button.getStyleClass().contains("menu-button-active")) {
            button.getStyleClass().add("menu-button-active");
        }
    }

    private Button makeLogoutButton() {
        Button btn = new Button("Выйти");
        btn.getStyleClass().add("app-bar-action");
        btn.setOnAction(e -> showLogin());
        return btn;
    }
}
