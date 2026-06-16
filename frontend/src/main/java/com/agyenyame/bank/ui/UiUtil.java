package com.agyenyame.bank.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Small UI helpers shared across views. */
final class UiUtil {
    private UiUtil() {}

    static VBox card(String title) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(28));
        box.setAlignment(Pos.TOP_CENTER);
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        box.getChildren().add(heading);
        return box;
    }

    static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    static void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
