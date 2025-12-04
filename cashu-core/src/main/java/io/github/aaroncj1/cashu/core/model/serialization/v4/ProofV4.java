package io.github.aaroncj1.cashu.core.model.serialization.v4;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aaroncj1.cashu.core.model.DleqProof;

public record ProofV4(@JsonProperty("a") Long amount,
                      @JsonProperty("s") String secret,
                      @JsonProperty("c") byte[] C,
                      @JsonProperty("d") DleqProof dleqProof,
                      @JsonProperty("w") String witness) {
}
