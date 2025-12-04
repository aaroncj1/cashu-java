package io.github.aaroncj1.cashu.core.mint.controller;

import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;

public interface MintInfoController {

    // /v1/info
    MintInfoResponse getMintInfo();
}
