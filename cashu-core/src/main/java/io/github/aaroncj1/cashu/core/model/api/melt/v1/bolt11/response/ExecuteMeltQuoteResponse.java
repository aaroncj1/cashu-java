package io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response;

import io.github.aaroncj1.cashu.core.model.BlindSignature;
import java.util.List;

public record ExecuteMeltQuoteResponse(String quote, String amount,
                                       String unit, String request,
                                       String fee_reserve, Integer expiry,
                                       String state, String payment_preimage,
                                       List<BlindSignature> change) {
    public boolean paid() {
        return "PAID".equals(state);
    }
}
