package io.github.aaroncj1.cashu.wallet.service;

import java.io.IOException;

public interface WalletService {

    boolean receiveEcash(String token) throws Exception;

    String sendEcash(String amount) throws Exception;

//    void receiveLighting(String amount);
//
//    void sendLightning(String amount);

    void getBalance(String mint);

    void addMint(String mintUrl);
}
