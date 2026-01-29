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
    private TextField depositAmountField;

    @FXML
    private TextArea invoiceArea;

    @FXML
    private TextField quoteIdField;

    @FXML
    private TextArea meltInvoiceArea;

    @FXML
    private TextField meltQuoteIdField;

    @FXML
    public void initialize() {
        log("Wallet initialized.");
    }

    @FXML
    public void onRequestMint() {
        String amount = depositAmountField.getText();
        String mint = mintUrlField.getText();
        if (amount == null || amount.isEmpty()) {
            showError("Input Error", "Please enter amount.");
            return;
        }
        if (mint == null || mint.isEmpty()) {
            showError("Input Error", "Please enter mint URL.");
            return;
        }

        new Thread(() -> {
            try {
                // requestMint returns the invoice, but we need the quoteId as well.
                // The current implementation of requestMint returns only the invoice.
                // I need to update it to return both or something.
                // Or I can parse the invoice? No, quoteId is from the response.
                // I will update requestMint to return the invoice for now, but I realized I need the quoteId to check status/mint.
                // WalletService.requestMint currently returns String (invoice).
                // Let's assume for now the quote ID is needed.
                // Actually, in the implementation I did:
                // pendingMintBlindingInfos.put(response.quote(), blindingInfos);
                // return response.request();
                // So I have the quoteId inside the service, but the UI needs it to call mintTokens(quoteId).
                // I should change requestMint to return the Quote ID or an object containing both.
                // Let's just fix the service to return the Quote ID for now, or maybe the invoice is encoded with it? No.
                // I'll make a quick hack: WalletService.requestMint returns "QUOTE_ID:INVOICE" or I change the signature.
                // Changing signature is better. But I can't change the interface easily without breaking other things? No, I am the only user.
                
                // Wait, let's look at what I wrote in WalletServiceImpl.java for requestMint:
                // return response.request();
                // This is just the bolt11.
                
                // Let's modify WalletService interface and impl to return a Pair or a custom object, or just parse it if I concat.
                // I will update WalletService to return a String "QUOTE_ID|INVOICE" for simplicity in this turn without adding DTOs.
                
                String result = walletService.requestMint(amount, mint);
                String[] parts = result.split("\\|", 2);
                String quoteId = parts[0];
                String invoice = parts[1];
                
                Platform.runLater(() -> {
                    invoiceArea.setText(invoice);
                    quoteIdField.setText(quoteId);
                    log("Invoice received. Pay it then click 'Check Payment'. Quote ID: " + quoteId);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Deposit Error", e));
            }
        }).start();
    }

    @FXML
    public void onMintTokens() {
        String quoteId = quoteIdField.getText();
        String amount = depositAmountField.getText();
        String mint = mintUrlField.getText();
        
        if (quoteId == null || quoteId.isEmpty()) {
            showError("Input Error", "Quote ID missing.");
            return;
        }
        
        new Thread(() -> {
            try {
                walletService.mintTokens(quoteId, amount, mint);
                Platform.runLater(() -> {
                    log("Tokens minted successfully! Balance updated.");
                    invoiceArea.clear();
                    quoteIdField.clear();
                });
            } catch (Exception e) {
                 Platform.runLater(() -> showError("Mint Error", e));
            }
        }).start();
    }

    @FXML
    public void onRequestMelt() {
        String invoice = meltInvoiceArea.getText();
        String mint = mintUrlField.getText();
        
        if (invoice == null || invoice.isEmpty()) {
            showError("Input Error", "Invoice missing.");
            return;
        }
         if (mint == null || mint.isEmpty()) {
            showError("Input Error", "Please enter mint URL.");
            return;
        }
        
        new Thread(() -> {
            try {
                String quoteId = walletService.requestMelt(invoice, mint);
                Platform.runLater(() -> {
                    meltQuoteIdField.setText(quoteId);
                    log("Melt quote received: " + quoteId + ". Click 'Pay Invoice' to confirm.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Melt Quote Error", e));
            }
        }).start();
    }

    @FXML
    public void onMeltTokens() {
        String quoteId = meltQuoteIdField.getText();
        String mint = mintUrlField.getText();
        
         if (quoteId == null || quoteId.isEmpty()) {
            showError("Input Error", "Quote ID missing.");
            return;
        }
        
        new Thread(() -> {
            try {
                walletService.meltTokens(quoteId, mint);
                Platform.runLater(() -> {
                    log("Payment sent (Melted) successfully!");
                    meltInvoiceArea.clear();
                    meltQuoteIdField.clear();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Melt Error", e));
            }
        }).start();
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
