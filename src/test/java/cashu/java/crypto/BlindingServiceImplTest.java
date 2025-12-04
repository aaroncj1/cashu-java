//package cashu.java.crypto;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//
//import java.math.BigInteger;
//import java.security.SecureRandom;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BlindingServiceImplTest {
//
//    private BlindingServiceImpl blindingService;
//    private BigInteger n;  // RSA modulus
//    private BigInteger e;  // Public exponent
//    private BigInteger d;  // Private exponent
//    private final int BIT_LENGTH = 1024;  // Use 1024 for faster tests
//
//    @BeforeEach
//    void setUp() {
//        blindingService = new BlindingServiceImpl();
//
//        // Generate test RSA keys
//        BigInteger p = BigInteger.probablePrime(BIT_LENGTH / 2, new SecureRandom());
//        BigInteger q = BigInteger.probablePrime(BIT_LENGTH / 2, new SecureRandom());
//        n = p.multiply(q);
//        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
//
//        e = BigInteger.valueOf(65537);  // Common public exponent
//        d = e.modInverse(phi);  // Private exponent
//    }
//
//    @Test
//    void testFullBlindingCycle() {
//        // Generate test secret
//        BigInteger secret = new BigInteger(BIT_LENGTH - 1, new SecureRandom());
//        System.out.println(secret);
//        // Generate blinding factor
//        BigInteger r = blindingService.generateBlindingFactor(n);
//        System.out.println(r);
//        // Blind the secret
//        BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//        System.out.println(blindedSecret);
//        // "Sign" the blinded secret (simulate mint operation)
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//        System.out.println(signedBlindedSecret);
//        // Unblind the signature
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//        System.out.println(unblindedSignature);
//        // Verify the signature
////        boolean isValid = blindingService.verify(secret, unblindedSignature, e, n);
//
////        assertTrue(isValid, "Full blinding cycle should produce valid signature");
//    }
//
//    @ParameterizedTest
//    @ValueSource(ints = {1, 10, 100})
//        // Test with multiple secrets
//    void testMultipleSecrets(int count) {
//        for (int i = 0; i < count; i++) {
//            BigInteger secret = new BigInteger(BIT_LENGTH - 1, new SecureRandom());
//            BigInteger r = blindingService.generateBlindingFactor(n);
//            BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//
//            // Mint signs the blinded secret
//            BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//
//            BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//            boolean isValid = blindingService.verify(secret, unblindedSignature, e, n);
//
//            assertTrue(isValid, "Signature should be valid for secret #" + i);
//        }
//    }
//
//    @Test
//    void testBlindingFactorGeneration() {
//        BigInteger r = blindingService.generateBlindingFactor(n);
//
//        // Validate properties
//        assertTrue(r.compareTo(BigInteger.ONE) > 0, "r should be > 1");
//        assertTrue(r.compareTo(n) < 0, "r should be < n");
//        assertEquals(BigInteger.ONE, r.gcd(n), "r should be coprime to n");
//    }
//
//    @Test
//    void testInvalidUnblinding() {
//        BigInteger secret = BigInteger.valueOf(12345);
//        BigInteger r = blindingService.generateBlindingFactor(n);
//        BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//
//        // Mint signs the blinded secret
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//
//        // Use wrong blinding factor for unblinding
//        BigInteger wrongR = blindingService.generateBlindingFactor(n);
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, wrongR, n);
//
//        // Verification should fail
////        boolean isValid = blindingService.verify(secret, unblindedSignature, e, n);
//
////        assertFalse(isValid, "Unblinding with wrong r should invalidate signature");
//    }
//
//    @Test
//    void testTamperedSignature() {
//        BigInteger secret = BigInteger.valueOf(67890);
//        BigInteger r = blindingService.generateBlindingFactor(n);
//        BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//
//        // Mint signs the blinded secret
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//
//        // Unblind correctly
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//
//        // Tamper with the signature
//        BigInteger tamperedSignature = unblindedSignature.add(BigInteger.ONE);
//
//        // Verification should fail
//        boolean isValid = blindingService.verify(secret, tamperedSignature, e, n);
//
//        assertFalse(isValid, "Tampered signature should not verify");
//    }
//
//    @Test
//    void testWrongSecretVerification() {
//        BigInteger secret1 = BigInteger.valueOf(111);
//        BigInteger secret2 = BigInteger.valueOf(222);
//        BigInteger r = blindingService.generateBlindingFactor(n);
//        BigInteger blindedSecret = blindingService.blind(secret1, r, e, n);
//
//        // Mint signs the blinded secret
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//
//        // Unblind
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//
//        // Verify with different secret
//        boolean isValid = blindingService.verify(secret2, unblindedSignature, e, n);
//
//        assertFalse(isValid, "Verification with wrong secret should fail");
//    }
//
//    @Test
//    void testBlindingFactorCoprime() {
//        // Create a non-coprime blinding factor (should be rejected by generation)
//        BigInteger nonCoprimeR = n.divide(BigInteger.valueOf(2));
//
//        // Ensure our generation function wouldn't produce this
//        BigInteger validR = blindingService.generateBlindingFactor(n);
//        assertNotEquals(BigInteger.ZERO, validR.gcd(n).compareTo(BigInteger.ONE),
//                "Generated r should be coprime to n");
//
//        // Force test with invalid r
//        BigInteger secret = BigInteger.valueOf(42);
//        BigInteger blindedSecret = blindingService.blind(secret, nonCoprimeR, e, n);
//
//        // Mint signs the blinded secret
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//
//        // Unblinding should fail with ArithmeticException
//        assertThrows(ArithmeticException.class, () -> {
//            blindingService.unblind(signedBlindedSecret, nonCoprimeR, n);
//        });
//    }
//
//    @Test
//    void testEdgeCaseSecrets() {
//        // Test with minimum value (1)
//        testSecretVerification(BigInteger.ONE);
//
//        // Test with maximum value (n-1)
//        testSecretVerification(n.subtract(BigInteger.ONE));
//
//        // Test with zero (should be invalid but test behavior)
//        assertThrows(IllegalArgumentException.class, () -> {
//            BigInteger r = blindingService.generateBlindingFactor(n);
//            blindingService.blind(BigInteger.ZERO, r, e, n);
//        });
//
//        // Test with negative value (should be invalid)
//        assertThrows(IllegalArgumentException.class, () -> {
//            BigInteger r = blindingService.generateBlindingFactor(n);
//            blindingService.blind(BigInteger.valueOf(-1), r, e, n);
//        });
//
//        // Test with value equal to n - should be rejected
//        assertThrows(IllegalArgumentException.class, () -> {
//            BigInteger r = blindingService.generateBlindingFactor(n);
//            blindingService.blind(n, r, e, n);
//        });
//
//    }
//
//    private void testSecretVerification(BigInteger secret) {
//        BigInteger r = blindingService.generateBlindingFactor(n);
//        BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//        boolean isValid = blindingService.verify(secret, unblindedSignature, e, n);
//
//        // Zero is mathematically invalid for RSA, so expect failure
//        if (secret.equals(BigInteger.ZERO)) {
//            assertFalse(isValid, "Zero secret should not verify");
//        } else {
//            assertTrue(isValid, "Edge case secret should verify: " + secret);
//        }
//    }
//
//    @Test
//    void testBlindingFactorRegeneration() {
//        BigInteger secret = BigInteger.valueOf(123);
//        BigInteger r1 = blindingService.generateBlindingFactor(n);
//        BigInteger r2 = blindingService.generateBlindingFactor(n);
//
//        // Should generate different factors each time
//        assertNotEquals(r1, r2, "Subsequent blinding factors should be different");
//
//        // Both should work independently
//        BigInteger blinded1 = blindingService.blind(secret, r1, e, n);
//        BigInteger blinded2 = blindingService.blind(secret, r2, e, n);
//
//        // Should produce different blinded results
//        assertNotEquals(blinded1, blinded2, "Different r should produce different blinded secrets");
//
//        // Both should verify correctly after full cycle
//        testBlindingWithSpecificR(secret, r1);
//        testBlindingWithSpecificR(secret, r2);
//    }
//
//    private void testBlindingWithSpecificR(BigInteger secret, BigInteger r) {
//        BigInteger blindedSecret = blindingService.blind(secret, r, e, n);
//        BigInteger signedBlindedSecret = blindedSecret.modPow(d, n);
//        BigInteger unblindedSignature = blindingService.unblind(signedBlindedSecret, r, n);
//        boolean isValid = blindingService.verify(secret, unblindedSignature, e, n);
//        assertTrue(isValid, "Should work with specific r");
//    }
//}