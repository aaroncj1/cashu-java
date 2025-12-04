//package cashu.java.crypto;
//
//import java.math.BigInteger;
//import java.security.SecureRandom;
//import java.util.Objects;
//
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECParameterSpec;          // Bouncy‑Castle type
//import org.bouncycastle.math.ec.ECPoint;
//
//
//public class ECBlind {
//
//    /**
//     * **EC blind‑/un‑blind helper**
//     *
//     * <p>
//     *   • Input:  mint public key as an un‑compressed hex string
//     *   • Output: blinded public key, the blinding factor (secret) and the original key
//     * </p>
//     *
//     * <p>
//     *   The maths are:   {@code R = b·P}   (blinding)
//     *                     {@code P = b⁻¹·R} (un‑blinding)
//     *   where {@code P} is the mint’s public key and {@code b} is a random scalar
//     *   in the range {@code [1 , n‑1]}, {@code n} being the order of the secp256k1
//     *   curve.
//     * </p>
//     */
//
//    /* ------------------------------------------------------------------ *
//     *   Curve constants – loaded once                                      *
//     * ------------------------------------------------------------------ */
//    static {
//        java.security.Security.addProvider(new BouncyCastleProvider());
//    }
//
//    private static final String CURVE_NAME = "secp256k1";
//
//    /**
//     * Curve order (the scalar field size).
//     */
//    private static final BigInteger CURVE_ORDER;
//
//    /**
//     * Generator point G (affine).
//     */
//    private static final ECPoint G;
//
//    /**
//     * Secure RNG for the blinding factor.
//     */
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//    static {
//        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
//        CURVE_ORDER = bcSpec.getN();          // n
//        G = bcSpec.getG();                    // generator (not used directly here)
//    }
//
//    /**
//     * Private constructor – utility class only.
//     */
//    private ECBlind() {
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Public data containers                                            *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * Result of the *blinding* operation.
//     */
//    public static final class BlindResult {
//        /**
//         * Hex string of the blinded public key (un‑compressed SEC‑1).
//         */
//        public final String blindedPublicKeyHex;
//
//        /**
//         * Hex string of the secret blinding factor {@code b}.
//         */
//        public final String blindingFactorHex;
//
//        /**
//         * Hex string of the *nonce* that was used to build the commitment
//         * (the same value you will later need when you finish the Cashu‑token).
//         */
//        public final String secretScalarHex;
//
//        private BlindResult(String blindedPublicKeyHex,
//                            String blindingFactorHex,
//                            String secretScalarHex) {
//            this.blindedPublicKeyHex = blindedPublicKeyHex;
//            this.blindingFactorHex = blindingFactorHex;
//            this.secretScalarHex = secretScalarHex;
//        }
//
//        @Override
//        public String toString() {
//            return "BlindResult{blindedPublicKeyHex='" + blindedPublicKeyHex + '\'' +
//                    ", blindingFactorHex='" + blindingFactorHex + '\'' +
//                    ", secretScalarHex='" + secretScalarHex + '\'' + '}';
//        }
//    }
//
//    /**
//     * Result of the *un‑blinding* operation.
//     */
//    public static final class UnblindResult {
//        /**
//         * Hex string of the original (un‑blinded) public key.
//         */
//        public final String originalPublicKeyHex;
//
//        private UnblindResult(String originalPublicKeyHex) {
//            this.originalPublicKeyHex = originalPublicKeyHex;
//        }
//
//        @Override
//        public String toString() {
//            return "UnblindResult{originalPublicKeyHex='" + originalPublicKeyHex + '\'' + '}';
//        }
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Core operations                                                  *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * **Blinds** a mint public key.
//     *
//     * @param mintPublicKeyHex un‑compressed SEC‑1 representation of the
//     *                         mint’s public key (e.g. <code>04a1b2…</code>)
//     * @return an object that contains
//     * <ul>
//     *   <li>the blinded public key (hex)</li>
//     *   <li>the blinding factor {@code b} (hex) – keep this secret!</li>
//     *   <li>a random “secret scalar” {@code r} (hex) that you will later
//     *       use as the token’s nonce when you finish the Cashu flow.</li>
//     * </ul>
//     */
//    public static BlindResult blind(String mintPublicKeyHex) {
//        Objects.requireNonNull(mintPublicKeyHex, "mintPublicKeyHex must not be null");
//
//        // 1️⃣  decode the mint’s public key (SEC‑1, un‑compressed)
//        ECPoint mintPk = decodePoint(mintPublicKeyHex);
//
//        // 2️⃣  generate a random blinding factor b ∈ [1,n‑1]
//        BigInteger b = randomScalar();
//
//        // 3️⃣  compute the blinded point R = b·P
//        ECPoint blinded = mintPk.multiply(b).normalize();
//
//        // 4️⃣  generate a *secret* scalar r (the nonce you will later use
//        //     when you finish the Cashu token).  It is unrelated to the
//        //     blinding factor – we return it so you have it handy.
//        BigInteger r = randomScalar();
//
//        return new BlindResult(
//                pointToHex(blinded),
//                bigIntToHex(b),
//                bigIntToHex(r)
//        );
//    }
//
//    /**
//     * **Un‑blinds** a previously blinded public key.
//     *
//     * <p>
//     * The caller must supply the same blinding factor {@code b} that was
//     * returned by {@link #blind(String)}.
//     * </p>
//     *
//     * @param blindedPublicKeyHex the blinded point (hex, un‑compressed)
//     * @param blindingFactorHex   the blinding factor {@code b} that was used
//     * @return the original mint public key (hex, un‑compressed)
//     */
//    public static UnblindResult unblind(String blindedPublicKeyHex,
//                                        String blindingFactorHex) {
//        Objects.requireNonNull(blindedPublicKeyHex, "blindedPublicKeyHex must not be null");
//        Objects.requireNonNull(blindingFactorHex, "blindingFactorHex must not be null");
//
//        ECPoint blinded = decodePoint(blindedPublicKeyHex);
//        BigInteger b = hexToBigInt(blindingFactorHex);
//
//        // Compute inverse of b modulo the curve order: b⁻¹
//        BigInteger bInv = b.modInverse(CURVE_ORDER);
//
//        // Original point = b⁻¹·R
//        ECPoint original = blinded.multiply(bInv).normalize();
//
//        return new UnblindResult(pointToHex(original));
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Low‑level helpers (private)                                      *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * Generate a random scalar in the range [1 , n‑1].
//     */
//    private static BigInteger randomScalar() {
//        BigInteger r;
//        do {
//            r = new BigInteger(CURVE_ORDER.bitLength(), SECURE_RANDOM);
//        } while (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(CURVE_ORDER) >= 0);
//        return r;
//    }
//
//    /**
//     * Decode an un‑compressed SEC‑1 point (hex → ECPoint).
//     */
//    private static ECPoint decodePoint(String hex) {
//        byte[] encoded = hexStringToBytes(hex);
//        // Bouncy‑Castle can rebuild the point from the byte array
//        ECPoint point = ECNamedCurveTable.getParameterSpec(CURVE_NAME)
//                .getCurve()
//                .decodePoint(encoded)
//                .normalize();
//        if (point.isInfinity())
//            throw new IllegalArgumentException("Point at infinity is not allowed");
//        return point;
//    }
//
//    /**
//     * Convert an EC point to an un‑compressed hex string (SEC‑1).
//     */
//    private static String pointToHex(ECPoint p) {
//        return bytesToHex(p.normalize().getEncoded(false));   // false = uncompressed
//    }
//
//    /**
//     * Convert a {@link BigInteger} to a lower‑case hex string (no leading 0x).
//     */
//    private static String bigIntToHex(BigInteger v) {
//        return v.toString(16);
//    }
//
//    /**
//     * Convert a hex string to a {@link BigInteger}.
//     */
//    private static BigInteger hexToBigInt(String hex) {
//        return new BigInteger(hex, 16);
//    }
//
//    /**
//     * Hex string → byte[] (accepts upper‑ or lower‑case).
//     */
//    private static byte[] hexStringToBytes(String s) {
//        String clean = s.startsWith("0x") ? s.substring(2) : s;
//        if (clean.length() % 2 != 0) clean = "0" + clean; // pad if odd
//        int len = clean.length() / 2;
//        byte[] out = new byte[len];
//        for (int i = 0; i < len; i++) {
//            out[i] = (byte) Integer.parseInt(clean.substring(2 * i, 2 * i + 2), 16);
//        }
//        return out;
//    }
//
//    /**
//     * Byte[] → hex string (lower‑case).
//     */
//    private static String bytesToHex(byte[] data) {
//        StringBuilder sb = new StringBuilder(data.length * 2);
//        for (byte b : data) sb.append(String.format("%02x", b));
//        return sb.toString();
//    }
//}
//
//
