package io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response;

public record ExecuteMeltQuoteResponse(String quote, String amount,
                                       String unit, String request,
                                       String fee_reserve, Integer expiry,
                                       String state, String payment_preimage,
                                       String change) {
}
