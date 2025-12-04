package cashu.java.crypto;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Implements DLEQ proofs for non-interactive verification of blind signatures
 * Proves mint used same private key for all signatures without revealing key
 */
public class DleqProver {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a DLEQ proof
     *
     * @param privateKey       Mint's private key (d)
     * @param blindedMessage   Blinded secret (B)
     * @param blindedSignature Signed blinded secret (C')
     * @param generator        Base generator (g)
     * @param publicKeyY       Mint's public key y = g^d mod N
     * @param n                Modulus
     * @return DLEQ proof as a pair (c, s)
     */
    public DleqProof generateProof(BigInteger privateKey,
                                   BigInteger blindedMessage,
                                   BigInteger blindedSignature,
                                   BigInteger generator,
                                   BigInteger publicKeyY,
                                   BigInteger n) {
        try {
            // 1. Generate random nonce k
            BigInteger k = new BigInteger(n.bitLength(), SECURE_RANDOM).mod(n);

            // 2. Compute t1 = B^k mod N
            BigInteger t1 = blindedMessage.modPow(k, n);

            // 3. Compute t2 = g^k mod N
            BigInteger t2 = generator.modPow(k, n);

            // 4. Compute challenge c = H(B, C', g, y, t1, t2)
            BigInteger c = computeChallenge(blindedMessage, blindedSignature,
                    generator, publicKeyY, t1, t2, n);

            // 5. Compute s = k - c*d mod φ(n) (simplified)
            BigInteger s = k.subtract(c.multiply(privateKey));

            return new DleqProof(c, s);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Hash algorithm unavailable", e);
        }
    }

    /**
     * Verifies a DLEQ proof
     *
     * @param blindedMessage   Blinded secret (B)
     * @param blindedSignature Signed blinded secret (C')
     * @param generator        Base generator (g)
     * @param publicKeyY       Mint's public key y = g^d mod N
     * @param n                Modulus
     * @param proof            DLEQ proof (c, s)
     * @return true if proof is valid, false otherwise
     */
    public boolean verifyProof(BigInteger blindedMessage,
                               BigInteger blindedSignature,
                               BigInteger generator,
                               BigInteger publicKeyY,
                               BigInteger n,
                               DleqProof proof) {
        try {
            // 1. Compute t1' = B^s * (C')^c mod N
            BigInteger t1Prime = blindedMessage.modPow(proof.s(), n)
                    .multiply(blindedSignature.modPow(proof.c(), n))
                    .mod(n);

            // 2. Compute t2' = g^s * y^c mod N
            BigInteger t2Prime = generator.modPow(proof.s(), n)
                    .multiply(publicKeyY.modPow(proof.c(), n))
                    .mod(n);

            // 3. Compute challenge c' = H(B, C', g, y, t1', t2')
            BigInteger cPrime = computeChallenge(blindedMessage, blindedSignature,
                    generator, publicKeyY, t1Prime, t2Prime, n);

            // 4. Verify c == c'
            return proof.c().equals(cPrime);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Hash algorithm unavailable", e);
        }
    }

    private BigInteger computeChallenge(BigInteger B, BigInteger C_,
                                        BigInteger g, BigInteger y,
                                        BigInteger t1, BigInteger t2,
                                        BigInteger n) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

        // Use consistent byte representation
        updateDigest(digest, B);
        updateDigest(digest, C_);
        updateDigest(digest, g);
        updateDigest(digest, y);
        updateDigest(digest, t1);
        updateDigest(digest, t2);

        byte[] hash = digest.digest();
        return new BigInteger(1, hash).mod(n);
    }

    private void updateDigest(MessageDigest digest, BigInteger value) {
        byte[] bytes = value.toByteArray();
        digest.update((byte) (bytes.length >> 8));
        digest.update((byte) bytes.length);
        digest.update(bytes);
    }

    public record DleqProof(BigInteger c, BigInteger s) {
    }

    public static class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
