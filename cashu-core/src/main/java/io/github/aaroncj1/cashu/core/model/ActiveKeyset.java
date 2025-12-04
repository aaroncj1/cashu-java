package io.github.aaroncj1.cashu.core.model;

import java.util.Map;

public record ActiveKeyset(String id, String unit, Map<String, String> keys) {
}