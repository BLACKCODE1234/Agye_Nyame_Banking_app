package com.example.banking.ui;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.service.BankingService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.control.Separator;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import javafx.scene.control.TableRow;

public class DashboardView extends BorderPane {
    private final BankingService bankingService;
    private final String accountId;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Label ownerLabel = new Label();
    private final Label accountLabel = new Label();
    private final Label balanceLabel = new Label();
    private final TableView<Transaction> transactionsTable = new TableView<>();

    private final TextField amountField = new TextField();
    private final Label errorLabel = new Label();

    private final Runnable onLogout;

    public DashboardView(BankingService bankingService, Account account, Runnable onLogout) {
        this.bankingService = bankingService;
        this.accountId = account.getId();
        this.onLogout = onLogout;

        setPadding(new Insets(18));

        setupTop(account);
        setupCenter();
        setupBottom();

        refresh();
    }

    private void setupTop(Account account) {
        ownerLabel.setText("Owner: " + account.getOwnerName());
        accountLabel.setText("Account: " + account.getId());
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox header = new VBox(6, ownerLabel, accountLabel, balanceLabel);
        header.setPadding(new Insets(0, 0, 14, 0));
        setTop(header);
    }

    private void setupCenter() {
        TableColumn<Transaction, String> timeCol = new TableColumn<>("Time");
        timeCol.setPrefWidth(210);
        timeCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatInstant(cell.getValue().getTimestamp())));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(120);
        typeCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getType().name()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setPrefWidth(140);
        amountCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(currencyFormat.format(cell.getValue().getAmount())));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(220);
        descCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getDescription()));

        transactionsTable.getColumns().addAll(timeCol, typeCol, amountCol, descCol);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setCenter(transactionsTable);
    }

    private void setupBottom() {
        Label depositLabel = new Label("Amount");
        amountField.setPromptText("e.g. 50.00");
        amountField.setPrefWidth(160);

        Button depositBtn = new Button("Deposit");
        Button withdrawBtn = new Button("Withdraw");
        Button logoutBtn = new Button("Logout");

        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setVisible(false);

        depositBtn.setOnAction(evt -> {
            errorLabel.setVisible(false);
            try {
                BigDecimal amount = parseAmount(amountField.getText());
                bankingService.deposit(accountId, amount);
                refresh();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        withdrawBtn.setOnAction(evt -> {
            errorLabel.setVisible(false);
            try {
                BigDecimal amount = parseAmount(amountField.getText());
                bankingService.withdraw(accountId, amount);
                refresh();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        depositBtn.setPrefWidth(110);
        withdrawBtn.setPrefWidth(110);
        logoutBtn.setPrefWidth(110);

        HBox actionRow = new HBox(12,
                depositLabel,
                amountField,
                depositBtn,
                withdrawBtn,
                logoutBtn
        );
        actionRow.setAlignment(Pos.CENTER_LEFT);

        logoutBtn.setOnAction(evt -> onLogout.run());

        VBox bottom = new VBox(10, new Separator(), actionRow, errorLabel);
        setBottom(bottom);
    }

    private void refresh() {
        Account account = bankingService.getAccount(accountId);
        balanceLabel.setText("Balance: " + currencyFormat.format(account.getBalance()));

        List<Transaction> recent = bankingService.getRecentTransactions(accountId, 50);
        transactionsTable.setItems(FXCollections.observableArrayList(recent));
    }

    private void showError(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "Something went wrong.";
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Amount is required.");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Amount is required.");
        }

        // Allow plain "123.45" input only (demo).
        return new BigDecimal(trimmed);
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dt.format(dateTimeFormatter);
    }
}

