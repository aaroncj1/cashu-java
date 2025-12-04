package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.model.*;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.RequestMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapTokensRequest;
import io.github.aaroncj1.cashu.wallet.proxy.MintHttpClient;
import io.github.aaroncj1.cashu.wallet.service.mappers.ObjectConverts;

import java.util.List;

public class MintFacade {

    private final String mintUrl;


    public MintFacade(String mintUrl) {
        this.mintUrl = mintUrl;
    }

    public KeysetSummary getKeysetSummaryForUnit(String unit) throws Exception {
        List<KeysetSummary> keysetSummaryList = MintHttpClient.getKeysets(mintUrl).keysets();
        for (KeysetSummary keysetSummary : keysetSummaryList) {
            if (keysetSummary.active()) {
                if (keysetSummary.unit().equals(unit))
                    return keysetSummary;
            }
        }
        return null;
    }

    public KeysetKeys getKeysetDetails(String id) throws Exception {
        return MintHttpClient.getKeysById(mintUrl, id).keysets().get(0);
    }

    public RequestMintQuoteResponse mintTokens(RequestMintQuoteRequest request) throws Exception {
        return MintHttpClient.requestMintQuote(mintUrl, "bolt11", request);
    }

    public ExecuteMintQuoteResponse executeMintTokens(ExecuteMintQuoteRequest request) throws Exception {
        return MintHttpClient.mintTokens(mintUrl, "bolt11", request);
    }

    public SwapResponse swapTokens(List<Proof> proofList) throws Exception {
        BlindedMessageService blindedMessageService = new BlindedMessageService();
        List<BlindingInfo> blindingInfoList = blindedMessageService.generateBlindedMessagesToSwapProofs(proofList);
        SwapTokensRequest swapTokensRequest = new SwapTokensRequest(proofList, ObjectConverts.convertListBlindingInfo(blindingInfoList));
        return MintHttpClient.swapTokens(mintUrl, swapTokensRequest);
    }

    public String getMintUrl() {
        return mintUrl;
    }
}
