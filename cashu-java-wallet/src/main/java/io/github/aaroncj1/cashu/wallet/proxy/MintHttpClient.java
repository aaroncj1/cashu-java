package io.github.aaroncj1.cashu.wallet.proxy;

import io.github.aaroncj1.cashu.core.model.KeysetFullDetails;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.ActiveKeysetsResponse;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.KeysetsResponse;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request.ExecuteMeltQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request.RequestMeltQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response.ExecuteMeltQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response.RequestMeltQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.RequestMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapTokensRequest;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

public class MintHttpClient {

    private static RestClient client(String mintBaseUrl) {
        Assert.hasText(mintBaseUrl, "mintBaseUrl must not be empty");
        return RestClient.builder().baseUrl(mintBaseUrl).build();
    }

    public static MintInfoResponse getMintInfo(String mintBaseUrl) {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/info")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(MintInfoResponse.class);
    }

    public static ActiveKeysetsResponse getActiveKeys(String mintBaseUrl) throws Exception {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/keys")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ActiveKeysetsResponse.class);
    }

    public static ActiveKeysetsResponse getKeysById(String mintBaseUrl, String keysetId) throws Exception {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/keys/{keysetId}", keysetId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ActiveKeysetsResponse.class);
    }

    public static KeysetsResponse getKeysets(String mintBaseUrl) throws Exception {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/keysets")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KeysetsResponse.class);
    }

    public static RequestMintQuoteResponse requestMintQuote(String mintBaseUrl, String method, RequestMintQuoteRequest request) throws Exception {
        return client(mintBaseUrl)
                .post()
                .uri("/v1/mint/quote/{method}", method)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RequestMintQuoteResponse.class);
    }

    public static RequestMintQuoteResponse mintState(String mintBaseUrl, String method, String quoteId) throws Exception {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/mint/quote/{method}/{quoteId}", method, quoteId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RequestMintQuoteResponse.class);
    }

    public static ExecuteMintQuoteResponse mintTokens(String mintBaseUrl, String method, ExecuteMintQuoteRequest request) throws Exception {
        return client(mintBaseUrl)
                .post()
                .uri("/v1/mint/{method}", method)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ExecuteMintQuoteResponse.class);
    }

    public static RequestMeltQuoteResponse requestMeltQuote(String mintBaseUrl, String method, RequestMeltQuoteRequest request) throws Exception {
        return client(mintBaseUrl)
                .post()
                .uri("/v1/melt/quote/{method}", method)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RequestMeltQuoteResponse.class);
    }

    public static RequestMeltQuoteResponse meltState(String mintBaseUrl, String method, String quoteId) throws Exception {
        return client(mintBaseUrl)
                .get()
                .uri("/v1/melt/quote/{method}/{quoteId}", method, quoteId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RequestMeltQuoteResponse.class);
    }

    public static ExecuteMeltQuoteResponse executeMeltTokens(String mintBaseUrl, String method, ExecuteMeltQuoteRequest request) throws Exception {
        return client(mintBaseUrl)
                .post()
                .uri("/v1/melt/{method}", method)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ExecuteMeltQuoteResponse.class);
    }

    public static SwapResponse swapTokens(String mintBaseUrl, SwapTokensRequest request) throws Exception {
        return client(mintBaseUrl)
                .post()
                .uri("/v1/swap")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SwapResponse.class);
    }
}