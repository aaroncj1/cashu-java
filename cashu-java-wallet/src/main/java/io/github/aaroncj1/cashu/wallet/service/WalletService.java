package io.github.aaroncj1.cashu.wallet.service;

import java.io.IOException;

public interface WalletService {

    boolean receiveEcash(String token) throws Exception;

    String sendEcash(String amount) throws Exception;

//    void receiveLighting(String amount);
//
//    void sendLightning(String amount);

    long getBalance(String mint);

    void addMint(String mintUrl);

    String requestMint(String amount, String mintUrl) throws Exception;

    void mintTokens(String quoteId, String amount, String mintUrl) throws Exception;

    String requestMelt(String invoice, String mintUrl) throws Exception;

    void meltTokens(String quoteId, String mintUrl) throws Exception;
}
