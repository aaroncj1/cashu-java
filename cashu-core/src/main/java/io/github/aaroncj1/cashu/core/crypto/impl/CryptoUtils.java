package io.github.aaroncj1.cashu.core.crypto.impl;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

public class CryptoUtils {
    private static final SecureRandom RNG = new SecureRandom();
    private static final String CURVE_NAME = "secp256k1";
    private static final BigInteger N;            // curve order
    private static final ECPoint G;               // generator

    static {
        java.security.Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        N = bcSpec.getN();
        G = bcSpec.getG();
    }


    public static String randomHex() {
        int length = 16; // Number of bytes
        byte[] bytes = new byte[length];
        RNG.nextBytes(bytes);

        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Random scalar in the interval [1, N‑1].
     */
    public static BigInteger randomScalar() {
        BigInteger s;
        do {
            s = new BigInteger(N.bitLength(), RNG);
        } while (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(N) >= 0);
        return s;
    }

    public static String bigIntToHex(BigInteger v) {
        return v.toString(16);
    }

    public static String pointToHex(ECPoint p) {
        return bytesToHex(p.normalize().getEncoded(true));
    }

    public static ECPoint hexToPoint(String hex) {
        byte[] enc = hexToBytes(hex);
        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
                .getCurve()
                .decodePoint(enc)
                .normalize();
    }

    public static byte[] hexToBytes(String s) {
        String clean = s.startsWith("0x") ? s.substring(2) : s;
        if (clean.length() % 2 != 0) clean = "0" + clean;
        int len = clean.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = (byte) Integer.parseInt(clean.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }

    public static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    public static String bytesToHexId(byte[] b) {
        // Special handling for keyset ID which might be hex-encoded string as bytes
        // If the bytes represent ASCII chars of a hex string, we want the string, not hex of bytes.
        // Example: id=0050f763b36a7b8c (16 chars)
        // If received as bytes of this string: 30303530...
        // We want to return new String(b)
        // But if received as raw bytes of the ID (8 bytes), we want hex.
        // Keyset ID is typically 8 bytes (16 hex chars).
        
        if (b.length == 16) {
             // If length is 16, it might be the raw ASCII bytes of the hex string?
             // Or it's a 16-byte ID?
             // Cashu keyset IDs are 8 bytes base64 encoded usually, or hex?
             // NUT-02: "Keyset ID is derived ... first 8 bytes of the hash."
             // So it's 8 bytes.
             // Hex representation is 16 characters.
             // If we have 16 bytes here, it's likely the ASCII bytes of the hex string.
             try {
                 String s = new String(b);
                 // Check if it's valid hex
                 if (s.matches("^[0-9a-fA-F]{16}$")) {
                     return s;
                 }
             } catch (Exception e) {
                 // ignore
             }
        }
        return bytesToHex(b);
    }

    public static BigInteger hexToBigInt(String hex) {
        return new BigInteger(hex, 16);
    }
}
