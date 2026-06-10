package com.example.banking;

import com.example.banking.model.Account;
import com.example.banking.service.BankingService;
import com.example.banking.ui.DashboardView;
import com.example.banking.ui.LoginView;
import com.example.banking.ui.ResetPinView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private BankingService bankingService;

    @Override
    public void start(Stage stage) {
        bankingService = new BankingService();

        stage.setTitle("Banking System");
        stage.setScene(createLoginScene(stage));
        stage.show();
    }

    private Scene createLoginScene(Stage stage) {
        LoginView loginView = new LoginView(
                bankingService,
                (Account account) -> {
                    DashboardView dashboardView = new DashboardView(
                            bankingService,
                            account,
                            () -> stage.setScene(createLoginScene(stage))
                    );
                    stage.setScene(new Scene(dashboardView, 920, 560));
                },
                () -> stage.setScene(createResetPinScene(stage))
        );

        return new Scene(loginView, 540, 460);
    }

    private Scene createResetPinScene(Stage stage) {
        ResetPinView resetPinView = new ResetPinView(
                bankingService,
                () -> stage.setScene(createLoginScene(stage))
        );

        return new Scene(resetPinView, 560, 520);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
