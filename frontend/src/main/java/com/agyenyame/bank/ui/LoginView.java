package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

/** Login screen: mobile number + PIN. */
public class LoginView {

    private final VBox root = UiUtil.card("Welcome to Agye Nyame Bank");

    public LoginView() {
        TextField mobile = new TextField();
        mobile.setPromptText("Mobile number");
        PasswordField pin = new PasswordField();
        pin.setPromptText("PIN");

        Button login = new Button("Login");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);
        Button toSignup = new Button("Create an account");
        toSignup.setMaxWidth(Double.MAX_VALUE);

        login.setOnAction(e -> {
            try {
                JsonNode res = ApiClient.get().post("/api/auth/login",
                        Map.of("mobileNumber", mobile.getText().trim(), "pin", pin.getText()), false);
                ApiClient.get().setToken(res.get("token").asText());
                BankApp.showDashboard();
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        toSignup.setOnAction(e -> BankApp.showSignup());

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(mobile, pin, login, toSignup);
    }

    public VBox getRoot() { return root; }
}
