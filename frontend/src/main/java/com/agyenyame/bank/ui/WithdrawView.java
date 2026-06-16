package com.agyenyame.bank.ui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/** Withdraw screen: amount + PIN. */
public class WithdrawView {

    private final VBox root = UiUtil.card("Withdraw");

    public WithdrawView() {
        Label hint = new Label("Enter the amount and your PIN to withdraw.");
        hint.setWrapText(true);
        TextField amount = new TextField();
        amount.setPromptText("Amount");
        PasswordField pin = new PasswordField();
        pin.setPromptText("PIN");

        Button submit = new Button("Withdraw");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        Button cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", amount.getText().trim());
            body.put("pin", pin.getText());
            try {
                ApiClient.get().post("/api/account/withdraw", body, true);
                UiUtil.info("Withdrawal completed successfully!");
                BankApp.showDashboard();
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        cancel.setOnAction(e -> BankApp.showDashboard());

        root.getChildren().addAll(hint, amount, pin, submit, cancel);
    }

    public VBox getRoot() { return root; }
}
