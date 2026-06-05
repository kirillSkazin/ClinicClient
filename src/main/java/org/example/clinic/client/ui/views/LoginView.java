package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

public class LoginView implements View {

    private final MainWindow window;
    private final StackPane root;
    private final MFXTextField username;
    private final MFXPasswordField password;
    private final MFXButton signIn;
    private final MFXButton goRegister;
    private final Label statusLabel;

    public LoginView(MainWindow window) {
        this.window = window;

        Label title = new Label("Запись к врачу");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Войдите в систему, чтобы продолжить");
        subtitle.getStyleClass().add("login-subtitle");

        username = new MFXTextField();
        username.setFloatingText("Логин");
        username.setFloatMode(FloatMode.ABOVE);
        username.setPrefWidth(320);

        password = new MFXPasswordField();
        password.setFloatingText("Пароль");
        password.setFloatMode(FloatMode.ABOVE);
        password.setPrefWidth(320);

        signIn = new MFXButton("Войти");
        signIn.getStyleClass().add("button-primary");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setOnAction(e -> doLogin());

        goRegister = new MFXButton("Зарегистрироваться как пациент");
        goRegister.getStyleClass().add("button-flat");
        goRegister.setOnAction(e -> window.showFullScreen(new RegisterView(window)));

        statusLabel = new Label();
        statusLabel.getStyleClass().add("label-muted");

        password.setOnAction(e -> doLogin());

        VBox card = new VBox(26, title, subtitle,
                new Region() {{ setMinHeight(8); }},
                username, password, signIn, goRegister, statusLabel);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);

        root = new StackPane(card);
        root.getStyleClass().add("login-screen");
        root.setPadding(new Insets(40));
    }

    private void doLogin() {
        String u = username.getText() == null ? "" : username.getText().trim();
        String p = password.getText() == null ? "" : password.getText();
        if (u.isEmpty() || p.isEmpty()) {
            statusLabel.setText("Введите логин и пароль");
            return;
        }
        signIn.setDisable(true);
        statusLabel.setText("Подключение к серверу...");
        Async.run(
                () -> window.getServices().auth().login(u, p),
                resp -> {
                    signIn.setDisable(false);
                    statusLabel.setText("");
                    window.showRoleHome();
                },
                err -> {
                    signIn.setDisable(false);
                    statusLabel.setText("");
                    Dialogs.error(window.getStage().getOwner(), "Не удалось войти", err);
                });
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Вход";
    }
}
