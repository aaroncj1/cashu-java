package io.github.aaroncj1.cashu.core.mint.controller;

import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request.ExecuteMeltQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request.RequestMeltQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response.ExecuteMeltQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response.RequestMeltQuoteResponse;

public interface MeltController {

    // POST /v1/melt/quote/{method}
    RequestMeltQuoteResponse requestMeltQuote(String method, RequestMeltQuoteRequest request);

    // GET /v1/melt/quote/{method}/{quote_id}
    RequestMeltQuoteResponse meltState(String method, String quoteId);

    // POST /v1/melt/{method}
    ExecuteMeltQuoteResponse executeMeltTokens(String method, ExecuteMeltQuoteRequest request);
}
