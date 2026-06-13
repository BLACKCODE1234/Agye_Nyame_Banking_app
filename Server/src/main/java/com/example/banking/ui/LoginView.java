package com.example.banking.ui;

import com.example.banking.model.Account;
import com.example.banking.service.BankingService;
import com.example.banking.util.InputValidator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView extends VBox {
    public LoginView(
            BankingService bankingService,
            Consumer<Account> onLogin,
            Runnable onForgotPin
    ) {
        setPadding(new Insets(18));
        setSpacing(12);

        Label title = new Label("Sign in");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        TextField mobileField = new TextField();
        mobileField.setPromptText("10-digit mobile number");
        mobileField.setTextFormatter(InputValidator.digitsOnlyFormatter(10));

        PinFieldWithToggle pinInput = new PinFieldWithToggle("4-digit PIN");

        Button loginBtn = new Button("Login");
        Hyperlink forgotPinLink = new Hyperlink("Forgot PIN?");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setVisible(false);

        form.add(new Label("Mobile Number:"), 0, 0);
        form.add(mobileField, 1, 0);
        form.add(new Label("PIN:"), 0, 1);
        form.add(pinInput, 1, 1);
        form.add(loginBtn, 1, 2);
        form.add(forgotPinLink, 1, 3);

        Label hint = new Label("Demo logins: 9876543210/1234, 9123456780/4321");
        hint.setStyle("-fx-text-fill: #555;");

        loginBtn.setOnAction(evt -> {
            errorLabel.setVisible(false);

            try {
                String mobileNumber = mobileField.getText();
                String pin = pinInput.getPin();

                Account account = bankingService.authenticate(mobileNumber, pin);
                onLogin.accept(account);
            } catch (InputValidator.ValidationException | BankingService.BankingException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        forgotPinLink.setOnAction(evt -> onForgotPin.run());

        getChildren().addAll(title, form, hint, errorLabel);
    }
}
