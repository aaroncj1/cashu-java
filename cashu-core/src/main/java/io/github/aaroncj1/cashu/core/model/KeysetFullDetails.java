package io.github.aaroncj1.cashu.core.model;

import io.github.aaroncj1.cashu.core.mint.Keys;

public record KeysetFullDetails(String id, Integer input_fee_ppk, String unit, boolean active, Keys keys, String path) {

    public KeysetFullDetails(KeysetSummary keysetSummary, Keys keys, String path) {
        this(keysetSummary.id(), keysetSummary.input_fee_ppk(), keysetSummary.unit(), keysetSummary.active(), keys, path);
    }
}
