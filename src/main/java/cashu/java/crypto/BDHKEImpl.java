//package cashu.java.crypto;
//
//import java.math.BigInteger;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.util.Objects;
//
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECParameterSpec;
//import org.bouncycastle.math.ec.ECPoint;
//
//public class BDHKEImpl {
//
//    /**
//     * Wallet‑side implementation of the Cashu “hash‑to‑curve + blinded DH” protocol.
//     *
//     * <p>
//     *   The class is deliberately stateless – every method receives only the data
//     *   it needs and returns a plain Java object that contains the hex strings
//     *   required for the HTTP request / response handling.
//     * </p>
//     *
//     * <pre>
//     *   // --------------------------------------------------------------
//     *   // 1️⃣  Alice creates the blinded request that she will send to the mint
//     *   // --------------------------------------------------------------
//     *   String mintPubHex = "...";                // K = k·G   (obtained from /v1/keys)
//     *   String secretX    = "deadbeef...";        // any entropy you like
//     *
//     *   BlindResult blind = Wallet.blind(mintPubHex, secretX);
//     *
//     *   // POST to   /v1/{keyset_id}/issue/blind
//     *   //   body = {
//     *   //       "public_key": mintPubHex,
//     *   //       "secret":     blind.blindedPublicKeyHex,   // ← B_
//     *   //       "hash":       blind.hashToCurveHex,        // ← Y  (optional, only for debugging)
//     *   //       "blind":      blind.blindingFactorHex      // ← r  (you keep it locally)
//     *   //   }
//     *
//     *   // --------------------------------------------------------------
//     *   // 2️⃣  Mint replies with the blinded key C_ (hex string)
//     *   // --------------------------------------------------------------
//     *   String cPrimeHex = "...";                 // value returned by the mint
//     *
//     *   // --------------------------------------------------------------
//     *   // 3️⃣  Alice un‑blinds and obtains the final token (x , C)
//     *   // --------------------------------------------------------------
//     *   UnblindResult token = Wallet.unblind(mintPubHex,
//     *                                        secretX,
//     *                                        blind.blindingFactorHex,
//     *                                        cPrimeHex);
//     *
//     *   //   token.tokenSecretHex  = x   (the secret you chose)
//     *   //   token.tokenPointHex   = C   (the un‑blinded public key)
//     *
//     *   // Store (x , C) – this is the Cashu token that can later be spent.
//     * </pre>
//     *
//     * All heavy lifting (scalar multiplication, point addition, modulo‑order
//     * arithmetic, hash‑to‑curve) is performed inside the helper methods.
//     */
//    /* -------------------------------------------------------------- *
//     *   Curve constants – loaded once                                 *
//     * -------------------------------------------------------------- */
//    static {
//        java.security.Security.addProvider(new BouncyCastleProvider());
//    }
//
//    private static final String CURVE_NAME = "secp256k1";
//
//    /**
//     * Order of the curve (prime n).
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
//    private static final SecureRandom RNG = new SecureRandom();
//
//    static {
//        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
//        CURVE_ORDER = bcSpec.getN();          // n
//        G = bcSpec.getG();                    // generator
//    }
//
//    private BDHKEImpl() { /* static utility class */ }
//
//    /* -------------------------------------------------------------- *
//     *   Public data containers                                        *
//     * -------------------------------------------------------------- */
//
//    /**
//     * Result of the **blinding** step (the data that is sent to the mint).
//     */
//    public static final class BlindResult {
//        /**
//         * B_ = Y + r·G   (hex, un‑compressed SEC‑1).   Sent as “secret”.
//         */
//        public final String blindedPublicKeyHex;
//
//        /**
//         * r (the random blinding scalar) – keep this secret!
//         */
//        public final String blindingFactorHex;
//
//        /**
//         * Y = hash_to_curve(x) – optional, only for debugging / logging.
//         */
//        public final String secretHex;
//
//        private BlindResult(String blindedPublicKeyHex,
//                            String blindingFactorHex,
//                            String secretHex) {
//            this.blindedPublicKeyHex = blindedPublicKeyHex;
//            this.blindingFactorHex = blindingFactorHex;
//            this.secretHex = secretHex;
//        }
//
//        @Override
//        public String toString() {
//            return "BlindResult{blindedPublicKeyHex='" + blindedPublicKeyHex + '\'' +
//                    ", blindingFactorHex='" + blindingFactorHex + '\'' +
//                    ", secretHex='" + secretHex + '\'' + '}';
//        }
//    }
//
//    /**
//     * Result of the **un‑blinding** step (the final token).
//     */
//    public static final class UnblindResult {
//        /**
//         * The original secret that Alice chose (x).
//         */
//        public final String tokenSecretHex;      // same value that was passed to blind()
//
//        /**
//         * C = k·Y   (hex, un‑compressed SEC‑1).  This is the token’s public key.
//         */
//        public final String tokenPointHex;
//
//        private UnblindResult(String tokenSecretHex, String tokenPointHex) {
//            this.tokenSecretHex = tokenSecretHex;
//            this.tokenPointHex = tokenPointHex;
//        }
//
//        @Override
//        public String toString() {
//            return "UnblindResult{tokenSecretHex='" + tokenSecretHex + '\'' +
//                    ", tokenPointHex='" + tokenPointHex + '\'' + '}';
//        }
//    }
//
//    /* -------------------------------------------------------------- *
//     *   Core API                                                       *
//     * -------------------------------------------------------------- */
//
//    /**
//     * **Step 1 – Alice blinds the mint’s public key.**
//     *
//     * @param mintPublicKeyHex K = k·G  (un‑compressed SEC‑1, starts with “04”)
////     * @param secretXHex       the secret the wallet wants to embed in the token
//     * @return a {@link BlindResult} containing B_ (the value that is sent to the mint)
//     */
//    public static BlindResult blind(String mintPublicKeyHex) {
//        Objects.requireNonNull(mintPublicKeyHex, "mintPublicKeyHex");
////        String secretXHex = randomScalar().toString();
//
//        // ---- 1️⃣  decode the mint public key (only needed for sanity‑check) ----
//        ECPoint mintPk = decodePoint(mintPublicKeyHex);
//
//        // ---- 2️⃣  hash the secret into a curve point: Y = H→curve(x) ----
//        //        Here we simply hash the secret with SHA‑256, interpret the
//        //        digest as a scalar modulo the curve order, and multiply the
//        //        generator.  This is a widely‑used “hash‑to‑curve” construction
//        //        for secp256k1.
//        BigInteger secret = hashToScalar("deadbeefcafebabef00ddeadbeef1234567890abcdef1234567890abcdef");
//        ECPoint Y = G.multiply(secret).normalize();
//
//        // ---- 3️⃣  pick a random blinding factor r (mod n) ----
//        BigInteger r = randomScalar();
//
//        // ---- 4️⃣  compute the blinded public key B_ = Y + r·G ----
//        ECPoint B_ = Y.add(G.multiply(r)).normalize();
//
//        // Return everything the wallet needs later (r will be used for un‑blinding)
//        return new BlindResult(
//                pointToHex(B_),
//                bigIntToHex(r),
//                bigIntToHex(secret)          // optional – helps debugging
//        );
//    }
//
//    /**
//     * **Step 3 – Alice un‑blinds the value returned by the mint.**
//     *
//     * @param mintPublicKeyHex  K = k·G (hex, un‑compressed)
//     * @param secretXHex        the same secret that was used in {@link #blind}
//     * @param blindingFactorHex r (the value that was returned by {@link #blind})
//     * @param cPrimeHex         C_ = k·B_  (hex, un‑compressed) – the mint’s reply
//     * @return an {@link UnblindResult} containing the final token (x , C)
//     */
//    public static UnblindResult unblind(String mintPublicKeyHex,
//                                        String secretXHex,
//                                        String blindingFactorHex,
//                                        String cPrimeHex) {
//        Objects.requireNonNull(mintPublicKeyHex, "mintPublicKeyHex");
//        Objects.requireNonNull(secretXHex, "secretXHex");
//        Objects.requireNonNull(blindingFactorHex, "blindingFactorHex");
//        Objects.requireNonNull(cPrimeHex, "cPrimeHex");
//
//        // ---- decode everything we need ----
//        ECPoint K = decodePoint(mintPublicKeyHex);          // K = k·G
//        ECPoint Cp = decodePoint(cPrimeHex);                // C_ = k·B_
//        BigInteger r = hexToBigInt(blindingFactorHex);        // the blinding factor
//
//        // ---- 5️⃣  compute r·K  (this is the part we subtract) ----
//        ECPoint rK = K.multiply(r).normalize();
//
//        // ---- 6️⃣  un‑blind: C = C_ – r·K ----
//        ECPoint C = Cp.subtract(rK).normalize();               // C = k·Y
//
//        // ---- 7️⃣  return the token (x , C) ----
//        return new UnblindResult(secretXHex, pointToHex(C));
//    }
//
//    /* -------------------------------------------------------------- *
//     *   Low‑level helpers (private)                                   *
//     * -------------------------------------------------------------- */
//
//    /**
//     * Generate a random scalar 1 ≤ s < n.
//     */
//    private static BigInteger randomScalar() {
//        BigInteger s;
//        do {
//            s = new BigInteger(CURVE_ORDER.bitLength(), RNG);
//        } while (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(CURVE_ORDER) >= 0);
//        return s;
//    }
//
//    /**
//     * SHA‑256(x) → scalar mod n (used for hash‑to‑curve).
//     */
//    private static BigInteger hashToScalar(String hexInput) {
//        try {
//            MessageDigest sha = MessageDigest.getInstance("SHA-256");
//            sha.update(hexStringToBytes(hexInput));
//            return new BigInteger(1, sha.digest()).mod(CURVE_ORDER);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Decode an un‑compressed SEC‑1 point (hex → ECPoint).
//     */
//    private static ECPoint decodePoint(String hex) {
//        byte[] enc = hexStringToBytes(hex);
//        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
//                .getCurve()
//                .decodePoint(enc)
//                .normalize();
//    }
//
//    /**
//     * Encode an EC point to un‑compressed SEC‑1 hex (65 bytes, starts with “04”).
//     */
//    private static String pointToHex(ECPoint p) {
//        return bytesToHex(p.normalize().getEncoded(false));   // false → uncompressed
//    }
//
//    /**
//     * Convert a BigInteger to a lower‑case hex string (no leading “0x”).
//     */
//    private static String bigIntToHex(BigInteger v) {
//        return v.toString(16);
//    }
//
//    /**
//     * Hex string → byte[] (accepts upper‑ or lower‑case, optional “0x”).
//     */
//    private static byte[] hexStringToBytes(String s) {
//        String clean = s.startsWith("0x") ? s.substring(2) : s;
//        if (clean.length() % 2 != 0) clean = "0" + clean;
//        int len = clean.length() / 2;
//        byte[] out = new byte[len];
//        for (int i = 0; i < len; i++) {
//            out[i] = (byte) Integer.parseInt(clean.substring(2 * i, 2 * i + 2), 16);
//        }
//        return out;
//    }
//
//    /**
//     * Byte[] → lower‑case hex string.
//     */
//    private static String bytesToHex(byte[] b) {
//        StringBuilder sb = new StringBuilder(b.length * 2);
//        for (byte v : b) sb.append(String.format("%02x", v));
//        return sb.toString();
//    }
//
//    /**
//     * Hex → BigInteger (unsigned).
//     */
//    private static BigInteger hexToBigInt(String hex) {
//        return new BigInteger(hex, 16);
//    }
//
//    /* -------------------------------------------------------------- *
//     *   Demo – run this class to see the whole flow in action          *
//     * -------------------------------------------------------------- */
//    public static void main(String[] args) throws Exception {
//        System.out.println("\n=== Cashu wallet‑side blind / un‑blind demo (secp256k1) ===\n");
//
//        // -----------------------------------------------------------------
//        // 0️⃣  Mint public key (K = k·G).  In a real wallet you fetch this
//        //     from the mint’s /v1/keys endpoint.  Here we generate a dummy one.
//        // -----------------------------------------------------------------
//        BigInteger mintPriv = randomScalar();                 // k
//        ECPoint mintPub = G.multiply(mintPriv).normalize();  // K
//        String mintPubHex = pointToHex(mintPub);
//        System.out.println("[Mint] public key K = " + mintPubHex);
//
//        // -----------------------------------------------------------------
//        // 1️⃣  Alice creates a secret x and blinds the mint’s public key.
//        // -----------------------------------------------------------------
//        String secretXHex = "deadbeefcafebabef00ddeadbeef1234567890abcdef1234567890abcdef"; // any entropy
//        BlindResult blind = BDHKEImpl.blind(mintPubHex);
//        System.out.println("\n[Wallet] blind() result:");
//        System.out.println("  B_ (blinded public key) = " + blind.blindedPublicKeyHex);
//        System.out.println("  r (blinding factor)    = " + blind.blindingFactorHex);
//        System.out.println("  Y = H→curve(x)          = " + blind.secretHex);
//
//        // -----------------------------------------------------------------
//        // 2️⃣  Mint performs the DH step: C_ = k·B_
//        // -----------------------------------------------------------------
//        ECPoint Bprime = decodePoint(blind.blindedPublicKeyHex);
//        ECPoint Cprime = Bprime.multiply(mintPriv).normalize();   // C_ = k·B_
//        String cPrimeHex = pointToHex(Cprime);
//        System.out.println("\n[Mint] computed C_ = k·B_ = " + cPrimeHex);
//
//        // -----------------------------------------------------------------
//        // 3️⃣  Alice un‑blinds the result and obtains the token (x , C)
//        // -----------------------------------------------------------------
//        UnblindResult token = BDHKEImpl.unblind(mintPubHex,
//                secretXHex,
//                blind.blindingFactorHex,
//                cPrimeHex);
//        System.out.println("\n[Wallet] unblind() result (the token):");
//        System.out.println("  secret x          = " + token.tokenSecretHex);
//        System.out.println("  token point C    = " + token.tokenPointHex);
//
//        // -----------------------------------------------------------------
//        // 4️⃣  Verification (what the mint will do when the token is spent)
//        // -----------------------------------------------------------------
//        // Mint recomputes Y = H→curve(x) and checks k·Y == C
//        ECPoint Y = decodePoint(blind.secretHex);              // same Y we derived earlier
//        ECPoint kY = Y.multiply(mintPriv).normalize();              // k·Y
//        ECPoint C = decodePoint(token.tokenPointHex);
//
//        boolean ok = kY.equals(C);
//        System.out.println("\n[Mint] verification of the token (k·Y == C) : " + (ok ? "OK" : "FAIL"));
//    }
//}
