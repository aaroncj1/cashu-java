package io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request;

import io.github.aaroncj1.cashu.core.model.BlindedMessage;

import java.util.List;

public record ExecuteMintQuoteRequest(String quote, List<BlindedMessage> outputs) {}
