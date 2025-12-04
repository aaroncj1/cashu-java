package io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response;

public record RequestMintQuoteResponse(String quote, String request,
                                       String unit, String amount,
                                       String state, String expiry,
                                       String pubkey, String paid) {
}
