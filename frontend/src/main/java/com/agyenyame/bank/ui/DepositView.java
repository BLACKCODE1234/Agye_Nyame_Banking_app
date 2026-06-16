package com.agyenyame.bank.ui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

/** Deposit screen: amount only. */
public class DepositView {

    private final VBox root = UiUtil.card("Deposit");

    public DepositView() {
        Label hint = new Label("Enter the amount to deposit into your account.");
        hint.setWrapText(true);
        TextField amount = new TextField();
        amount.setPromptText("Amount");

        Button submit = new Button("Deposit");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        Button cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);

        submit.setOnAction(e -> {
            try {
                ApiClient.get().post("/api/account/deposit",
                        Map.of("amount", amount.getText().trim()), true);
                UiUtil.info("Deposit completed successfully!");
                BankApp.showDashboard();
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
        cancel.setOnAction(e -> BankApp.showDashboard());

        root.getChildren().addAll(hint, amount, submit, cancel);
    }

    public VBox getRoot() { return root; }
}
