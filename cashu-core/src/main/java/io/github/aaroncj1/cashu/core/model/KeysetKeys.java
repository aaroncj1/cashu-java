package io.github.aaroncj1.cashu.core.model;

import java.util.Map;

public record KeysetKeys(String id, String unit, Map<String, String> keys) {

    public KeysetKeys(KeysetFullDetails keysetFullDetails) {
        this(keysetFullDetails.id(), keysetFullDetails.unit(), keysetFullDetails.keys().getKeysetStringMap());

    }
}
