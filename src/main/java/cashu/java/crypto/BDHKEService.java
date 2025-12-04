package cashu.java.crypto;

public interface BDHKEService {
    BlindingInfo wallet_blind(String mintPublicKeyHex) throws Exception;

    String wallet_unblind(String mintPublicKeyHex, String blindingFactorHex, String cPrimeHex);

    String mint_sign(String B_, String privateKey);

    boolean mint_verify(String privateKey, String C, String secretMessage) throws Exception;
}
