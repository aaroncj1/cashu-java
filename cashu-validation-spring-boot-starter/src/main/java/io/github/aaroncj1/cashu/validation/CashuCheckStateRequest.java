package io.github.aaroncj1.cashu.validation;

import io.github.aaroncj1.cashu.core.model.Proof;

import java.util.List;

public record CashuCheckStateRequest(List<Proof> proofs) {
}
