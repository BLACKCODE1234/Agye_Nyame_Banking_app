package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transfer screen: recipient mobile number + amount + PIN.
 * Initiates an OTP-gated transfer; verifying the OTP executes it.
 */
public class TransferView {

    private final VBox root = UiUtil.card("Transfer");

    public TransferView() {
        Label hint = new Label("Send money to another account.");
        hint.setWrapText(true);
        TextField recipient = new TextField();
        recipient.setPromptText("Recipient mobile number");
        TextField amount = new TextField();
        amount.setPromptText("Amount");
        PasswordField pin = new PasswordField();
        pin.setPromptText("PIN");

        Button submit = new Button("Continue");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        Button cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("transferMobileNumber", recipient.getText().trim());
            body.put("amount", amount.getText().trim());
            body.put("pin", pin.getText());
            try {
                // Step 1: init transfer -> backend issues an OTP.
                JsonNode res = ApiClient.get().post("/api/account/transfer", body, true);
                String reference = res.get("reference").asText();
                if (res.hasNonNull("devCode")) {
                    UiUtil.info("OTP (dev mode): " + res.get("devCode").asText());
                }
                // Step 2: OTP screen -> verifying executes the transfer.
                BankApp.showOtp(reference, OtpView.Purpose.TRANSFER, null);
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        cancel.setOnAction(e -> BankApp.showDashboard());

        root.getChildren().addAll(hint, recipient, amount, pin, submit, cancel);
    }

    public VBox getRoot() { return root; }
}
