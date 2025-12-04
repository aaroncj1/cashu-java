package io.github.aaroncj1.cashu.core.model;

public record MintProof(String mintUrl, String amount, String C, String id, String secret) {

    public Proof toProof() {
        return new Proof(amount, C, id, secret);
    }
}
