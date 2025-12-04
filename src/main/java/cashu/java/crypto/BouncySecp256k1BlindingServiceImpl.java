//package cashu.java.crypto;
//
//import org.bitcoinj.secp.bouncy.Bouncy256k1;
//import org.bitcoinj.secp.bouncy.BouncyKeyPair;
//import org.bitcoinj.secp.bouncy.BouncyPrivKey;
//import org.bitcoinj.secp.bouncy.BouncyPubKey;
//import org.bouncycastle.jcajce.provider.asymmetric.dh.BCDHPrivateKey;
//
//import java.math.BigInteger;
//import java.security.PrivateKey;
//
//public class BouncySecp256k1BlindingServiceImpl implements BlindingService {
//
//    //def step1_alice(
//    //    secret_msg: str, blinding_factor: Optional[PrivateKey] = None
//    //) -> tuple[PublicKey, PrivateKey]:
//    //    Y: PublicKey = hash_to_curve(secret_msg.encode("utf-8"))
//    //    r = blinding_factor or PrivateKey()
//    //    B_: PublicKey = Y + r.pubkey  # type: ignore
//    //    return B_, r
//    @Override
//    public BigInteger blind(BigInteger secret, BigInteger r, BigInteger e, BigInteger n) {
//        Bouncy256k1 bouncy256k1 = new Bouncy256k1();
//        BouncyPrivKey privKey = bouncy256k1.ecPrivKeyCreate();
//        BouncyPubKey pubKey = bouncy256k1.ecPubKeyCreate(privKey);
//
//        return null;
//    }
//
//    //def step3_alice(C_: PublicKey, r: PrivateKey, A: PublicKey) -> PublicKey:
//    //    C: PublicKey = C_ - A.mult(r)  # type: ignore
//    //    return C
//    @Override
//    public BigInteger unblind(BigInteger signedBlindedSecret, BigInteger r, BigInteger n) {
//        return null;
//    }
//
////    public BouncyPubKey unblind(BouncyPubKey C_, BouncyPrivKey r, BouncyPubKey A) {
//////        BouncyPubKey C = (byte) (A.getEncoded() * r.getEncoded());
//////        return C;
////    }
//
//    //def verify(a: PrivateKey, C: PublicKey, secret_msg: str) -> bool:
//    //    Y: PublicKey = hash_to_curve(secret_msg.encode("utf-8"))
//    //    valid = C == Y.mult(a)  # type: ignore
//    //    # BEGIN: BACKWARDS COMPATIBILITY < 0.15.1
//    //    if not valid:
//    //        valid = verify_deprecated(a, C, secret_msg)
//    //    # END: BACKWARDS COMPATIBILITY < 0.15.1
//    //    return valid
//    public boolean verify(BouncyPrivKey privKey, BouncyPubKey pubKey, String secretMessage) {
//        return false;
//    }
//}
