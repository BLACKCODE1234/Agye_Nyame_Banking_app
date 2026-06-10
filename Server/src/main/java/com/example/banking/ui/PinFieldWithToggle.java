package com.example.banking.ui;

import com.example.banking.util.InputValidator;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class PinFieldWithToggle extends HBox {
    private final PasswordField hiddenField = new PasswordField();
    private final TextField visibleField = new TextField();
    private final CheckBox showCheckBox = new CheckBox("Show");

    public PinFieldWithToggle(String promptText) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        hiddenField.setPromptText(promptText);
        visibleField.setPromptText(promptText);
        hiddenField.setTextFormatter(InputValidator.digitsOnlyFormatter(4));
        visibleField.setTextFormatter(InputValidator.digitsOnlyFormatter(4));

        visibleField.textProperty().bindBidirectional(hiddenField.textProperty());
        visibleField.setVisible(false);
        visibleField.setManaged(false);
        HBox.setHgrow(hiddenField, Priority.ALWAYS);
        HBox.setHgrow(visibleField, Priority.ALWAYS);

        showCheckBox.selectedProperty().addListener((obs, wasShown, show) -> {
            hiddenField.setVisible(!show);
            hiddenField.setManaged(!show);
            visibleField.setVisible(show);
            visibleField.setManaged(show);
        });

        getChildren().addAll(hiddenField, visibleField, showCheckBox);
    }

    public String getPin() {
        return hiddenField.getText() == null ? "" : hiddenField.getText();
    }

    public void clear() {
        hiddenField.clear();
        showCheckBox.setSelected(false);
    }
}
