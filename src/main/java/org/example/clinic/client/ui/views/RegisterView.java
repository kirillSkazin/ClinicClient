package org.example.clinic.client.ui.views;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.clinic.client.ui.MainWindow;
import org.example.clinic.client.ui.View;
import org.example.clinic.client.util.Async;
import org.example.clinic.client.util.Dialogs;

public class RegisterView implements View {

    private final MainWindow window;
    private final StackPane root;

    public RegisterView(MainWindow window) {
        this.window = window;

        Label title = new Label("Регистрация пациента");
        title.getStyleClass().add("login-title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        MFXTextField username = floating("Логин");
        MFXPasswordField password = passwordField("Пароль");
        MFXTextField email = floating("Email");
        MFXTextField fullName = floating("ФИО");
        MFXTextField phone = floating("Телефон");
        MFXDatePicker birthDate = new MFXDatePicker();
        birthDate.setFloatingText("Дата рождения");
        birthDate.setFloatMode(FloatMode.ABOVE);
        birthDate.setPrefWidth(340);
        MFXTextField address = floating("Адрес");
        MFXTextField insurance = floating("Номер полиса");

        GridPane grid = new GridPane();
        grid.setHgap(34);
        grid.setVgap(30);
        grid.setAlignment(Pos.CENTER);
        grid.add(username, 0, 0);     grid.add(password, 1, 0);
        grid.add(email, 0, 1);        grid.add(fullName, 1, 1);
        grid.add(phone, 0, 2);        grid.add(birthDate, 1, 2);
        grid.add(address, 0, 3);      grid.add(insurance, 1, 3);
        VBox.setMargin(grid, new Insets(34, 0, 0, 0));

        MFXButton registerBtn = new MFXButton("Создать аккаунт");
        registerBtn.getStyleClass().add("button-primary");

        MFXButton backBtn = new MFXButton("Назад ко входу");
        backBtn.getStyleClass().add("button-flat");
        backBtn.setOnAction(e -> window.showLogin());

        Label status = new Label();
        status.getStyleClass().add("label-muted");
        status.setMaxWidth(Double.MAX_VALUE);
        status.setAlignment(Pos.CENTER);

        registerBtn.setOnAction(e -> {
            if (isBlank(username) || isBlank(password) || isBlank(email)
                    || isBlank(fullName)) {
                status.setText("Заполните обязательные поля: логин, пароль, email, ФИО");
                return;
            }
            registerBtn.setDisable(true);
            status.setText("Регистрация...");
            Async.run(
                    () -> window.getServices().auth().registerPatient(
                            username.getText().trim(),
                            password.getText(),
                            email.getText().trim(),
                            fullName.getText().trim(),
                            phone.getText(),
                            birthDate.getValue(),
                            address.getText(),
                            insurance.getText()),
                    resp -> {
                        registerBtn.setDisable(false);
                        status.setText("");
                        window.showRoleHome();
                    },
                    err -> {
                        registerBtn.setDisable(false);
                        status.setText("");
                        Dialogs.error(window.getStage().getOwner(), "Не удалось зарегистрироваться", err);
                    });
        });

        HBox actions = new HBox(14, backBtn, registerBtn);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(24, title, grid, actions, status);
        card.getStyleClass().addAll("login-card", "register-card");
        card.setMinWidth(820);
        card.setMaxWidth(860);
        card.setAlignment(Pos.CENTER);

        root = new StackPane(card);
        root.getStyleClass().add("login-screen");
        root.setPadding(new Insets(30));
    }

    private static MFXTextField floating(String label) {
        MFXTextField f = new MFXTextField();
        f.setFloatingText(label);
        f.setFloatMode(FloatMode.ABOVE);
        f.setPrefWidth(340);
        return f;
    }

    private static MFXPasswordField passwordField(String label) {
        MFXPasswordField f = new MFXPasswordField();
        f.setFloatingText(label);
        f.setFloatMode(FloatMode.ABOVE);
        f.setPrefWidth(340);
        return f;
    }

    private static boolean isBlank(MFXTextField f) {
        return f.getText() == null || f.getText().isBlank();
    }

    private static boolean isBlank(MFXPasswordField f) {
        return f.getText() == null || f.getText().isBlank();
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public String getTitle() {
        return "Регистрация";
    }
}
