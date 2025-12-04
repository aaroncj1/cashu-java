package cashu.java.wallet;

import cashu.java.wallet.persistence.MintRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletServiceImplTest {

    @Autowired
    MintRepository mintRepository;

    @Autowired
    WalletServiceImpl walletService;

    @Test
    void addMint() throws IOException, InterruptedException {
        walletService.addMint("test mint testnut", "https://testnut.cashu.space");
        walletService.addMint("test mint 8333", "https://8333.space:3338");
        walletService.listMints();
    }

}