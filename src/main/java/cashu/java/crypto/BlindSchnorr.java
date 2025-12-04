//package cashu.java.crypto;
//
//import java.io.IOException;
//import java.math.BigInteger;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.util.AbstractMap;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//import cashu.java.wallet.MintClient;
//import cashu.java.wallet.MintClientImpl;
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECParameterSpec;          // Bouncy‑Castle version
//import org.bouncycastle.math.ec.ECPoint;
//
//public class BlindSchnorr {
//
//    /*====================================================================*
//     *  1️⃣  Core blind‑Schnorr primitives (fixed)                         *
//     *====================================================================*/
//        static {
//            java.security.Security.addProvider(new BouncyCastleProvider());
//        }
//
//        private static final String CURVE_NAME = "secp256k1";
//
//        /**
//         * Order of the scalar field (prime n).
//         */
//        static final BigInteger CURVE_ORDER;          // n
//
//        /**
//         * Generator point G (affine).
//         */
//        static final ECPoint G;                       // generator
//
//        /**
//         * Secure RNG for all secret material.
//         */
//        static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//        static {
//            ECParameterSpec bcSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
//            CURVE_ORDER = bcSpec.getN();
//            G = bcSpec.getG();
//        }
//
//        /**
//         * Generate a random scalar in the range [1 , n‑1].
//         */
//        static BigInteger randomScalar() {
//            BigInteger r;
//            do {
//                r = new BigInteger(CURVE_ORDER.bitLength(), SECURE_RANDOM);
//            } while (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(CURVE_ORDER) >= 0);
//            return r;
//        }
//
//        /**
//         * Schnorr challenge e = H(R‖pk‖msg) (mod n).
//         */
//        static BigInteger challenge(ECPoint R, ECPoint pk, byte[] msg) {
//            try {
//                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
//                sha256.update(R.normalize().getEncoded(false));
//                sha256.update(pk.normalize().getEncoded(false));
//                sha256.update(msg);
//                return new BigInteger(1, sha256.digest()).mod(CURVE_ORDER);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        /* --------------------- data structures --------------------- */
//
//        /**
//         * Mint’s long‑term key pair.
//         */
//        static final class BobKeyPair {
//            final BigInteger sk;   // secret
//            final ECPoint pk;   // public = sk·G
//
//            private BobKeyPair(BigInteger sk, ECPoint pk) {
//                this.sk = sk;
//                this.pk = pk;
//            }
//
//            static BobKeyPair generate() {
//                BigInteger sk = randomScalar();
//                ECPoint pk = G.multiply(sk).normalize();
//                return new BobKeyPair(sk, pk);
//            }
//        }
//
//        /**
//         * What Alice sends to the mint after receiving the public key.
//         */
//        static final class AliceBlindedChallenge {
//            final BigInteger ePrime;   // e' = e + b (mod n)
//
//            private AliceBlindedChallenge(BigInteger ePrime) {
//                this.ePrime = ePrime;
//            }
//        }
//
//        /**
//         * What the mint returns: s' = e'·sk (mod n).
//         */
//        static final class BobBlindedSignature {
//            final BigInteger sPrime;   // s' (mod n)
//
//            private BobBlindedSignature(BigInteger sPrime) {
//                this.sPrime = sPrime;
//            }
//        }
//
//        /**
//         * The ordinary Schnorr signature (R, s) that any verifier can check.
//         */
//        static final class SchnorrSignature {
//            final ECPoint R;      // commitment point
//            final BigInteger s;   // signature scalar
//
//            SchnorrSignature(ECPoint R, BigInteger s) {
//                this.R = R;
//                this.s = s;
//            }
//        }
//
//        /**
//         * Alice’s per‑request state – must be kept until the mint replies.
//         */
//        static final class AliceState {
//            final BigInteger secret;   // r  (the token secret & nonce)
//            final BigInteger blinding; // b
//            final BigInteger e;        // ordinary challenge e = H(R‖pk‖msg)
//
//            private AliceState(BigInteger secret, BigInteger blinding, BigInteger e) {
//                this.secret = secret;
//                this.blinding = blinding;
//                this.e = e;
//            }
//        }
//
//        /* --------------------- public API ------------------------ */
//
//        /**
//         * 1️⃣  Alice creates a secret r, commitment R = r·G and the blinded challenge.
//         */
//        static AbstractMap.SimpleEntry<AliceBlindedChallenge, AliceState>
//        aliceCreateBlindedChallenge(ECPoint pk, byte[] tokenInfo) {
//
//            // secret / nonce that will become the Cashu “secret”
//            BigInteger r = randomScalar();
//            ECPoint R = G.multiply(r).normalize();          // R = r·G
//
//            // ordinary Schnorr challenge
//            BigInteger e = challenge(R, pk, tokenInfo);
//
//            // blinding factor
//            BigInteger b = randomScalar();
//
//            // blinded challenge e' = e + b (mod n)
//            BigInteger ePrime = e.add(b).mod(CURVE_ORDER);
//
//            AliceBlindedChallenge blind = new AliceBlindedChallenge(ePrime);
//            AliceState state = new AliceState(r, b, e);
//            // Return R via the state (it must be sent to the mint)
//            // For convenience we embed R into the state object.
//            // The mint never sees r or b.
//            return new AbstractMap.SimpleEntry<>(blind, state);
//        }
//
//        /**
//         * Helper – retrieve the commitment R from a stored secret r.
//         */
//        static ECPoint commitmentFromSecret(BigInteger secret) {
//            return G.multiply(secret).normalize();
//        }
//
//        /**
//         * 2️⃣  Mint signs the blinded challenge: s' = e'·sk (mod n).
//         */
//        static BobBlindedSignature mintSignBlinded(BigInteger ePrime, BobKeyPair bob) {
//            BigInteger sPrime = ePrime.multiply(bob.sk).mod(CURVE_ORDER);
//            return new BobBlindedSignature(sPrime);
//        }
//
//        /**
//         * 3️⃣  Alice removes the blinding factor and adds her nonce → ordinary signature.
//         */
//        static SchnorrSignature aliceUnblind(AliceState state,
//                                             BobBlindedSignature blinded,
//                                             BobKeyPair mintKp) {
//            // s = r + (s' – b·sk)  (mod n)
//            BigInteger s = state.secret
//                    .add(blinded.sPrime.subtract(state.blinding.multiply(mintKp.sk)).mod(CURVE_ORDER))
//                    .mod(CURVE_ORDER);
//            ECPoint R = commitmentFromSecret(state.secret);
//            return new SchnorrSignature(R, s);
//        }
//
//        /**
//         * 4️⃣  Normal Schnorr verification – any party can call this.
//         */
//        static boolean verifySignature(SchnorrSignature sig,
//                                       ECPoint pk,
//                                       byte[] tokenInfo) {
//            BigInteger e = challenge(sig.R, pk, tokenInfo);
//            ECPoint left = G.multiply(sig.s).normalize();                // s·G
//            ECPoint right = sig.R.add(pk.multiply(e)).normalize();        // R + e·pk
//            return left.equals(right);
//        }
//    }
//
//    /*====================================================================*
//     *  2️⃣  Cashu‑specific wrapper classes (Mint, Wallet, Token)          *
//     *====================================================================*/
//    final class CashuToken {
//        final String keysetId;        // which Mint key did we use?
//        final BigInteger secret;      // the token secret (also the Schnorr nonce)
//        final ECPoint commitment;   // R = secret·G
//        final BigInteger signature;   // s
//
//        CashuToken(String keysetId,
//                   BigInteger secret,
//                   ECPoint commitment,
//                   BigInteger signature) {
//            this.keysetId = keysetId;
//            this.secret = secret;
//            this.commitment = commitment;
//            this.signature = signature;
//        }
//
//        @Override
//        public String toString() {
//            return "CashuToken{keysetId='" + keysetId + '\'' +
//                    ", secret=" + secret.toString(16) +
//                    ", R=" + bytesToHex(commitment.normalize().getEncoded(false)) +
//                    ", s=" + signature.toString(16) + '}';
//        }
//
//        private static String bytesToHex(byte[] b) {
//            StringBuilder sb = new StringBuilder(b.length * 2);
//            for (byte v : b) sb.append(String.format("%02x", v));
//            return sb.toString();
//        }
//    }
//
//    /**
//     * The Mint – holds a set of long‑term key pairs (one per denomination).
//     */
//    final class Mint {
//        private final Map<String, BlindSchnorr.BobKeyPair> keysets = new HashMap<>();
//
//        Mint(String... ids) {
//            for (String id : ids) keysets.put(id, BlindSchnorr.BobKeyPair.generate());
//        }
//
//        /**
//         * Public part that a wallet can fetch (keyset‑id → public key).
//         */
//        Map<String, ECPoint> getPublicKeys() {
//            Map<String, ECPoint> out = new HashMap<>();
//            keysets.forEach((id, kp) -> out.put(id, kp.pk));
//            return out;
//        }
//
//        /**
//         * Retrieve the private key for a given key‑set – only used here for demo.
//         */
//        BlindSchnorr.BobKeyPair getKeyPair(String id) {
//            BlindSchnorr.BobKeyPair kp = keysets.get(id);
//            if (kp == null) throw new IllegalArgumentException("unknown keyset: " + id);
//            return kp;
//        }
//    }
//
//    /**
//     * The wallet (Alice).
//     */
//    final class Wallet {
//        private final Map<String, String> mintPublicKeys;
//        private final Mint mint;   // in a real system this would be a remote service
//        private final MintClientImpl mintClient;   // in a real system this would be a remote service
//
//        Wallet(Mint mint, MintClientImpl mintClient) throws IOException, InterruptedException {
//            this.mint = mint;
//            this.mintClient = mintClient;
//            this.mintPublicKeys = mintClient.getKeys("1");   // fetch once at start‑up
//        }
//
//        /**
//         * Request a Cashu token of a given denomination.
//         *
//         * @param keysetId  id of the denomination (e.g. “USD‑5”)
//         * @param tokenInfo data that the mint binds to the token (amount, nonce, …)
//         * @return an un‑blinded Cashu token
//         */
//        CashuToken requestToken(String keysetId, byte[] tokenInfo) {
//            ECPoint pk = Objects.requireNonNull(mintPublicKeys.get(keysetId),
//                    "Mint does not expose the requested keyset");
//
//            // ---------- Alice creates secret r, R and the blinded challenge ----------
//            var pair = BlindSchnorr.aliceCreateBlindedChallenge(pk, tokenInfo);
//            BlindSchnorr.AliceBlindedChallenge blinded = pair.getKey();
//            BlindSchnorr.AliceState aliceState = pair.getValue();
//
//            // ---------- Mint signs the blinded challenge ----------
//            BlindSchnorr.BobKeyPair mintKp = mint.getKeyPair(keysetId);
//            BlindSchnorr.BobBlindedSignature sPrime =
//                    BlindSchnorr.mintSignBlinded(blinded.ePrime, mintKp);
//
//            // ---------- Alice un‑blinds and adds her nonce ----------
//            BlindSchnorr.SchnorrSignature sig =
//                    BlindSchnorr.aliceUnblind(aliceState, sPrime, mintKp);
//
//            // ---------- Build the Cashu token ----------
//            return new CashuToken(keysetId,
//                    aliceState.secret,
//                    sig.R,
//                    sig.s);
//        }
//
//        /**
//         * Verify (and thus “redeem”) a token – the same check the mint would perform.
//         */
//        boolean redeem(CashuToken token, byte[] tokenInfo) {
//            ECPoint pk = Objects.requireNonNull(mintPublicKeys.get(token.keysetId),
//                    "Unknown keyset while redeeming");
//            BlindSchnorr.SchnorrSignature sig =
//                    new BlindSchnorr.SchnorrSignature(token.commitment, token.signature);
//            return BlindSchnorr.verifySignature(sig, pk, tokenInfo);
//        }
//    }
//
//    /*====================================================================*
//     *  3️⃣  Demo – end‑to‑end Mint ↔ Wallet run                           *
//     *====================================================================*/
