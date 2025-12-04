package io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request;

import io.github.aaroncj1.cashu.core.model.Proof;

import java.util.List;

public record ExecuteMeltQuoteRequest(String quote, List<Proof> inputs) {
}
