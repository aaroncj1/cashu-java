package cashu.java.common;


public record BlindSignature(String amount, String C_, String id, DleqProof dleq) {

    @Override
    public String toString() {
        return "MintSignedBlindToken [amount=" + amount + ", C_=" + C_ + ", id=" + id + "]";
    }

    public record DleqProof(String e, String s) {
    }
}
