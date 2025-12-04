package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;
import io.github.aaroncj1.cashu.core.serialization.TokenCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletServiceImplTest {

    @Test
    void testReceive() throws Exception {
        WalletServiceImpl walletService = new WalletServiceImpl();
        walletService.receiveEcash("");
    }

    @Test
    void testSend() throws Exception {
        WalletServiceImpl walletService = new WalletServiceImpl();
        String token = walletService.sendEcash("100");
        System.out.println(token);
        TokenV4 tokenV4 = TokenCodec.deserializeV4(token);
        System.out.println(tokenV4.toString());

        walletService.receiveEcash(token);
    }

}