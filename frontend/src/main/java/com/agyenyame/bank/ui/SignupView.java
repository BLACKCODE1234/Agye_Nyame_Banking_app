package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Signup screen. On submit, the backend issues an OTP and returns a reference;
 * the user is taken to the OTP screen, and the account is only created after OTP verification.
 */
public class SignupView {

    private final VBox root = UiUtil.card("Create your account");

    public SignupView() {
        TextField firstName = new TextField(); firstName.setPromptText("First name");
        TextField lastName = new TextField(); lastName.setPromptText("Last name");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField mobile = new TextField(); mobile.setPromptText("Mobile number");
        PasswordField pin = new PasswordField(); pin.setPromptText("PIN (4-6 digits)");
        PasswordField confirmPin = new PasswordField(); confirmPin.setPromptText("Confirm PIN");

        Button submit = new Button("Get OTP");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        Button back = new Button("Back to login");
        back.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            if (!pin.getText().equals(confirmPin.getText())) {
                UiUtil.error("PIN and confirm PIN do not match");
                return;
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("firstName", firstName.getText().trim());
            body.put("lastName", lastName.getText().trim());
            body.put("email", email.getText().trim());
            body.put("mobileNumber", mobile.getText().trim());
            body.put("pin", pin.getText());
            body.put("confirmPin", confirmPin.getText());
            try {
                JsonNode res = ApiClient.get().post("/api/auth/signup", body, false);
                String reference = res.get("reference").asText();
                if (res.hasNonNull("devCode")) {
                    UiUtil.info("OTP (dev mode): " + res.get("devCode").asText());
                }
                BankApp.showOtp(reference, OtpView.Purpose.SIGNUP, null);
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        back.setOnAction(e -> BankApp.showLogin());

        root.getChildren().addAll(firstName, lastName, email, mobile, pin, confirmPin, submit, back);
    }

    public VBox getRoot() { return root; }
}
