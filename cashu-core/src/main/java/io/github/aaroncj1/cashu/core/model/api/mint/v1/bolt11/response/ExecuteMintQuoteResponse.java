package io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response;

import io.github.aaroncj1.cashu.core.model.BlindSignature;

import java.util.List;

public class ExecuteMintQuoteResponse {
    public List<BlindSignature> signatures;
}
