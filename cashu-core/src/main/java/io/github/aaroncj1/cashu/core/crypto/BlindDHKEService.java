package io.github.aaroncj1.cashu.core.crypto;

import io.github.aaroncj1.cashu.core.model.BlindingInfo;

import java.math.BigInteger;

/*
 * Blind Diffie Helman Key Exchange
 *
 * Handles the cryptography for wallets/mint interactions
 */
public interface BlindDHKEService {
    BlindingInfo createBlindMessage(String secret, BigInteger r) throws Exception;

//    String unblindSignature(String mintPublicKeyHex, String blindingFactorHex, String cPrimeHex);
//
//    String signBlindMessage(String B_, String privateKey);
//
//    boolean verifySignature(String privateKey, String C, String secretMessage) throws Exception;
}
