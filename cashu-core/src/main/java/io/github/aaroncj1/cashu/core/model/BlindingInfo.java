package io.github.aaroncj1.cashu.core.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BlindingInfo {

    private String secretXHex;
    private String blindingFactorHex;
    private String blindedPublicKeyHex;
    private Long amount;
    private String id;

    public BlindingInfo(String secretXHex, String blindingFactorHex, String blindedPublicKeyHex) {
        this.secretXHex = secretXHex;
        this.blindingFactorHex = blindingFactorHex;
        this.blindedPublicKeyHex = blindedPublicKeyHex;
    }

    public BlindingInfo(String secretXHex, String blindingFactorHex, String blindedPublicKeyHex, Long amount, String id) {
        this.secretXHex = secretXHex;
        this.blindingFactorHex = blindingFactorHex;
        this.blindedPublicKeyHex = blindedPublicKeyHex;
        this.amount = amount;
        this.id = id;
    }

    public BlindedMessage blindedMessage() {
        return new BlindedMessage(amount.toString(), id, blindedPublicKeyHex);
    }

    @Override
    public String toString() {
        return "BlindingInfo{secretXHex='" + secretXHex + '\'' +
                ", blindingFactorHex='" + blindingFactorHex + '\'' +
                ", blindedPublicKeyHex='" + blindedPublicKeyHex + '\'' + '}';
    }

}
