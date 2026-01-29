package io.github.aaroncj1.cashu.wallet;

import io.github.aaroncj1.cashu.wallet.ui.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CashuWalletApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
