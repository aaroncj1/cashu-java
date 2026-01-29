package io.github.aaroncj1.cashu.validation;

import io.github.aaroncj1.cashu.core.model.Proof;

import java.util.List;
import java.util.Map;

public record ParsedCashuToken(String unit,
                               String memo,
                               Map<String, List<Proof>> proofsByMint) {
    public long totalAmount() {
        long total = 0;
        for (List<Proof> proofs : proofsByMint.values()) {
            for (Proof proof : proofs) {
                total += Long.parseLong(proof.amount());
            }
        }
        return total;
    }
}
