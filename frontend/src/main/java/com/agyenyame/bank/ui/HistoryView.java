package com.agyenyame.bank.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Transaction history screen: lists the user's transactions over time, newest first. */
public class HistoryView {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final VBox root = UiUtil.card("Transaction History");
    private final ListView<String> historyList = new ListView<>();
    private final Label empty = new Label("No transactions yet.");

    public HistoryView() {
        historyList.setPrefHeight(360);
        VBox.setMargin(historyList, new Insets(8, 0, 0, 0));

        Button refresh = new Button("Refresh");
        refresh.setMaxWidth(Double.MAX_VALUE);
        Button back = new Button("Back");
        back.setMaxWidth(Double.MAX_VALUE);
        refresh.setOnAction(e -> reload());
        back.setOnAction(e -> BankApp.showDashboard());

        root.getChildren().addAll(historyList, refresh, back);
        reload();
    }

    private void reload() {
        try {
            historyList.getItems().clear();
            JsonNode history = ApiClient.get().get("/api/account/history");
            if (history.isEmpty()) {
                historyList.setPlaceholder(empty);
            }
            for (JsonNode tx : history) {
                String when = tx.hasNonNull("createdAt")
                        ? TS.format(Instant.parse(tx.get("createdAt").asText())) : "";
                String counterparty = tx.hasNonNull("counterpartyMobile")
                        ? " (" + tx.get("counterpartyMobile").asText() + ")" : "";
                historyList.getItems().add(
                        when + "  " + tx.get("type").asText() + "  " + tx.get("amount").asText()
                                + counterparty + "  -> balance " + tx.get("resultingBalance").asText());
            }
        } catch (ApiClient.ApiError ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    public VBox getRoot() { return root; }
}
