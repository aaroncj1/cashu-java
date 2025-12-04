package cashu.java.wallet;

import cashu.java.common.BlindedMessage;
import cashu.java.common.Keyset;
import cashu.java.common.Proof;
import cashu.java.common.api.request.SwapTokensRequest;
import cashu.java.common.api.response.PRInvoice;
import cashu.java.common.api.request.ClaimTokensRequest;
import cashu.java.common.api.response.InvoiceResponse;
import cashu.java.common.api.response.InvoiceStatusResponse;
import cashu.java.common.api.response.KeysetsResponse;
import cashu.java.common.api.response.MintTokensResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MintClientImpl implements MintClient {

    private final String mintBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Keyset> keysets = new HashMap<>();
    private final Map<String, Keyset> activeKeysets = new HashMap<>();
    private final Map<String, PRInvoice> paymentRequests = new HashMap<>();

    public MintClientImpl(String mintBaseUrl) throws IOException, InterruptedException {
        this.mintBaseUrl = mintBaseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        refreshKeysets(); // Fetch keysets on initialization
    }

    @Override
    public PRInvoice requestMintInvoice(String amount) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/mint?amount=" + amount))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        InvoiceResponse invoiceResponse = objectMapper.readValue(response.body(), InvoiceResponse.class);
        paymentRequests.put(invoiceResponse.hash, new PRInvoice(invoiceResponse, false));
        return new PRInvoice(invoiceResponse, false);
    }

    @Override
    public boolean checkInvoicePaid(String hash) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/v1/mint/quote/bolt11/" + hash))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        InvoiceStatusResponse invoiceStatusResponse = objectMapper.readValue(response.body(), InvoiceStatusResponse.class);

        if (invoiceStatusResponse.paid)
            paymentRequests.put(hash, new PRInvoice(invoiceStatusResponse, true));
        return invoiceStatusResponse.paid;
    }

    @Override
    public MintTokensResponse claimTokens(List<BlindedMessage> blindedTokens, String quote) throws IOException, InterruptedException {
        ClaimTokensRequest claimTokensRequest = new ClaimTokensRequest(quote, blindedTokens);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/v1/mint/bolt11"))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(claimTokensRequest)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), MintTokensResponse.class);
    }

    public MintTokensResponse swapTokens(List<Proof> inputs, List<BlindedMessage> outputs) throws IOException, InterruptedException {
        SwapTokensRequest swapTokensRequest = new SwapTokensRequest(inputs, outputs);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/v1/swap"))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(swapTokensRequest)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), MintTokensResponse.class);
    }

    @Override
    public void meltTokens() {

    }

    @Override
    public void refreshKeysets() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/v1/keysets"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        KeysetsResponse keysetsResponse = objectMapper.readValue(response.body(), KeysetsResponse.class);

        keysets.clear();
        for (Keyset ks : keysetsResponse.keysets) {
            keysets.put(ks.id(), ks);
            if (ks.active()) {
                activeKeysets.put(ks.unit(), ks);
            }
        }
    }

    public Map<String, String> getKeys(String unit) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mintBaseUrl + "/keys/" + getActiveKey(unit)))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readValue(response.body(), new TypeReference<>(){});
    }

    public String getActiveKey(String unit) {
        return activeKeysets.get(unit).id();
    }

    public String getMintBaseUrl() {
        return mintBaseUrl;
    }
}
