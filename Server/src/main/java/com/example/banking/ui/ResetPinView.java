package com.example.banking.ui;

import com.example.banking.service.BankingService;
import com.example.banking.util.InputValidator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ResetPinView extends VBox {
    public ResetPinView(BankingService bankingService, Runnable onBackToLogin) {
        setPadding(new Insets(18));
        setSpacing(12);

        Label title = new Label("Reset PIN");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Verify your account, then choose a new 4-digit PIN.");
        subtitle.setStyle("-fx-text-fill: #555;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        TextField mobileField = new TextField();
        mobileField.setPromptText("10-digit mobile number");
        mobileField.setTextFormatter(InputValidator.digitsOnlyFormatter(10));

        TextField accountIdField = new TextField();
        accountIdField.setPromptText("Account ID (e.g. 1001)");

        PinFieldWithToggle newPinInput = new PinFieldWithToggle("New PIN");
        PinFieldWithToggle confirmPinInput = new PinFieldWithToggle("Confirm new PIN");

        Button resetBtn = new Button("Reset PIN");
        Hyperlink backLink = new Hyperlink("Back to login");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setVisible(false);

        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #1b5e20;");
        successLabel.setVisible(false);

        form.add(new Label("Mobile Number:"), 0, 0);
        form.add(mobileField, 1, 0);
        form.add(new Label("Account ID:"), 0, 1);
        form.add(accountIdField, 1, 1);
        form.add(new Label("New PIN:"), 0, 2);
        form.add(newPinInput, 1, 2);
        form.add(new Label("Confirm PIN:"), 0, 3);
        form.add(confirmPinInput, 1, 3);
        form.add(resetBtn, 1, 4);
        form.add(backLink, 1, 5);

        Label hint = new Label("Demo: mobile 9876543210, account 1001");
        hint.setStyle("-fx-text-fill: #555;");

        resetBtn.setOnAction(evt -> {
            errorLabel.setVisible(false);
            successLabel.setVisible(false);

            try {
                String mobile = mobileField.getText();
                String accountId = accountIdField.getText() == null ? "" : accountIdField.getText().trim();
                String newPin = newPinInput.getPin();
                String confirmPin = confirmPinInput.getPin();

                bankingService.resetPin(mobile, accountId, newPin, confirmPin);

                successLabel.setText("PIN updated successfully. Go back to login and sign in with your new PIN.");
                successLabel.setVisible(true);
                newPinInput.clear();
                confirmPinInput.clear();
            } catch (InputValidator.ValidationException | BankingService.BankingException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        backLink.setOnAction(evt -> onBackToLogin.run());

        getChildren().addAll(title, subtitle, form, hint, errorLabel, successLabel);
    }
}
