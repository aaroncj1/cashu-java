package io.github.aaroncj1.cashu.core.model.serialization.v4;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProofV4Group(@JsonProperty("i") byte[] id, @JsonProperty("p") List<ProofV4> proofs) {
}
