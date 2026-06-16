package com.agyenyame.bank.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Entry point. Manages a single Stage and swaps scenes between views. */
public class BankApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Agye Nyame Bank");
        showLogin();
        stage.show();
    }

    private static void setScene(javafx.scene.Parent root) {
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 420, 560));
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }

    public static void showLogin() { setScene(new LoginView().getRoot()); }
    public static void showSignup() { setScene(new SignupView().getRoot()); }
    public static void showOtp(String reference, OtpView.Purpose purpose, Runnable onSuccessExtra) {
        setScene(new OtpView(reference, purpose, onSuccessExtra).getRoot());
    }
    public static void showDashboard() { setScene(new DashboardView().getRoot()); }
    public static void showDeposit() { setScene(new DepositView().getRoot()); }
    public static void showWithdraw() { setScene(new WithdrawView().getRoot()); }
    public static void showTransfer() { setScene(new TransferView().getRoot()); }
    public static void showHistory() { setScene(new HistoryView().getRoot()); }

    public static void main(String[] args) {
        launch(args);
    }
}
