package cashu.java.crypto;

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
import java.security.SecureRandom;
import java.util.Objects;

public class BDHKEServiceSecp256k1Impl implements BDHKEService {

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

    /**
     * Secure RNG for secrets & blinding factors.
     */
    private static final SecureRandom RNG = new SecureRandom();

    static {
        java.security.Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        N = bcSpec.getN();
        G = bcSpec.getG();
    }

    @Override
    public BlindingInfo wallet_blind(String mintPublicKeyHex) throws Exception {
        Objects.requireNonNull(mintPublicKeyHex);
        String secret = randomHex();

        ECPoint Y = hashToCurve(secret.getBytes(StandardCharsets.UTF_8));

        BigInteger r = randomScalar();
        ECPoint B_ = Y.add(G.multiply(r)).normalize();

        return new BlindingInfo(
                secret,
                bigIntToHex(r),
                pointToCompressedHex(B_)
        );
    }

    @Override
    public String wallet_unblind(String mintPublicKeyHex, String blindingFactorHex, String cPrimeHex) {
        ECPoint K = decodeUncompressedPoint(mintPublicKeyHex);
        ECPoint Cprime = decodeCompressedPoint(cPrimeHex);
        BigInteger r = hexToBigInt(blindingFactorHex);

        ECPoint rK = K.multiply(r).normalize();
        ECPoint C = Cprime.subtract(rK).normalize();
        return pointToCompressedHex(C);
    }

    @Override
    public String mint_sign(String B_, String privateKey) {
        ECPoint blindMessage = decodeCompressedPoint(B_);
        BigInteger k = hexToBigInt(privateKey);

        ECPoint C_ = blindMessage.multiply(k).normalize();
        // dleq proof?
        return pointToCompressedHex(C_);
    }

    @Override
    public boolean mint_verify(String privateKey, String C, String secretMessage) throws Exception {
        ECPoint Y = hashToCurve(secretMessage.getBytes(StandardCharsets.UTF_8));
        BigInteger k = hexToBigInt(privateKey);

        return decodeCompressedPoint(C).equals(Y.multiply(k).normalize());
    }

    private static ECPoint hashToCurve(byte[] message) throws Exception {
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
    private static BigInteger randomScalar() {
        BigInteger s;
        do {
            s = new BigInteger(N.bitLength(), RNG);
        } while (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(N) >= 0);
        return s;
    }

    private static String bigIntToHex(BigInteger v) {
        return v.toString(16);
    }

    /**
     * Encode EC point to **compressed** SEC‑1 hex (33 bytes).
     */
    public static String pointToCompressedHex(ECPoint p) {
        return bytesToHex(p.normalize().getEncoded(true));   // true → compressed
    }

    private static ECPoint decodeUncompressedPoint(String hex) {
        byte[] enc = hexStringToBytes(hex);
        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
                .getCurve()
                .decodePoint(enc)
                .normalize();
    }

    /**
     * Decode a **compressed** point (hex, starts with 02 or 03).
     */
    private static ECPoint decodeCompressedPoint(String hex) {
        byte[] enc = hexStringToBytes(hex);
        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
                .getCurve()
                .decodePoint(enc)
                .normalize();
    }

    private static byte[] hexStringToBytes(String s) {
        String clean = s.startsWith("0x") ? s.substring(2) : s;
        if (clean.length() % 2 != 0) clean = "0" + clean;
        int len = clean.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = (byte) Integer.parseInt(clean.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }

    /**
     * Byte[] → lower‑case hex string.
     */
    public static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    /**
     * Hex → BigInteger (unsigned).
     */
    private static BigInteger hexToBigInt(String hex) {
        return new BigInteger(hex, 16);
    }
}
