package io.github.aaroncj1.cashu.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aaroncj1.cashu.core.model.Proof;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CashuMintClient {
    private final RestClient.Builder restClientBuilder;
    private final CashuValidationProperties properties;
    private final ObjectMapper objectMapper;

    public CashuMintClient(RestClient.Builder restClientBuilder,
                           CashuValidationProperties properties,
                           ObjectMapper objectMapper) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public CashuMintCheckResult check(String mintUrl, List<Proof> proofs) {
        if (!StringUtils.hasText(mintUrl)) {
            return CashuMintCheckResult.notSpendable("Missing mint URL", null, null, null);
        }
        if (proofs == null || proofs.isEmpty()) {
            return CashuMintCheckResult.notSpendable("No proofs supplied", null, null, null);
        }

        try {
            JsonNode response = restClientBuilder.baseUrl(mintUrl)
                    .build()
                    .post()
                    .uri(properties.getCheckEndpointPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new CashuCheckStateRequest(proofs))
                    .retrieve()
                    .body(JsonNode.class);

            return parseCheckResponse(response, proofs.size());
        } catch (RestClientResponseException ex) {
            return CashuMintCheckResult.notSpendable(
                    "Mint check failed with status " + ex.getRawStatusCode(), null, null, null);
        } catch (Exception ex) {
            return CashuMintCheckResult.notSpendable("Mint check failed: " + ex.getMessage(), null, null, null);
        }
    }

    private CashuMintCheckResult parseCheckResponse(JsonNode response, int expectedCount) {
        if (response == null || response.isNull()) {
            return CashuMintCheckResult.notSpendable("Empty mint response", null, null, null);
        }

        if (response.has("spendable")) {
            List<Boolean> spendable = readBooleanList(response.get("spendable"));
            List<Boolean> pending = readBooleanList(response.get("pending"));
            if (spendable == null || spendable.size() != expectedCount) {
                return CashuMintCheckResult.notSpendable("Spendable list size mismatch", spendable, pending, null);
            }
            boolean allSpendable = spendable.stream().allMatch(Boolean.TRUE::equals);
            if (!allSpendable) {
                return CashuMintCheckResult.notSpendable("One or more proofs are spent", spendable, pending, null);
            }
            if (properties.isRejectPending() && hasTrue(pending)) {
                return CashuMintCheckResult.notSpendable("One or more proofs are pending", spendable, pending, null);
            }
            return CashuMintCheckResult.spendable(spendable, pending);
        }

        if (response.has("states")) {
            List<String> states = readStates(response.get("states"));
            if (states.size() != expectedCount) {
                return CashuMintCheckResult.notSpendable("State list size mismatch", null, null, states);
            }
            CashuMintCheckResult result = evaluateStates(states);
            if (result.spendable()) {
                return result;
            }
            return result;
        }

        if (response.has("state")) {
            List<String> states = new ArrayList<>();
            states.add(response.get("state").asText());
            return evaluateStates(states);
        }

        return CashuMintCheckResult.notSpendable("Unsupported mint check response", null, null, null);
    }

    private CashuMintCheckResult evaluateStates(List<String> states) {
        boolean hasSpent = false;
        boolean hasPending = false;
        for (String state : states) {
            String normalized = state == null ? "" : state.trim().toLowerCase(Locale.ROOT);
            if ("spent".equals(normalized) || "invalid".equals(normalized)) {
                hasSpent = true;
            }
            if ("pending".equals(normalized)) {
                hasPending = true;
            }
        }
        if (hasSpent) {
            return CashuMintCheckResult.notSpendable("One or more proofs are spent", null, null, states);
        }
        if (properties.isRejectPending() && hasPending) {
            return CashuMintCheckResult.notSpendable("One or more proofs are pending", null, null, states);
        }
        return CashuMintCheckResult.spendableFromStates(states);
    }

    private List<Boolean> readBooleanList(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return objectMapper.convertValue(node, new TypeReference<>() { });
    }

    private List<String> readStates(JsonNode node) {
        List<String> states = new ArrayList<>();
        if (node == null || node.isNull()) {
            return states;
        }
        for (JsonNode entry : node) {
            if (entry.isTextual()) {
                states.add(entry.asText());
                continue;
            }
            if (entry.has("state")) {
                states.add(entry.get("state").asText());
            }
        }
        return states;
    }

    private boolean hasTrue(List<Boolean> values) {
        if (values == null) {
            return false;
        }
        return values.stream().anyMatch(Boolean.TRUE::equals);
    }
}
