package io.github.aaroncj1.cashu.wallet.proxy;

import io.github.aaroncj1.cashu.core.crypto.impl.BlindDHKEServiceImpl;
import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.model.BlindedMessage;
import io.github.aaroncj1.cashu.core.model.BlindingInfo;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.ActiveKeysetsResponse;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.KeysetsResponse;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.request.RequestMeltQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.melt.v1.bolt11.response.RequestMeltQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.RequestMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

class MintHttpClientTest {

    @Test
    void testInfo() {
        MintInfoResponse response = MintHttpClient.getMintInfo("https://testnut.cashu.space");
        System.out.println(response.toString());
        response.getNuts().forEach((integer, nut) -> System.out.println(integer + " " + nut));
    }

    @Test
    void testKeys() throws Exception {
        ActiveKeysetsResponse response = MintHttpClient.getActiveKeys("https://testnut.cashu.space");
        System.out.println(response.toString());
    }

    @Test
    void testKeyset() throws Exception {
        KeysetsResponse response = MintHttpClient.getKeysets("https://testnut.cashu.space");
        System.out.println(response.toString());
    }

    @Test
    void testKeysetById() throws Exception {
        ActiveKeysetsResponse response = MintHttpClient.getKeysById("https://testnut.cashu.space", "0042ade98b2a370a");
        System.out.println(response.toString());
    }

    @Test
    void testMint() throws Exception {
        RequestMintQuoteRequest request = new RequestMintQuoteRequest("sat", "100");
        RequestMintQuoteResponse quoteResponse = MintHttpClient.requestMintQuote("https://testnut.cashu.space", "bolt11", request);
        RequestMintQuoteResponse checkedResponse = MintHttpClient.mintState("https://testnut.cashu.space", "bolt11", quoteResponse.quote());
        System.out.println(checkedResponse.toString());
        Assertions.assertEquals(quoteResponse.quote(), checkedResponse.quote());
    }

    @Test
    void testExecuteMint() throws Exception {
        RequestMintQuoteRequest request = new RequestMintQuoteRequest("sat", "96");
        RequestMintQuoteResponse quoteResponse = MintHttpClient.requestMintQuote("https://testnut.cashu.space", "bolt11", request);

        String secret = CryptoUtils.randomHex();
        BigInteger r = CryptoUtils.randomScalar();
        BlindingInfo blindingInfo = BlindDHKEServiceImpl.createBlindMessage(secret, r);
        String secret2 = CryptoUtils.randomHex();
        BigInteger r2 = CryptoUtils.randomScalar();
        BlindingInfo blindingInfo2 = BlindDHKEServiceImpl.createBlindMessage(secret2, r2);
        BlindedMessage blindedMessage = new BlindedMessage("64", "00e9d961fcf4393e", blindingInfo.getBlindedPublicKeyHex());
        BlindedMessage blindedMessage2 = new BlindedMessage("32", "00e9d961fcf4393e", blindingInfo2.getBlindedPublicKeyHex());

        ExecuteMintQuoteRequest executeMintQuoteRequest = new ExecuteMintQuoteRequest(quoteResponse.quote(), List.of(blindedMessage, blindedMessage2));
        ExecuteMintQuoteResponse executeMintQuoteResponse = MintHttpClient.mintTokens("https://testnut.cashu.space", "bolt11", executeMintQuoteRequest);
        System.out.println(executeMintQuoteResponse.toString());
    }

    @Test
    void testMelt() throws Exception {
        RequestMeltQuoteRequest request = new RequestMeltQuoteRequest("lnbc50n1p5t798xpp5906esvmc9e8wl8c4p49l0pumxf8ez3rd38gcl9g53yhg88xzdnaqdqqcqzrcxqr8qasp5jh7nxpsnqnqt2cgmhyl7mkuarv6753yggqrnvrfkfxmgyllde2dq9qxpqysgq8l6zaxrq72unn73250s2mfaya04lxz6f9j8v2n2k5yyx0jfmn42hf9k3nahrwqyp67vjqmqwr6x9q5ua5q2wqpx94mzpg9rvycx200qp9hggv0", "sat");
        RequestMeltQuoteResponse quoteResponse = MintHttpClient.requestMeltQuote("https://testnut.cashu.space", "bolt11", request);
        RequestMeltQuoteResponse checkedResponse = MintHttpClient.meltState("https://testnut.cashu.space", "bolt11", quoteResponse.quote());
        System.out.println(checkedResponse.toString());
        Assertions.assertEquals(quoteResponse.quote(), checkedResponse.quote());
    }

    @Test
    void testMintTokens() throws Exception {
        ExecuteMintQuoteRequest request = new ExecuteMintQuoteRequest(null, null);
        ExecuteMintQuoteResponse response = MintHttpClient.mintTokens("https://testnut.cashu.space", "bolt11", request);
        System.out.println(response.toString());
    }

//    @Test
//    void testSwapTokens() throws Exception {
//        MintHttpClient mintHttpClient = new MintHttpClient();
//        CryptoUtils cryptoUtils = new CryptoUtils();
//        String secret = CryptoUtils.randomHex();
//        BigInteger r = CryptoUtils.randomScalar();
//        BlindingInfo blindingInfo = BlindDHKEServiceImpl.createBlindMessage(secret, r);
//
//        SwapTokensRequest request = new SwapTokensRequest();
//        SwapResponse response = mintHttpClient.swapTokens(, request);
//        System.out.println(response.toString());
//    }
//
}