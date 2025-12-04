package io.github.aaroncj1.cashu.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DleqProof(@JsonProperty("e") byte[] e,
                        @JsonProperty("s") byte[] s,
                        @JsonProperty("r") byte[] r) {
}
