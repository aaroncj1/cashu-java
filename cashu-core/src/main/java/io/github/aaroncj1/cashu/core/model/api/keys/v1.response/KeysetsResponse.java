package io.github.aaroncj1.cashu.core.model.api.keys.v1.response;

import io.github.aaroncj1.cashu.core.model.KeysetSummary;

import java.util.List;

public record KeysetsResponse(List<KeysetSummary> keysets) {
}
