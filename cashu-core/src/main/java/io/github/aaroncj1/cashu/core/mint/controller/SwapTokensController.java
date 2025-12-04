package io.github.aaroncj1.cashu.core.mint.controller;

import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapTokensRequest;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapResponse;

public interface SwapTokensController {

    // /v1/swap
    SwapResponse swapTokens(SwapTokensRequest request);
}
