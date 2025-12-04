package io.github.aaroncj1.cashu.core.model.serialization.v4;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TokenV4(@JsonProperty("m") String mint,
                      @JsonProperty("u") String unit,
                      @JsonProperty("d") String memo,
                      @JsonProperty("t") List<ProofV4Group> tokens) {
}
