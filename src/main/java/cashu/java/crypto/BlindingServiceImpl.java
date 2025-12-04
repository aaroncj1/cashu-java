//package cashu.java.crypto;
//
//import cashu.java.common.Token;
//import org.bitcoinj.secp.bouncy.Bouncy256k1;
//import org.bitcoinj.secp.bouncy.BouncyPrivKey;
//import org.bitcoinj.secp.bouncy.BouncyPubKey;
//import org.bouncycastle.asn1.sec.SECNamedCurves;
//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.crypto.params.ECDomainParameters;
//import org.bouncycastle.jce.ECNamedCurveTable;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
//import org.bouncycastle.math.ec.ECCurve;
//import org.bouncycastle.math.ec.ECPoint;
//import org.bouncycastle.util.encoders.Hex;
//
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.security.Security;
//import java.util.Objects;
//
//public class BlindingServiceImpl implements BlindingService {
//    private static final X9ECParameters CURVE = SECNamedCurves.getByName("secp256k1");
//    private static final ECCurve curve = CURVE.getCurve();
//
//    private static final BigInteger ORDER = CURVE.getN();
//
//    public static final ECDomainParameters DOMAIN = new ECDomainParameters(
//            CURVE.getCurve(), CURVE.getG(), CURVE.getN(), CURVE.getH());
//    private static final SecureRandom RANDOM = new SecureRandom();
//
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//    // Parse hex public key to ECPoint
//    public static ECPoint parsePublicKey(String hexPubKey) {
//        byte[] pubKeyBytes = hexStringToBytes(hexPubKey);
//        return CURVE.getCurve().decodePoint(pubKeyBytes);
//    }
//
//
//    static {
//        // Register Bouncy Castle provider
//        if (Security.getProvider("BC") == null) {
//            Security.addProvider(new BouncyCastleProvider());
//        }
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
//    //def verify(a: PrivateKey, C: PublicKey, secret_msg: str) -> bool:
//    //    Y: PublicKey = hash_to_curve(secret_msg.encode("utf-8"))
//    //    valid = C == Y.mult(a)  # type: ignore
//    //    # BEGIN: BACKWARDS COMPATIBILITY < 0.15.1
//    //    if not valid:
//    //        valid = verify_deprecated(a, C, secret_msg)
//    //    # END: BACKWARDS COMPATIBILITY < 0.15.1
//    //    return valid
//    public boolean verify(BigInteger a, BigInteger C, String secretMessage) throws Exception {
//        ECPoint Y = hashToCurve(secretMessage.getBytes(StandardCharsets.UTF_8));
//        return Objects.equals(C, Y.multiply(a));
//    }
//
//    // Generate blinding factor
//    public static BigInteger generateBlindingFactor() {
//        return new BigInteger(256, RANDOM);
//    }
//
//    // Helper: Hex string to byte array
//    private static byte[] hexStringToBytes(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i + 1), 16));
//        }
//        return data;
//    }
//
//    @Override
//    public BigInteger blind(BigInteger secret, BigInteger r, BigInteger e, BigInteger n) {
//        if (secret.compareTo(BigInteger.ONE) < 0) {
//            throw new IllegalArgumentException("Secret must be positive");
//        }
//        if (secret.compareTo(n) >= 0) {
//            throw new IllegalArgumentException("Secret must be less than modulus n");
//        }
//        if (secret.equals(BigInteger.ZERO)) {
//            throw new IllegalArgumentException("Zero is not a valid secret");
//        }
//        BigInteger rPowE = r.modPow(e, n);
//        return secret.multiply(rPowE).mod(n);
//    }
//
//    public static java.security.spec.ECPoint blind(ECPoint secret, BouncyPrivKey r, ECPoint mintPublicKey) {
//        // B_ = secret + r*G
//        Bouncy256k1 bouncy256k1 = new Bouncy256k1();
//        BouncyPrivKey privKey = bouncy256k1.ecPrivKeyCreate();
//        BouncyPubKey pubKey = bouncy256k1.ecPubKeyCreate(privKey);
////        return secret.add((bouncy256k1.ecPubKeyCreate(r).getEncoded()));
//        return null;
//    }
//
//    // Unblind a signature
//    public static ECPoint unblind(ECPoint blindSignature, BigInteger r, ECPoint mintPublicKey) {
//        // C = C_ - r*K
//        // Where K is mint's public key
//        return blindSignature.subtract(mintPublicKey.multiply(r));
//    }
//
//    @Override
//    public BigInteger unblind(BigInteger signedBlindedSecret, BigInteger r, BigInteger n) {
//        BigInteger rInv = r.modInverse(n);
//        return signedBlindedSecret.multiply(rInv).mod(n);
//    }
//
//    private static boolean isValidHex(String hex, int expectedLength) {
//        return hex != null &&
//                hex.length() == expectedLength &&
//                hex.matches("^[0-9a-fA-F]+$");
//    }
//    public static boolean validateProof(String secret, String C, String mintPublicKeyHex) {
//        if (!isValidHex(secret, 66)) {
//            System.out.println(("Invalid secret format: " + secret));
//        }
//        if (!isValidHex(C, 66)) {
//            System.out.println("Invalid signature format: " + C);
//        }
//        if (!isValidHex(mintPublicKeyHex, 66)) {
//            System.out.println("Invalid mint key format: " + mintPublicKeyHex);
//        }
//            try {
//            // 1. Parse points from hex
//            ECPoint secretPoint = parsePoint(secret);
//            ECPoint signaturePoint = parsePoint(C);
//            ECPoint mintPublicKey = parsePoint(mintPublicKeyHex);
//
//            // 2. Verify points are on curve
//            if (!isOnCurve(secretPoint) ||
//                    !isOnCurve(signaturePoint) ||
//                    !isOnCurve(mintPublicKey)) {
//                System.out.println("ifed out!");
//                return false;
//            }
//
//                if (!isInPrimeSubgroup(secretPoint)) {
//                    System.out.println("Secret point not in prime subgroup");
//                }
//                if (!isInPrimeSubgroup(signaturePoint)) {
//                    System.out.println("Signature point not in prime subgroup");
//                }
//
//                    ECPoint kS = secretPoint.multiply(deriveScalar(mintPublicKey));
//            System.out.println("ks: " + kS + " sig: "  + signaturePoint);
////            constantTimeEquals(kS, signaturePoint);
//                BigInteger s = extractSecretScalar(secretPoint);
//                ECPoint C_expected = mintPublicKey.multiply(s).normalize();
//                System.out.println("s: " + s);
//                System.out.println("c expected: " + C_expected);
//                System.out.println("hmmm:  " + constantTimeEquals(C_expected, signaturePoint));
//                System.out.println(Hex.toHexString(C_expected.getEncoded(true)) +
//                        " vs actual " + C);
//
//            // 3. Check signature: C == k * S
//            // Where k = mint's private key (implied by K = k*G)
//            ECPoint expectedSignature = secretPoint.multiply(getPrivateKeyFromSignature(signaturePoint, secretPoint));
//            System.out.println("e: " + expectedSignature);
//            System.out.println("a: " + signaturePoint);
//            System.out.println("mint pub:: " + mintPublicKeyHex);
//            // 4. Verify expected signature matches actual signature
//            return expectedSignature.equals(signaturePoint);
//
//        } catch (Exception e) {
//            System.out.println("exception!");
//            return false;
//        }
//    }
//
//
//    private static boolean isInPrimeSubgroup(ECPoint p) {
//        ECPoint check = p.multiply(ORDER);
//        return check.isInfinity();
//    }
//
//    private static BigInteger extractSecretScalar(ECPoint S) {
//        // For compressed points: s = x-coordinate mod n
//        BigInteger x = S.normalize().getXCoord().toBigInteger();
//        return x.mod(ORDER);
//    }
//
//
//    private static BigInteger deriveScalar(ECPoint K) {
//        // In practice, you'd use the mint's public key consistently
//        // This simplified version assumes K is valid
//        return K.normalize().getXCoord().toBigInteger();
//    }
//
//    private static boolean constantTimeEquals(ECPoint a, ECPoint b) {
//        byte[] aEnc = a.getEncoded(true);
//        byte[] bEnc = b.getEncoded(true);
//        return MessageDigest.isEqual(aEnc, bEnc);
//    }
//
//
//    // Helper to parse hex to ECPoint
//    private static ECPoint parsePoint(String hex) {
//        byte[] bytes = Hex.decode(hex);
//        return curve.decodePoint(bytes);
//    }
//
//    // Check if point is on curve
//    private static boolean isOnCurve(ECPoint point) {
//        return point != null && point.isValid();
//    }
//
//    /**
//     * Derives the implied private key multiplier
//     * (Only works for this specific EC relationship)
//     */
//    private static BigInteger getPrivateKeyFromSignature(ECPoint C, ECPoint S) {
//        // This uses the special property: C = k * S
//        // Where k is effectively the private key for this specific token
//        return S.normalize().getXCoord().toBigInteger()
//                .multiply(C.normalize().getXCoord().toBigInteger().modInverse(curve.getOrder()))
//                .mod(curve.getOrder());
//    }
//
//
//    public BigInteger generateBlindingFactor(BigInteger n) {
//        BigInteger r;
//        do {
//            r = new BigInteger(n.bitLength(), SECURE_RANDOM);
//        } while (r.compareTo(n) >= 0 ||
//                r.compareTo(BigInteger.ONE) <= 0 ||
//                !r.gcd(n).equals(BigInteger.ONE));
//
//        return r;
//    }
//
//}
