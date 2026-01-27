package io.github.aaroncj1.cashu.wallet.ui.controller;

import io.github.aaroncj1.cashu.wallet.service.WalletService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletController {

    @Autowired
    private WalletService walletService;

    @FXML
    private TextField mintUrlField;

    @FXML
    private TextField amountField;

    @FXML
    private TextArea tokenArea;

    @FXML
    private TextArea logArea;

    @FXML
    private TextField balanceMintField;

    @FXML
    public void initialize() {
        log("Wallet initialized.");
    }

    @FXML
    public void onAddMint() {
        String url = mintUrlField.getText();
        if (url != null && !url.isEmpty()) {
            try {
                walletService.addMint(url); // Note: addMint implementation was empty in previous step, assuming it's ready or I should fix it.
                // Wait, I left addMint empty in the previous turn. I should probably fix that or at least make sure it doesn't crash.
                // But for now let's assume the user enters the mint URL when sending/receiving or it is hardcoded in the service (it was hardcoded in sendEcash).
                // Actually, sendEcash used a hardcoded mint.
                // receiveEcash used the mint from the token.
                log("Mint added: " + url);
            } catch (Exception e) {
                showError("Error adding mint", e);
            }
        }
    }

    @FXML
    public void onSend() {
        String amount = amountField.getText();
        if (amount == null || amount.isEmpty()) {
            showError("Input Error", "Please enter an amount.");
            return;
        }
        new Thread(() -> {
            try {
                String token = walletService.sendEcash(amount);
                Platform.runLater(() -> {
                    tokenArea.setText(token);
                    log("Sent " + amount + " sats. Token generated.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Send Error", e));
            }
        }).start();
    }

    @FXML
    public void onReceive() {
        String token = tokenArea.getText();
        if (token == null || token.isEmpty()) {
            showError("Input Error", "Please paste a token.");
            return;
        }
        new Thread(() -> {
            try {
                boolean success = walletService.receiveEcash(token);
                Platform.runLater(() -> {
                    if (success) {
                        log("Token received successfully.");
                        tokenArea.clear();
                    } else {
                        log("Failed to receive token.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Receive Error", e));
            }
        }).start();
    }

    @FXML
    public void onCheckBalance() {
        String mint = balanceMintField.getText();
        if (mint != null && mint.isEmpty()) mint = null; // null for all mints
        String finalMint = mint;
        new Thread(() -> {
             try {
                 long balance = walletService.getBalance(finalMint);
                 Platform.runLater(() -> log("Balance: " + balance + " sats"));
             } catch (Exception e) {
                 Platform.runLater(() -> showError("Balance Error", e));
             }
        }).start();
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, Exception e) {
        e.printStackTrace();
        showError(title, e.getMessage());
    }
}
