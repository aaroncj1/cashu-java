//package cashu.java.crypto;
//
//import java.math.BigInteger;
//import java.security.*;
//import java.security.spec.ECGenParameterSpec;
//
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.math.ec.ECPoint;
//
//public class aiBDHKE {
//
//    /**
//     * Demonstration of a **blinded Diffie‑Hellman key exchange** on the
//     * secp256k1 curve, written in pure Java using Bouncy‑Castle.
//     * <p>
//     * The steps are:
//     * 1. Each party creates a normal EC key‑pair (private scalar a / public point A = G·a).
//     * 2. Each party picks a random blinding scalar r and computes a *blinded* public key R·A.
//     * 3. The two blinded public keys are exchanged.
//     * 4. Each party does a normal EC‑DH using its own private scalar and the *other* blinded public key.
//     * This yields a *blinded* shared point S_blinded = G^{ab·r_other}.
//     * 5. Both parties multiply (or exponentiate) that point by the *inverse* of the other party's blinding factor.
//     * The result is G^{ab}, exactly the same value you would have obtained from an
//     * ordinary (un‑blinded) EC‑DH.
//     * <p>
//     * The example prints all intermediate values for clarity.
//     *
//     /* ---------- static utilities ---------- */
//
//    /** Initialise Bouncy‑Castle once. */
//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }
//
//    /**
//     * Curve name for secp256k1 in the JCA naming scheme.
//     */
//    private static final String CURVE_NAME = "secp256k1";
//
//    /**
//     * Order of the secp256k1 curve (the size of the scalar field).
//     */
//    private static final BigInteger CURVE_ORDER;
//
//    /**
//     * Generator point G of secp256k1 (used for verification / printing).
//     */
//    private static final ECPoint G;
//
//    static {
//        try {
//            // <-- NEW: load the curve spec from Bouncy‑Castle's named‑curve table
//            org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
//                    org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(CURVE_NAME);
//
//            CURVE_ORDER = bcSpec.getN();      // the group order (the scalar field)
//            G = bcSpec.getG();                // the generator point as a Bouncy‑Castle ECPoint
//        } catch (Exception e) {
//            throw new RuntimeException("Unable to load secp256k1 parameters", e);
//        }
//    }
//
//    /**
//     * Secure random source – always use a strong RNG for crypto.
//     */
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//    /**
//     * Generate a random scalar in the range [1, n‑1] where n = curve order.
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
//     * Compute modular inverse of a scalar modulo the curve order.
//     */
//    private static BigInteger invertScalar(BigInteger k) {
//        return k.modInverse(CURVE_ORDER);
//    }
//
//    /**
//     * Convert a raw EC point (projective) to its uncompressed X9.62 encoding.
//     * Useful for printing / transmitting.
//     */
//    private static byte[] encodePoint(ECPoint p) {
//        return p.normalize().getEncoded(false); // false → uncompressed
//    }
//
//    /**
//     * Simple helper to print a point in hex (uncompressed form).
//     */
//    private static String pointToHex(ECPoint p) {
//        return bytesToHex(encodePoint(p));
//    }
//
//    /**
//     * Byte‑array → hex string.
//     */
//    private static String bytesToHex(byte[] bytes) {
//        StringBuilder sb = new StringBuilder(bytes.length * 2);
//        for (byte b : bytes)
//            sb.append(String.format("%02x", b));
//        return sb.toString();
//    }
//
//    /* ---------- core blinded DH operations ---------- */
//
//    /**
//     * Represents everything a party needs to keep locally.
//     */
//    private static final class Party {
//        final String name;                 // just for pretty printing
//        final BigInteger priv;             // a  (private scalar)
//        final ECPoint pub;                  // A = G·a
//        final BigInteger blind;            // r  (blinding scalar)
//        final ECPoint blindedPub;           // R·A = G·(a·r)
//
//        Party(String name) {
//            this.name = name;
//            this.priv = randomScalar();                 // a
//            this.pub = G.multiply(priv).normalize();    // A = G·a
//            this.blind = randomScalar();                // r
//            this.blindedPub = pub.multiply(blind).normalize(); // R·A
//        }
//    }
//
//    /**
//     * Compute the **blinded** shared secret from one's own private scalar
//     * and the *other* party's *blinded* public key.
//     *
//     * @param ownPriv      your private scalar (a)
//     * @param otherBlindPK the other side's blinded public point (= G·b·r_other)
//     * @return EC point S_blinded = G^{a·b·r_other}
//     */
//    private static ECPoint computeBlindedShared(BigInteger ownPriv, ECPoint otherBlindPK) {
//        // DH: (otherBlindPK) ^ ownPriv = (G·b·r) ^ a = G^{a·b·r}
//        return otherBlindPK.multiply(ownPriv).normalize();
//    }
//
//    /**
//     * Un‑blind a previously computed blinded shared point.
//     *
//     * @param blindedShared    the point G^{a·b·r_other}
//     * @param otherBlindScalar the *other* party's blinding scalar r_other
//     * @return EC point G^{a·b}
//     */
//    private static ECPoint unblindShared(ECPoint blindedShared, BigInteger otherBlindScalar) {
//        // Multiply by (r_other)⁻¹  (i.e. raise to the inverse scalar)
//        BigInteger inv = invertScalar(otherBlindScalar);
//        return blindedShared.multiply(inv).normalize();
//    }
//
//    /* ---------- demo driver ---------- */
//
//    public static void main(String[] args) {
//        System.out.println("=== secp256k1 Blinded Diffie‑Hellman Demo ===\n");
//
//        // -------------------- 1️⃣  generate two parties --------------------
//        Party alice = new Party("Alice");
//        Party bob = new Party("Bob");
//
//        // Print raw (un‑blinded) keys for comparison
//        System.out.println("[Alice] private a = " + alice.priv.toString(16));
//        System.out.println("[Alice] public  A = " + pointToHex(alice.pub));
//        System.out.println("[Bob]   private b = " + bob.priv.toString(16));
//        System.out.println("[Bob]   public  B = " + pointToHex(bob.pub));
//        System.out.println();
//
//        // -------------------- 2️⃣  each side blinds its public key --------------------
//        System.out.println("[Alice] blinding factor r_A = " + alice.blind.toString(16));
//        System.out.println("[Alice] blinded public R_A = " + pointToHex(alice.blindedPub));
//        System.out.println("[Bob]   blinding factor r_B = " + bob.blind.toString(16));
//        System.out.println("[Bob]   blinded public R_B = " + pointToHex(bob.blindedPub));
//        System.out.println();
//
//        // -------------------- 3️⃣  exchange blinded public keys --------------------
//        // (In a real protocol you would also send the *blinding factor* to the peer
//        //  in a protected way – here we just keep it locally for demonstration.)
//        ECPoint aliceBlindedPK = alice.blindedPub; // sent to Bob
//        ECPoint bobBlindedPK = bob.blindedPub;   // sent to Alice
//
//        // -------------------- 4️⃣  each side computes the *blinded* shared point --------------------
//        ECPoint aliceBlindedShared = computeBlindedShared(alice.priv, bobBlindedPK);
//        ECPoint bobBlindedShared = computeBlindedShared(bob.priv, aliceBlindedPK);
//
//        System.out.println("[Alice] blinded shared S_A = " + pointToHex(aliceBlindedShared));
//        System.out.println("[Bob]   blinded shared S_B = " + pointToHex(bobBlindedShared));
//        System.out.println();
//
//        // -------------------- 5️⃣  each side unblinds the result --------------------
//        ECPoint aliceUnblinded = unblindShared(aliceBlindedShared, bob.blind);
//        ECPoint bobUnblinded = unblindShared(bobBlindedShared, alice.blind);
//
//        System.out.println("[Alice] unblinded shared secret = " + pointToHex(aliceUnblinded));
//        System.out.println("[Bob]   unblinded shared secret = " + pointToHex(bobUnblinded));
//        System.out.println();
//
//        // -------------------- 6️⃣  verify that both results are identical --------------------
//        boolean equal = aliceUnblinded.equals(bobUnblinded);
//        System.out.println("=> Do the two parties obtain the same secret? " + (equal ? "YES" : "NO"));
//
//        // -------------------- 7️⃣  sanity‑check against ordinary EC‑DH --------------------
//        ECPoint ordinaryShared = bob.pub.multiply(alice.priv).normalize(); // G^{ab}
//        System.out.println("[Sanity] ordinary (unblinded) EC‑DH secret = " + pointToHex(ordinaryShared));
//        System.out.println("   matches blinded‑unblinded secret? " + (ordinaryShared.equals(aliceUnblinded) ? "YES" : "NO"));
//    }
//}
