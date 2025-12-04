package io.github.aaroncj1.cashu.core.model;

import java.util.List;

public record Token(List<Proof> proofs, String mintUrl) {
}
