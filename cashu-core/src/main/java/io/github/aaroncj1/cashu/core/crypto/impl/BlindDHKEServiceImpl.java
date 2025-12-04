package io.github.aaroncj1.cashu.core.crypto.impl;

import io.github.aaroncj1.cashu.core.model.BlindingInfo;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils.*;

public class BlindDHKEServiceImpl {

    /* ------------------------------------------------------------------ *
     *   Curve constants – loaded once                                     *
     * ------------------------------------------------------------------ */
    private static final String CURVE_NAME = "secp256k1";
    private static final String DOMAIN_SEPARATOR_HEX = "536563703235366b315f48617368546f43757276655f43617368755f";


    /**
     * Order of the scalar field (prime n).
     */
    private static final BigInteger N;            // curve order

    /**
     * Generator point G (affine).
     */
    private static final ECPoint G;               // generator

    static {
        java.security.Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        N = bcSpec.getN();
        G = bcSpec.getG();
    }

    public static ECPoint hashToCurve(byte[] message) throws Exception {
        // Domain separator hex (from Python specification)
        final byte[] DOMAIN_SEPARATOR = Hex.decode(DOMAIN_SEPARATOR_HEX);

        // Initialize SHA-256
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        // First hash: domain_separator + message
        sha256.update(DOMAIN_SEPARATOR);
        byte[] msgToHash = sha256.digest(message);

        // Get secp256k1 curve parameters
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        ECCurve curve = spec.getCurve();

        // Iterate counter from 0 to 2^16 - 1
        for (int counter = 0; counter < 65536; counter++) {
            // Convert counter to 4-byte little-endian
            byte[] counterBytes = new byte[4];
            counterBytes[0] = (byte) (counter);
            counterBytes[1] = (byte) (counter >> 8);
            counterBytes[2] = (byte) (counter >> 16);
            counterBytes[3] = (byte) (counter >> 24);

            // Compute SHA-256(msgToHash + counterBytes)
            sha256.reset();
            sha256.update(msgToHash);
            byte[] hashInput = sha256.digest(counterBytes);

            // Create candidate point (0x02 || hash_output)
            byte[] candidatePub = new byte[33];
            candidatePub[0] = 0x02; // Compressed format
            System.arraycopy(hashInput, 0, candidatePub, 1, 32);

            try {
                // Try to decode point (throws if not on curve)
                ECPoint point = curve.decodePoint(candidatePub);

                return point;
            } catch (IllegalArgumentException e) {
                // Point not on curve - try next counter
            }
        }
        throw new Exception("No valid point found after 2^16 iterations");
    }

    public static BlindingInfo createBlindMessage(String secret, BigInteger r) throws Exception {
        ECPoint Y = hashToCurve(secret.getBytes(StandardCharsets.UTF_8));
        ECPoint B_ = Y.add(G.multiply(r)).normalize();
        return new BlindingInfo(
                secret,
                bigIntToHex(r),
                pointToHex(B_),
                null,
                null
        );
    }


    public static String unblindSignature(String mintPublicKeyHex, String blindingFactorHex, String cPrimeHex) {
        ECPoint K = hexToPoint(mintPublicKeyHex);
        ECPoint Cprime = hexToPoint(cPrimeHex);
        BigInteger r = hexToBigInt(blindingFactorHex);

        ECPoint rK = K.multiply(r).normalize();
        ECPoint C = Cprime.subtract(rK).normalize();
        return pointToHex(C);
    }

    public static String signBlindMessage(String B_, String privateKey) {
        ECPoint blindMessage = hexToPoint(B_);
        BigInteger k = hexToBigInt(privateKey);

        ECPoint C_ = blindMessage.multiply(k).normalize();
        // dleq proof?
        return pointToHex(C_);
    }

    public static boolean verifySignature(String privateKey, String C, String secretMessage) throws Exception {
        ECPoint Y = hashToCurve(secretMessage.getBytes(StandardCharsets.UTF_8));
        BigInteger k = hexToBigInt(privateKey);

        return hexToPoint(C).equals(Y.multiply(k).normalize());
    }
}
