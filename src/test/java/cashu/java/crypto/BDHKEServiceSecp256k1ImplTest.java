package cashu.java.crypto;

import org.bouncycastle.jcajce.provider.asymmetric.dh.BCDHPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;

class BDHKEServiceSecp256k1ImplTest {

    @Test
    void blind() throws Exception {
        BDHKEServiceSecp256k1Impl s = new BDHKEServiceSecp256k1Impl();
        String pubkey = "025e8c3efd8207ca9c335ce72957138c267e8c5b70734b301c82a1b0c2fbd1f3e9";
        BlindingInfo blindingInfo = s.wallet_blind(pubkey);

        Assertions.assertNotNull(blindingInfo.secretXHex());
        Assertions.assertNotNull(blindingInfo.blindedPublicKeyHex());
        Assertions.assertNotNull(blindingInfo.blindingFactorHex());
    }

    @Test
    void unblind() throws Exception {
        BDHKEServiceSecp256k1Impl s = new BDHKEServiceSecp256k1Impl();
        String pubkey = "025e8c3efd8207ca9c335ce72957138c267e8c5b70734b301c82a1b0c2fbd1f3e9";
        String blindingFactor = "dcfe0b94e72c67cd1d24f52a5187d6a69222466481207ad555fc339f8c2dbaf8";
        String C_ = "02c818b913155292c6bc5020c234e5048ba2a1a9ea8210ad4549144eaab900926a";
        String output = s.wallet_unblind(pubkey, blindingFactor, C_);
        System.out.println(output);
        Assertions.assertNotNull(output);
    }


    @Test
    void mint_wallet_flow() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec, new SecureRandom());
        KeyPair kp = keyGen.genKeyPair();
        // 3) Get private scalar (hex)
        ECPrivateKey bcPriv = (ECPrivateKey) kp.getPrivate();
        String privHex = toHex(bcPriv.getD().toByteArray()); // scalar (may need strip leading 00)
        privHex = stripLeadingZeroByteHex(privHex);           // normalize output

        // 4) Get public key as raw SEC1 point
        ECPublicKey bcPub = (ECPublicKey) kp.getPublic();
        ECPoint Q = bcPub.getQ().normalize();
        byte[] pubSec1Compressed = Q.getEncoded(true);   // 33 bytes, starts 0x02/0x03
        byte[] pubSec1Uncompressed = Q.getEncoded(false); // 65 bytes, starts 0x04

        String pubCompressedHex = toHex(pubSec1Compressed);
        String pubUncompressedHex = toHex(pubSec1Uncompressed);

        System.out.println("Private (scalar) hex:    " + privHex);
        System.out.println("Public SEC1 (compressed): " + pubCompressedHex);
        System.out.println("Public SEC1 (uncompressed): " + pubUncompressedHex);

        BDHKEServiceSecp256k1Impl s = new BDHKEServiceSecp256k1Impl();

        BlindingInfo blindingInfo = s.wallet_blind(pubCompressedHex);
        String C_ = s.mint_sign(blindingInfo.blindedPublicKeyHex(), privHex);

        String blindingFactor = blindingInfo.blindingFactorHex();
//        String C_ = "02c818b913155292c6bc5020c234e5048ba2a1a9ea8210ad4549144eaab900926a";
        String C = s.wallet_unblind(pubCompressedHex, blindingFactor, C_);
        System.out.println(C);
        Assertions.assertNotNull(C);

        Assertions.assertTrue(s.mint_verify(privHex,C,blindingInfo.secretXHex()));
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    private static String stripLeadingZeroByteHex(String hex) {
        // BigInteger.toByteArray() is two's complement; it may add a leading 00 for positive values.
        // Remove that if present to keep a consistent scalar representation.
        if (hex.startsWith("00")) {
            return hex.replaceFirst("^00+", "");
        }
        return hex;
    }
}