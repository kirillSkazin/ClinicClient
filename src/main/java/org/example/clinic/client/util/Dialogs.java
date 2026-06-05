package org.example.clinic.client.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.clinic.client.network.ApiException;

import java.util.Optional;

public final class Dialogs {

    private Dialogs() {
    }

    public static void info(Window owner, String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void error(Window owner, String title, Throwable ex) {
        String message = ex.getMessage();
        if (ex instanceof ApiException api) {
            message = "[" + api.getCode() + "] " + message;
        }
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(owner);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static boolean confirm(Window owner, String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                message, ButtonType.OK, ButtonType.CANCEL);
        a.initOwner(owner);
        a.setTitle(title);
        a.setHeaderText(null);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    
    public static Optional<ButtonType> form(Window owner, String title, String header, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        
        
        
        VBox padded = new VBox(content);
        padded.setPadding(new Insets(22, 16, 12, 16));
        padded.setMinWidth(460);
        padded.setFillWidth(true);

        dialog.getDialogPane().setContent(padded);
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialog.showAndWait();
    }

    
    public static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(12));
        return g;
    }

    public static Label label(String text) {
        Label l = new Label(text);
        l.setMinWidth(120);
        return l;
    }
}
