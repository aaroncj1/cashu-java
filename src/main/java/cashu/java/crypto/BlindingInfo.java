package cashu.java.crypto;

public record BlindingInfo(String secretXHex,
                           String blindingFactorHex,
                           String blindedPublicKeyHex) {
    @Override
    public String toString() {
        return "BlindingInfo{secretXHex='" + secretXHex + '\'' +
                ", blindingFactorHex='" + blindingFactorHex + '\'' +
                ", blindedPublicKeyHex='" + blindedPublicKeyHex + '\'' + '}';
    }
}
