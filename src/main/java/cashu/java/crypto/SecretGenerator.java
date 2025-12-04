package cashu.java.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates unique secrets for ecash tokens
 * Uses 128-bit secure random values as per Cashu spec
 */
public class SecretGenerator {
    private static final int SECRET_BYTES = 16; // 128-bit secrets
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new random secret
     *
     * @return Secret as BigInteger
     */
    public BigInteger generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return new BigInteger(1, bytes);
    }

    /**
     * Converts secret to base64 string for storage
     *
     * @param secret Secret as BigInteger
     * @return Base64-encoded string representation
     */
    public String toBase64(BigInteger secret) {
        byte[] bytes = secret.toByteArray();
        // Handle leading zero byte from BigInteger
        if (bytes.length > SECRET_BYTES && bytes[0] == 0) {
            byte[] trimmed = new byte[SECRET_BYTES];
            System.arraycopy(bytes, 1, trimmed, 0, SECRET_BYTES);
            return Base64.getEncoder().encodeToString(trimmed);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Converts base64 string back to BigInteger secret
     *
     * @param base64Secret Base64-encoded secret
     * @return Secret as BigInteger
     */
    public BigInteger fromBase64(String base64Secret) {
        byte[] bytes = Base64.getDecoder().decode(base64Secret);
        return new BigInteger(1, bytes);
    }
}
