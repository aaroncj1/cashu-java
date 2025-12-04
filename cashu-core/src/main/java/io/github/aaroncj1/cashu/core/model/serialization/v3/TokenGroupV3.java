package io.github.aaroncj1.cashu.core.model.serialization.v3;

import java.util.List;

public record TokenGroupV3(String mint, List<ProofV3> proofs) {
}
