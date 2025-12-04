package io.github.aaroncj1.cashu.core.model;


public record BlindSignature(String amount, String C_, String id, DleqProof dleq) {

    @Override
    public String toString() {
        return "MintSignedBlindToken [amount=" + amount + ", C_=" + C_ + ", id=" + id + "]";
    }

    public record DleqProof(String e, String s) {
    }
}
