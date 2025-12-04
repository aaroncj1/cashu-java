//package io.github.aaroncj1.cashu.core.crypto.impl;// File: src/test/java/cashu/java/crypto/B_DHKEServiceSecp256k1ImplTest.java
//
//import io.github.aaroncj1.cashu.core.model.BlindingInfo;
//import org.bouncycastle.jce.interfaces.ECPrivateKey;
//import org.bouncycastle.jce.interfaces.ECPublicKey;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.math.ec.ECPoint;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.SecureRandom;
//import java.security.Security;
//import java.security.spec.ECGenParameterSpec;
//
//class B_DHKEServiceSecp256k1ImplTest {
//
//    @BeforeAll
//    static void setupProvider() {
//        Security.addProvider(new BouncyCastleProvider());
//    }
//
//    private static KeyMat newKeyPair() throws Exception {
//        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
//        kpg.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
//        KeyPair kp = kpg.generateKeyPair();
//
//        ECPrivateKey bcPriv = (ECPrivateKey) kp.getPrivate();
//        String privHex = stripLeadingZeroByteHex(toHex(bcPriv.getD().toByteArray()));
//
//        ECPublicKey bcPub = (ECPublicKey) kp.getPublic();
//        ECPoint Q = bcPub.getQ().normalize();
//        String pubCompressedHex = toHex(Q.getEncoded(true));
//
//        return new KeyMat(privHex, pubCompressedHex);
//    }
//
//    private static String newKeyPairCompressedPubHex() throws Exception {
//        return newKeyPair().pubCompressedHex;
//    }
//
//    private static boolean isHex(String s) {
//        if (s == null || s.isEmpty()) return false;
//        for (int i = 0; i < s.length(); i++) {
//            char c = Character.toLowerCase(s.charAt(i));
//            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) return false;
//        }
//        return true;
//    }
//
//    private static String flipFirstHexNibble(String hex) {
//        if (hex == null || hex.isEmpty()) return hex;
//        char c = Character.toLowerCase(hex.charAt(0));
//        char flipped = (c != '0') ? '0' : '1';
//        return flipped + (hex.length() > 1 ? hex.substring(1) : "");
//    }
//
//    private static String toHex(byte[] b) {
//        StringBuilder sb = new StringBuilder(b.length * 2);
//        for (byte v : b) sb.append(String.format("%02x", v));
//        return sb.toString();
//    }
//
//    // Helpers
//
//    private static String stripLeadingZeroByteHex(String hex) {
//        if (hex == null || hex.length() < 2) return hex;
//        int i = 0;
//        while (i + 1 < hex.length() && hex.startsWith("00", i)) {
//            i += 2;
//        }
//        return hex.substring(i);
//    }
//
//    @Test
//    void createBlindMessage_returns_non_null_and_hex() throws Exception {
//        // Arrange
//        String mintPubCompressedHex = newKeyPairCompressedPubHex();
//        B_DHKEServiceSecp256k1Impl service = new B_DHKEServiceSecp256k1Impl();
//
//        // Act
//        BlindingInfo info = service.createBlindMessage("hi", );
//
//        // Assert
//        Assertions.assertNotNull(info);
//        Assertions.assertNotNull(info.secretXHex());
//        Assertions.assertNotNull(info.blindedPublicKeyHex());
//        Assertions.assertNotNull(info.blindingFactorHex());
//
//        // basic hex validation
//        Assertions.assertTrue(isHex(info.secretXHex()));
//        Assertions.assertTrue(isHex(info.blindedPublicKeyHex()));
//        Assertions.assertTrue(isHex(info.blindingFactorHex()));
//
//        // blinded public key should be compressed SEC1: 33 bytes -> 66 hex chars, starts with 02/03
//        String B_ = info.blindedPublicKeyHex();
//        Assertions.assertEquals(66, B_.length(), "blinded point should be compressed SEC1 (33 bytes)");
//        Assertions.assertTrue(B_.startsWith("02") || B_.startsWith("03"), "compressed point must start with 02/03");
//    }
//
//    @Test
//    void sign_unblind_and_verify_round_trip_succeeds() throws Exception {
//        // Arrange: mint keypair
//        KeyMat km = newKeyPair();
//        String privHex = km.privHex;
//        String mintPubCompressedHex = km.pubCompressedHex;
//
//        B_DHKEServiceSecp256k1Impl service = new B_DHKEServiceSecp256k1Impl();
//
//        // Alice blinds
//        BlindingInfo info = service.createBlindMessage("hi", );
//
//        // Mint signs blinded message
//        String C_ = service.signBlindMessage(info.blindedPublicKeyHex(), privHex);
//
//        // Alice unblinds
//        String C = service.unblindSignature(mintPubCompressedHex, info.blindingFactorHex(), C_);
//
//        // Verify k*Y equals C
//        Assertions.assertTrue(service.verifySignature(privHex, C, info.secretXHex()));
//    }
//
//    @Test
//    void verify_fails_with_wrong_secret() throws Exception {
//        // Arrange
//        KeyMat km = newKeyPair();
//        String privHex = km.privHex;
//        String mintPubCompressedHex = km.pubCompressedHex;
//
//        B_DHKEServiceSecp256k1Impl service = new B_DHKEServiceSecp256k1Impl();
//        BlindingInfo info = service.createBlindMessage("hi", );
//        String C_ = service.signBlindMessage(info.blindedPublicKeyHex(), privHex);
//        String C = service.unblindSignature(mintPubCompressedHex, info.blindingFactorHex(), C_);
//
//        // Tamper secret
//        String wrongSecret = flipFirstHexNibble(info.secretXHex());
//
//        // Assert
//        Assertions.assertFalse(service.verifySignature(privHex, C, wrongSecret));
//    }
//
//    @Test
//    void createBlindMessage_throws_on_null_input() {
//        B_DHKEServiceSecp256k1Impl service = new B_DHKEServiceSecp256k1Impl();
//        Assertions.assertThrows(NullPointerException.class, () -> service.createBlindMessage("hi", ));
//    }
//
//    @RepeatedTest(3)
//    void multiple_round_trips_randomized_inputs() throws Exception {
//        KeyMat km = newKeyPair();
//        String privHex = km.privHex;
//        String mintPubCompressedHex = km.pubCompressedHex;
//
//        B_DHKEServiceSecp256k1Impl service = new B_DHKEServiceSecp256k1Impl();
//        BlindingInfo info = service.createBlindMessage("hi", );
//        String C_ = service.signBlindMessage(info.blindedPublicKeyHex(), privHex);
//        String C = service.unblindSignature(mintPubCompressedHex, info.blindingFactorHex(), C_);
//        Assertions.assertTrue(service.verifySignature(privHex, C, info.secretXHex()));
//    }
//
//    private static class KeyMat {
//        final String privHex;
//        final String pubCompressedHex;
//
//        KeyMat(String privHex, String pubCompressedHex) {
//            this.privHex = privHex;
//            this.pubCompressedHex = pubCompressedHex;
//        }
//    }
//}