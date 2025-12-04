package cashu.java.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.AbstractMap;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;          // Bouncy‑Castle version
import org.bouncycastle.math.ec.ECPoint;

public class v2bdhke {


    /**
     * **Blind Schnorr signature** on the secp256k1 curve.
     *
     * <p>Public API (all static):</p>
     *
     * <ul>
     *   <li>{@link #bobGenerateCommitment(BobKeyPair)} – creates a nonce R=k·G (Bob keeps k).</li>
     *   <li>{@link #aliceCreateChallenge(ECPoint, ECPoint, byte[])} – computes e = H(R‖pk‖msg)
     *       and a blinded version e' = e + b (mod n).</li>
     *   <li>{@link #bobSignBlinded(BobSigningSession, BigInteger)} – Bob signs e' producing s' = k + e'·sk.</li>
     *   <li>{@link #aliceUnblind(BigInteger, AliceState, BobKeyPair)} – Alice removes the blinding factor,
     *       producing the ordinary Schnorr signature (R, s).</li>
     *   <li>{@link #verifySignature(SchnorrSignature, ECPoint, byte[])} – ordinary verification.</li>
     * </ul>
     * <p>
     * The {@code main} method demonstrates a full end‑to‑end run.
     */

    /* --------------------------------------------------------------------- *
     *   Static initialisation – load curve parameters once                     *
     * --------------------------------------------------------------------- */
    static {
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    private static final String CURVE_NAME = "secp256k1";

    /**
     * Order of the scalar field (prime n).
     */
    public static final BigInteger CURVE_ORDER;          // n

    /**
     * Generator point G (affine).
     */
    public static final ECPoint G;                       // generator

    /**
     * Secure RNG for all secret material.
     */
    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static {
        ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
        CURVE_ORDER = bcSpec.getN();
        G = bcSpec.getG();
    }

    /* --------------------------------------------------------------------- *
     *   Helper utilities                                                    *
     * --------------------------------------------------------------------- */

    /**
     * Generate a random scalar in the range [1 , n‑1].
     */
    public static BigInteger randomScalar() {
        BigInteger r;
        do {
            r = new BigInteger(CURVE_ORDER.bitLength(), SECURE_RANDOM);
        } while (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(CURVE_ORDER) >= 0);
        return r;
    }

    /**
     * Compute the Schnorr challenge e = H(R‖pk‖msg) (mod n).
     */
    public static BigInteger challenge(ECPoint R, ECPoint pk, byte[] msg) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(R.normalize().getEncoded(false));
            sha256.update(pk.normalize().getEncoded(false));
            sha256.update(msg);
            byte[] digest = sha256.digest();
            return new BigInteger(1, digest).mod(CURVE_ORDER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hex conversion – only for pretty printing.
     */
    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    /**
     * Pretty‑print an EC point (uncompressed).
     */
    private static String pointToHex(ECPoint p) {
        return bytesToHex(p.normalize().getEncoded(false));
    }

    /* --------------------------------------------------------------------- *
     *   Data structures used by the protocol                                 *
     * --------------------------------------------------------------------- */

    /**
     * Bob’s long‑term key pair.
     */
    public static final class BobKeyPair {
        public final BigInteger sk;   // private scalar
        public final ECPoint pk;   // public point = sk·G

        private BobKeyPair(BigInteger sk, ECPoint pk) {
            this.sk = sk;
            this.pk = pk;
        }

        /**
         * Generate a fresh secp256k1 key pair.
         */
        public static BobKeyPair generate() {
            BigInteger sk = randomScalar();
            ECPoint pk = G.multiply(sk).normalize();
            return new BobKeyPair(sk, pk);
        }
    }

    /**
     * The signing session that Bob creates – it holds the nonce k (private).
     */
    public static final class BobSigningSession {
        public final ECPoint commitment;   // R = k·G (sent to Alice)
        private final BigInteger k;       // Bob’s secret nonce (kept inside the session)

        private BobSigningSession(ECPoint commitment, BigInteger k) {
            this.commitment = commitment;
            this.k = k;
        }

        /**
         * Compute s' = k + e'·sk (mod n).
         */
        private BigInteger computeSignature(BigInteger ePrime, BobKeyPair bob) {
            return k.add(ePrime.multiply(bob.sk)).mod(CURVE_ORDER);
        }
    }

    /**
     * What Alice sends to Bob after receiving R: a blinded challenge e'.
     */
    public static final class AliceBlindedChallenge {
        public final BigInteger ePrime;   // e' = e + b (mod n)

        private AliceBlindedChallenge(BigInteger ePrime) {
            this.ePrime = ePrime;
        }
    }

    /**
     * What Bob returns to Alice: the scalar s' = k + e'·sk.
     */
    public static final class BobBlindedSignature {
        public final BigInteger sPrime;   // s' (mod n)

        private BobBlindedSignature(BigInteger sPrime) {
            this.sPrime = sPrime;
        }
    }

    /**
     * The final ordinary Schnorr signature that any verifier can check.
     */
    public static final class SchnorrSignature {
        public final ECPoint R;   // commitment point (R = k·G)
        public final BigInteger s; // signature scalar

        private SchnorrSignature(ECPoint R, BigInteger s) {
            this.R = R;
            this.s = s;
        }
    }

    /**
     * State that Alice must keep between the three steps.
     */
    public static final class AliceState {
        public final BigInteger blinding;    // b
        public final BigInteger e;           // normal challenge e = H(R‖pk‖msg)

        private AliceState(BigInteger blinding, BigInteger e) {
            this.blinding = blinding;
            this.e = e;
        }
    }

    /* --------------------------------------------------------------------- *
     *   PUBLIC API                                                          *
     * --------------------------------------------------------------------- */

    /**
     * **Step 1 (Bob).**  Create a fresh nonce, compute the commitment point R = k·G,
     * and return a {@link BobSigningSession} that contains R (to be sent to Alice)
     * and keeps k private.
     */
    public static BobSigningSession bobGenerateCommitment(BobKeyPair bob) {
        BigInteger k = randomScalar();
        ECPoint R = G.multiply(k).normalize();
        return new BobSigningSession(R, k);
    }

    /**
     * **Step 2 (Alice).**  Given the commitment R that Bob sent, compute the ordinary
     * challenge e = H(R‖pk‖msg), pick a random blinding factor b, and return both
     * the blinded challenge e' = e + b (mod n) **and** the state that will be needed
     * later for un‑blinding.
     *
     * @param R   commitment point sent by Bob
     * @param pk  Bob's public key
     * @param msg message to be signed
     * @return pair {blindedChallenge, aliceState}
     */
    public static AbstractMap.SimpleEntry<AliceBlindedChallenge, AliceState>
    aliceCreateChallenge(ECPoint R, ECPoint pk, byte[] msg) {
        BigInteger e = challenge(R, pk, msg);          // ordinary challenge
        BigInteger b = randomScalar();                // blinding factor
        BigInteger ePrime = e.add(b).mod(CURVE_ORDER); // blinded challenge
        return new AbstractMap.SimpleEntry<>(new AliceBlindedChallenge(ePrime),
                new AliceState(b, e));
    }

    /**
     * **Step 3 (Bob).**  Sign the blinded challenge e' using the nonce k that belongs
     * to the previously created session.
     *
     * @param session the signing session (contains k and R)
     * @param ePrime  blinded challenge e' received from Alice
     * @param bob     Bob's long‑term key pair
     * @return the blinded signature scalar s' = k + e'·sk (mod n)
     */
    public static BobBlindedSignature bobSignBlinded(BobSigningSession session,
                                                     BigInteger ePrime,
                                                     BobKeyPair bob) {
        BigInteger sPrime = session.computeSignature(ePrime, bob);
        return new BobBlindedSignature(sPrime);
    }

    /**
     * **Step 4 (Alice).**  Un‑blind Bob's response and obtain a normal Schnorr
     * signature (R, s).  The formula is
     * <p>
     * s = s' – b·sk   (mod n)
     *
     * @param blindedSig Bob's blinded signature (s')
     * @param aliceState Alice's saved state (b, e)
     * @param bob        Bob's public key (only the private part sk is needed for un‑blinding)
     * @return ordinary Schnorr signature
     */
    public static SchnorrSignature aliceUnblind(BobBlindedSignature blindedSig,
                                                AliceState aliceState,
                                                BobKeyPair bob,
                                                ECPoint commitmentR) {
        // s = s' – b·sk   (mod n)
        BigInteger s = blindedSig.sPrime
                .subtract(aliceState.blinding.multiply(bob.sk))
                .mod(CURVE_ORDER);
        return new SchnorrSignature(commitmentR, s);
    }

    /**
     * Verify a *normal* Schnorr signature (R, s) against a public key.
     *
     * @param sig signature to verify
     * @param pk  public key of the signer
     * @param msg original message bytes
     * @return true if the signature is valid
     */
    public static boolean verifySignature(SchnorrSignature sig,
                                          ECPoint pk,
                                          byte[] msg) {
        BigInteger e = challenge(sig.R, pk, msg);
        ECPoint left = G.multiply(sig.s).normalize();           // s·G
        ECPoint right = sig.R.add(pk.multiply(e)).normalize();   // R + e·pk
        return left.equals(right);
    }

    /* --------------------------------------------------------------------- *
     *   Demo driver (run this class)                                         *
     * --------------------------------------------------------------------- */
    public static void main(String[] args) {
        System.out.println("\n=== Blind Schnorr Signature Demo (secp256k1) ===\n");

        // ------------------------------------------------- 1️⃣  Bob creates a long‑term key pair
        BobKeyPair bob = BobKeyPair.generate();
        System.out.println("[Bob] private key (sk) = " + bob.sk.toString(16));
        System.out.println("[Bob] public  key (pk) = " + pointToHex(bob.pk));
        System.out.println();

        // ------------------------------------------------- 2️⃣  Bob creates a nonce and sends R to Alice
        BobSigningSession bobSession = bobGenerateCommitment(bob);
        ECPoint R = bobSession.commitment;               // this is the only thing Alice sees
        System.out.println("[Bob] generated commitment R = " + pointToHex(R));
        System.out.println();

        // ------------------------------------------------- 3️⃣  Alice creates a blinded challenge
        String txt = "Hello, blind Schnorr!";
        byte[] msg = txt.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var pair = aliceCreateChallenge(R, bob.pk, msg);
        AliceBlindedChallenge aliceBlind = pair.getKey();
        AliceState aliceState = pair.getValue();

        System.out.println("[Alice] ordinary challenge e = " + aliceState.e.toString(16));
        System.out.println("[Alice] blinding factor b   = " + aliceState.blinding.toString(16));
        System.out.println("[Alice] blinded challenge e' = " + aliceBlind.ePrime.toString(16));
        System.out.println();

        // ------------------------------------------------- 4️⃣  Bob signs the blinded challenge
        BobBlindedSignature bobBlindSig = bobSignBlinded(bobSession, aliceBlind.ePrime, bob);
        System.out.println("[Bob] returns blinded signature s' = " + bobBlindSig.sPrime.toString(16));
        System.out.println();

        // ------------------------------------------------- 5️⃣  Alice unblinds and obtains a normal signature
        SchnorrSignature finalSig = aliceUnblind(bobBlindSig, aliceState, bob, R);
        System.out.println("[Alice] final (unblinded) Schnorr signature:");
        System.out.println("   R = " + pointToHex(finalSig.R));
        System.out.println("   s = " + finalSig.s.toString(16));
        System.out.println();

        // ------------------------------------------------- 6️⃣  Verification (anyone, including Bob)
        boolean ok = verifySignature(finalSig, bob.pk, msg);
        System.out.println("[Verifier] signature verification result: " + (ok ? "VALID" : "INVALID"));
    }
}
