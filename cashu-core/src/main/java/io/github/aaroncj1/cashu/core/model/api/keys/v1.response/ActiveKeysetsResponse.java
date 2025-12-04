package io.github.aaroncj1.cashu.core.model.api.keys.v1.response;

import io.github.aaroncj1.cashu.core.model.KeysetKeys;

import java.util.List;

public record ActiveKeysetsResponse(List<KeysetKeys> keysets) {
}

