package cashu.java.crypto;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public interface BlindingService {
    BigInteger blind(BigInteger secret, BigInteger r, BigInteger e, BigInteger n);
    BigInteger unblind(BigInteger signedBlindedSecret, BigInteger r, BigInteger n);
}
