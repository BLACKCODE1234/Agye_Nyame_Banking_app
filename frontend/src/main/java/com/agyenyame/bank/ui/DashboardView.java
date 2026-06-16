package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Main dashboard: shows balance and exposes deposit, withdraw, transfer and history. */
public class DashboardView {

    private final VBox root = UiUtil.card("Dashboard");
    private final Label balanceLabel = new Label("Balance: ...");
    private final ListView<String> historyList = new ListView<>();

    public DashboardView() {
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button transfer = new Button("Transfer");
        Button history = new Button("History");
        Button refresh = new Button("Refresh");
        Button logout = new Button("Logout");
        HBox actions = new HBox(8, deposit, withdraw, transfer, history, refresh);

        deposit.setOnAction(e -> BankApp.showDeposit());
        withdraw.setOnAction(e -> BankApp.showWithdraw());
        transfer.setOnAction(e -> BankApp.showTransfer());
        history.setOnAction(e -> BankApp.showHistory());
        refresh.setOnAction(e -> reload());
        logout.setOnAction(e -> { ApiClient.get().logout(); BankApp.showLogin(); });
        logout.setMaxWidth(Double.MAX_VALUE);

        historyList.setPrefHeight(260);
        VBox.setMargin(historyList, new Insets(8, 0, 0, 0));

        root.getChildren().addAll(balanceLabel, actions, new Label("Transaction history"), historyList, logout);
        reload();
    }

    private void reload() {
        try {
            JsonNode bal = ApiClient.get().get("/api/account/balance");
            balanceLabel.setText("Balance: " + bal.get("balance").asText());
            historyList.getItems().clear();
            JsonNode history = ApiClient.get().get("/api/account/history");
            for (JsonNode tx : history) {
                String counterparty = tx.hasNonNull("counterpartyMobile")
                        ? " (" + tx.get("counterpartyMobile").asText() + ")" : "";
                historyList.getItems().add(
                        tx.get("type").asText() + "  " + tx.get("amount").asText() + counterparty
                                + "  -> balance " + tx.get("resultingBalance").asText());
            }
        } catch (ApiClient.ApiError ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void onDeposit() {
        prompt("Deposit", "Amount").ifPresent(amount -> {
            try {
                ApiClient.get().post("/api/account/deposit", Map.of("amount", amount), true);
                reload();
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private void onWithdraw() {
        Optional<Map<String, String>> input = amountPinDialog("Withdraw");
        input.ifPresent(values -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("amount", values.get("amount"));
            body.put("pin", values.get("pin"));
            try {
                ApiClient.get().post("/api/account/withdraw", body, true);
                reload();
            } catch (ApiClient.ApiError ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private void onTransfer() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Transfer");
        dialog.setHeaderText("Send money to another account");
        ButtonType ok = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField recipient = new TextField(); recipient.setPromptText("Recipient mobile number");
        TextField amount = new TextField(); amount.setPromptText("Amount");
        PasswordField pin = new PasswordField(); pin.setPromptText("PIN");
        VBox box = new VBox(8, recipient, amount, pin);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(bt -> bt == ok
                ? Map.of("recipient", recipient.getText().trim(),
                         "amount", amount.getText().trim(), "pin", pin.getText())
                : null);

        dialog.showAndWait().ifPresent(values -> {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("transferMobileNumber", values.get("recipient"));
            body.put("amount", values.get("amount"));
            body.put("pin", values.get("pin"));
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
    }

    private Optional<String> prompt(String title, String field) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(field + ":");
        return dialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty());
    }

    private Optional<Map<String, String>> amountPinDialog(String title) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        TextField amount = new TextField(); amount.setPromptText("Amount");
        PasswordField pin = new PasswordField(); pin.setPromptText("PIN");
        VBox box = new VBox(8, amount, pin);
        box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(bt -> bt == ok
                ? Map.of("amount", amount.getText().trim(), "pin", pin.getText()) : null);
        return dialog.showAndWait();
    }

    public VBox getRoot() { return root; }
}
