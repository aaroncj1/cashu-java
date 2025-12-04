//package cashu.java.crypto;
//
//import java.math.BigInteger;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.util.Objects;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
//import org.bouncycastle.jce.spec.ECParameterSpec;
//import org.bouncycastle.math.ec.ECCurve;
//import org.bouncycastle.math.ec.ECPoint;
//import org.bouncycastle.util.encoders.Hex;
//
///**
// * **Cashu “blinded‑DH / un‑blinding” helper** – works with the public test‑mint
// * at https://testnut.cashu.space.
// *
// * <p>
// * The flow is:
// *
// * <pre>
// *   1️⃣  Alice picks secret x
// *   2️⃣  Y = H→curve(x)
// *   3️⃣  r ← random scalar
// *   4️⃣  B_ = Y + r·G                (blinded public key, sent to mint)
// *   5️⃣  Mint returns C_ = k·B_     (blinded signature)
// *   6️⃣  Alice computes C = C_ – r·K = k·Y      (un‑blinded token key)
// *   </pre>
// * <p>
// * The class provides two static methods:
// *
// *   <ul>
//\ *         that must be POSTed to <code>/v1/{keyset_id}/swap</code>.</li>
//\ *         mint’s reply, the original secret x and the blinding factor r,
// *         returns the final token (x, C).</li>
// *   </ul>
// * <p>
// *   All values are returned as **lower‑case hex strings** (no “0x”).  Points
// *   are in **compressed SEC‑1** format (33 bytes, prefix 02/03) because the
// *   Cashu test‑mint expects that.
// * </p>
// */
//public final class CashuSwap {
//
//    /* ------------------------------------------------------------------ *
//     *   Curve constants – loaded once                                     *
//     * ------------------------------------------------------------------ */
//    static {
//        java.security.Security.addProvider(new BouncyCastleProvider());
//    }
//
//    private static final String CURVE_NAME = "secp256k1";
//
//    /**
//     * Order of the scalar field (prime n).
//     */
//    private static final BigInteger N;            // curve order
//
//    /**
//     * Generator point G (affine).
//     */
//    private static final ECPoint G;               // generator
//
//    /**
//     * Secure RNG for secrets & blinding factors.
//     */
//    private static final SecureRandom RNG = new SecureRandom();
//
//    static {
//        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
//        N = bcSpec.getN();
//        G = bcSpec.getG();
//    }
//
//    private CashuSwap() { /* utility class */ }
//
//    /* ------------------------------------------------------------------ *
//     *   Public data containers                                            *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * Result of the **blinding** step – everything that must be kept locally.
//     */
//    public static final class BlindInfo {
//        /**
//         * The secret that will become the token’s serial number (x) – hex.
//         */
//        public final String secretXHex;
//
//
//        /**
//         * Random blinding factor r – hex (must be stored for un‑blinding).
//         */
//        public final String blindingFactorHex;
//
//        /**
//         * B_ = Y + r·G – the value that is sent to the mint (compressed).
//         */
//        public final String blindedPublicKeyHex;
//
//        BlindInfo(String secretXHex,
//                  String blindingFactorHex,
//                  String blindedPublicKeyHex) {
//            this.secretXHex = secretXHex;
//            this.blindingFactorHex = blindingFactorHex;
//            this.blindedPublicKeyHex = blindedPublicKeyHex;
//        }
//
//        @Override
//        public String toString() {
//            return "BlindInfo{secretXHex='" + secretXHex + '\'' +
//                    ", blindingFactorHex='" + blindingFactorHex + '\'' +
//                    ", blindedPublicKeyHex='" + blindedPublicKeyHex + '\'' + '}';
//        }
//    }
//
//    /**
//     * Result of the **un‑blinding** step – the final token.
//     */
//    public static final class Token {
//        /**
//         * The secret x that the wallet chose (hex).
//         */
//        public final String secretXHex;
//
//        /**
//         * C = k·Y – the public part of the token (compressed SEC‑1 hex).
//         */
//        public final String tokenPublicKeyHex;
//
//        Token(String secretXHex, String tokenPublicKeyHex) {
//            this.secretXHex = secretXHex;
//            this.tokenPublicKeyHex = tokenPublicKeyHex;
//        }
//
//        @Override
//        public String toString() {
//            return "Token{secretXHex='" + secretXHex + '\'' +
//                    ", tokenPublicKeyHex='" + tokenPublicKeyHex + '\'' + '}';
//        }
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Core API                                                         *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * **Step 1** – generate the data that will be sent to the mint.
//     *
//     * @param mintPublicKeyHex K = k·G (un‑compressed SEC‑1 hex, starts with “04”)
//     * @param secretXHex       any random 32‑byte value you want to use as the token’s secret
//     * @return a {@link BlindInfo} object containing the blinded public key (B_)
//     * and everything you must keep locally for the un‑blinding step.
//     */
//    public static BlindInfo createBlindInfo(String mintPublicKeyHex, String secretXHex) throws Exception {
//        Objects.requireNonNull(mintPublicKeyHex);
//        Objects.requireNonNull(secretXHex);
//
//        // -----------------------------------------------------------------
//        // 1️⃣  Map the secret x → curve point Y = H→curve(x)
//        //      We use the simple “scalar‑multiply‑G” hash‑to‑curve construction:
//        //          y = SHA256(x)   (mod n)
//        //          Y = y·G
//        // -----------------------------------------------------------------
//        ECPoint Y = hashToCurve(secretXHex.getBytes(StandardCharsets.UTF_8));
//
//        // -----------------------------------------------------------------
//        // 2️⃣  Pick a random blinding factor r ∈ [1 , n‑1]
//        // -----------------------------------------------------------------
//        BigInteger r = randomScalar();
//
//        // -----------------------------------------------------------------
//        // 3️⃣  Compute the blinded public key B_ = Y + r·G
//        // -----------------------------------------------------------------
//        ECPoint B_ = Y.add(G.multiply(r)).normalize();
//
//        return new BlindInfo(
//                secretXHex,
//                bigIntToHex(r),
//                pointToCompressedHex(B_)                 // ← this is sent to the mint
//        );
//    }
//
//    /**
//     * Build the **JSON body** that must be POSTed to the mint’s
//     * <code>/v1/{keyset_id}/swap</code> endpoint.
//     *
//     * @param mintPublicKeyHex K (un‑compressed hex, as returned by /v1/keys)
//     * @param blindInfo        the object returned by {@link #createBlindInfo}
//     * @return a Jackson {@link ObjectNode} that can be sent as the request payload.
//     */
//    public static ObjectNode buildSwapRequest(String mintPublicKeyHex, BlindInfo blindInfo) {
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode body = mapper.createObjectNode();
//        body.put("public_key", mintPublicKeyHex);                 // K
//        body.put("secret", blindInfo.blindedPublicKeyHex);        // B_
//        return body;
//    }
//
//    /**
//     * **Step 2** – un‑blind the value returned by the mint.
//     *
//     * @param mintPublicKeyHex K = k·G (un‑compressed hex)
//     * @param blindInfo        the {@link BlindInfo} you kept from the first step
////     * @param mintReplyJson    the raw JSON string returned by the mint
//     * @return a {@link Token} containing the final token (x , C)
//     */
//    public static Token unblind(String mintPublicKeyHex,
//                                BlindInfo blindInfo,
//                                String cPrimeHex) throws Exception {
//
//        // -----------------------------------------------------------------
//        // Decode everything we need
//        // -----------------------------------------------------------------
//        ECPoint K = decodeUncompressedPoint(mintPublicKeyHex);   // K = k·G
//        ECPoint Cprime = decodeCompressedPoint(cPrimeHex);            // C_ = k·B_
//        BigInteger r = hexToBigInt(blindInfo.blindingFactorHex);    // the blinding scalar
//
//        // -----------------------------------------------------------------
//        // Compute r·K
//        // -----------------------------------------------------------------
//        ECPoint rK = K.multiply(r).normalize();
//
//        // -----------------------------------------------------------------
//        // Un‑blind:   C = C_ – r·K   ( = k·Y )
//        // -----------------------------------------------------------------
//        ECPoint C = Cprime.subtract(rK).normalize();
//
//        // -----------------------------------------------------------------
//        // Return the final token (x , C)
//        // -----------------------------------------------------------------
//        return new Token(blindInfo.secretXHex, pointToCompressedHex(C));
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Low‑level helpers (private)                                      *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * Random scalar in the interval [1 , N‑1].
//     */
//    private static BigInteger randomScalar() {
//        BigInteger s;
//        do {
//            s = new BigInteger(N.bitLength(), RNG);
//        } while (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(N) >= 0);
//        return s;
//    }
//
//    public static ECPoint hashToCurve(byte[] message) throws Exception {
//        // Domain separator hex (from Python specification)
//        final String DOMAIN_SEPARATOR_HEX = "536563703235366b315f48617368546f43757276655f43617368755f";
//        final byte[] DOMAIN_SEPARATOR = Hex.decode(DOMAIN_SEPARATOR_HEX);
//
//        // Initialize SHA-256
//        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
//
//        // First hash: domain_separator + message
//        sha256.update(DOMAIN_SEPARATOR);
//        byte[] msgToHash = sha256.digest(message);
//
//        // Get secp256k1 curve parameters
//        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
//        ECCurve curve = spec.getCurve();
//
//        // Iterate counter from 0 to 2^16 - 1
//        for (int counter = 0; counter < 65536; counter++) {
//            // Convert counter to 4-byte little-endian
//            byte[] counterBytes = new byte[4];
//            counterBytes[0] = (byte) (counter);
//            counterBytes[1] = (byte) (counter >> 8);
//            counterBytes[2] = (byte) (counter >> 16);
//            counterBytes[3] = (byte) (counter >> 24);
//
//            // Compute SHA-256(msgToHash + counterBytes)
//            sha256.reset();
//            sha256.update(msgToHash);
//            byte[] hashInput = sha256.digest(counterBytes);
//
//            // Create candidate point (0x02 || hash_output)
//            byte[] candidatePub = new byte[33];
//            candidatePub[0] = 0x02; // Compressed format
//            System.arraycopy(hashInput, 0, candidatePub, 1, 32);
//
//            try {
//                // Try to decode point (throws if not on curve)
//                ECPoint point = curve.decodePoint(candidatePub);
//
//                return point;
//            } catch (IllegalArgumentException e) {
//                // Point not on curve - try next counter
//            }
//        }
//        throw new Exception("No valid point found after 2^16 iterations");
//    }
//
//    /**
//     * SHA‑256(x) → scalar (mod N).  Input is a hex string.
//     */
//    private static BigInteger hashToScalar(String hexInput) {
//        try {
//            MessageDigest sha = MessageDigest.getInstance("SHA-256");
//            sha.update(hexStringToBytes(hexInput));
//            return new BigInteger(1, sha.digest()).mod(N);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static String bigIntToHex(BigInteger v) {
//        return v.toString(16);
//    }
//
//    /**
//     * Encode EC point to **compressed** SEC‑1 hex (33 bytes).
//     */
//    private static String pointToCompressedHex(ECPoint p) {
//        return bytesToHex(p.normalize().getEncoded(true));   // true → compressed
//    }
//
//    /**
//     * Decode an **un‑compressed** point (hex, starts with 04).
//     */
//    private static ECPoint decodeUncompressedPoint(String hex) {
//        byte[] enc = hexStringToBytes(hex);
//        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
//                .getCurve()
//                .decodePoint(enc)
//                .normalize();
//    }
//
//    /**
//     * Decode a **compressed** point (hex, starts with 02 or 03).
//     */
//    private static ECPoint decodeCompressedPoint(String hex) {
//        byte[] enc = hexStringToBytes(hex);
//        return ECNamedCurveTable.getParameterSpec(CURVE_NAME)
//                .getCurve()
//                .decodePoint(enc)
//                .normalize();
//    }
//
//    /**
//     * Hex string → byte[] (accepts optional “0x”).
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
//    /* ------------------------------------------------------------------ *
//     *   Simple HTTP helper (you can replace with OkHttp, Apache, etc.)    *
//     * ------------------------------------------------------------------ */
//
//    /**
//     * POST a JSON object to the given URL and return the response body as a string.
//     */
//    private static String httpPost(String urlString, ObjectNode jsonBody) throws Exception {
//        byte[] payload = jsonBody.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
//        URL url = new URL(urlString);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//        conn.setRequestProperty("Content-Type", "application/json");
//        try (java.io.OutputStream os = conn.getOutputStream()) {
//            os.write(payload);
//        }
//        int code = conn.getResponseCode();
//        java.io.InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
//        String response = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
//        if (code != 200) {
//            throw new RuntimeException("Mint returned HTTP " + code + ": " + response);
//        }
//        return response;
//    }
//
//    /* ------------------------------------------------------------------ *
//     *   Demo – run the whole flow against the public test‑mint            *
//     * ------------------------------------------------------------------ */
//    public static void main(String[] args) throws Exception {
//        System.out.println("\n=== Cashu /swap demo (secp256k1) ===\n");
//
//        // -----------------------------------------------------------------
//        // 0️⃣  Obtain the mint public key (K).  In a real wallet you would GET
//        //     https://testnut.cashu.space/v1/keys and pick a key‑set id.
//        // -----------------------------------------------------------------
//        // For the demo we hard‑code a known test‑mint key (un‑compressed form).
//        // This key is taken from the test‑mint’s /v1/keys output (keyset “USD‑1”).
//        String mintPublicKeyHex =
//                "04e3c7f390647fe527251e0b417ab125fac9cde7f21a37c0c51ab8702bab1e825856cda254a9bc7d5bd8c2b020a0fd0fa592739ce5d5e4f6c0cfa58db9c3db6c5b";
//        // The key‑set identifier for this key (choose one that the mint advertises)
//        String keysetId = "USD-1";
//
//        // -----------------------------------------------------------------
//        // 1️⃣  Choose a random secret x (32‑byte hex).  In a real wallet you
//        //     would store this value as the token's “serial number”.
//        // -----------------------------------------------------------------
//        byte[] xBytes = new byte[32];
//        RNG.nextBytes(xBytes);
//        String secretXHex = bytesToHex(xBytes);
//        System.out.println("[Wallet] secret x (hex) = " + secretXHex);
//
//        // -----------------------------------------------------------------
//        // 2️⃣  Build the blinded request (B_)
//        // -----------------------------------------------------------------
//        BlindInfo blindInfo = createBlindInfo(mintPublicKeyHex, secretXHex);
//        System.out.println("[Wallet] blind info = " + blindInfo);
//
//        // Build JSON body for /swap
//        ObjectNode requestJson = buildSwapRequest(mintPublicKeyHex, blindInfo);
//        System.out.println("\n[Wallet] POST body for /swap:");
//        System.out.println(requestJson.toPrettyString());
//
//        // -----------------------------------------------------------------
//        // 3️⃣  POST to the mint
//        // -----------------------------------------------------------------
//        String url = "https://testnut.cashu.space/v1/" + keysetId + "/swap";
//        String mintReply = httpPost(url, requestJson);
//        System.out.println("\n[Mint] reply = " + mintReply);
//
//        // -----------------------------------------------------------------
//        // 4️⃣  Un‑blind the reply → final token (x , C)
//        // -----------------------------------------------------------------
//        Token token = unblind(mintPublicKeyHex, blindInfo, mintReply);
//        System.out.println("\n[Wallet] final token:");
//        System.out.println("   secret x : " + token.secretXHex);
//        System.out.println("   token C : " + token.tokenPublicKeyHex);
//        System.out.println("\nYou can now spend this token by sending {\"secret\":\"" +
//                token.secretXHex + "\",\"public_key\":\"" + token.tokenPublicKeyHex + "\"} to the mint's /redeem endpoint.");
//    }
//}
