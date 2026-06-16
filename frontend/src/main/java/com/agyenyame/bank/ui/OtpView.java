package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * OTP entry screen, reused for two flows:
 *  - SIGNUP: verifying creates the account and logs the user in.
 *  - TRANSFER: verifying executes the pending transfer.
 */
public class OtpView {

    public enum Purpose { SIGNUP, TRANSFER }

    private final VBox root = UiUtil.card("Enter OTP");

    public OtpView(String reference, Purpose purpose, Runnable onSuccessExtra) {
        Label hint = new Label("A one-time code was sent to your mobile number.");
        hint.setWrapText(true);
        TextField code = new TextField();
        code.setPromptText("6-digit code");

        Button verify = new Button("Verify");
        verify.setMaxWidth(Double.MAX_VALUE);
        verify.setDefaultButton(true);
        Button cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);

        verify.setOnAction(e -> {
            Map<String, Object> body = Map.of("reference", reference, "code", code.getText().trim());
            try {
                if (purpose == Purpose.SIGNUP) {
                    JsonNode res = ApiClient.get().post("/api/auth/signup/verify", body, false);
                    ApiClient.get().setToken(res.get("token").asText());
                    UiUtil.info("Account created successfully!");
                    BankApp.showDashboard();
                } else {
                    ApiClient.get().post("/api/account/transfer/verify", body, true);
                    UiUtil.info("Transfer completed successfully!");
                    if (onSuccessExtra != null) onSuccessExtra.run();
                    BankApp.showDashboard();
                }
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        cancel.setOnAction(e -> {
            if (purpose == Purpose.SIGNUP) BankApp.showLogin();
            else BankApp.showDashboard();
        });

        root.getChildren().addAll(hint, code, verify, cancel);
    }

    public VBox getRoot() { return root; }
}
