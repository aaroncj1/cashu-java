package io.github.aaroncj1.cashu.core.mint.controller;

import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.RequestMintQuoteResponse;

public interface MintController {

    // POST /v1/mint/quote/{method} GENERATES QUOTE TO PAY
    RequestMintQuoteResponse requestMintQuote(String method, RequestMintQuoteRequest body);

    // GET /v1/mint/quote/{method}/{quoteID} CHECKS QUOTE STATUS
    RequestMintQuoteResponse checkMintStatus(String method, String quoteID);

    // POST /v1/mint/{method} CLAIMS THE TOKENS
    ExecuteMintQuoteResponse claimTokens(String method, ExecuteMintQuoteRequest request);

}
