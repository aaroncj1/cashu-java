package io.github.aaroncj1.cashu.validation;

import io.github.aaroncj1.cashu.core.model.Proof;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CashuTokenValidator {
    private final CashuTokenParser parser;
    private final CashuMintClient mintClient;
    private final CashuValidationProperties properties;

    public CashuTokenValidator(CashuTokenParser parser,
                               CashuMintClient mintClient,
                               CashuValidationProperties properties) {
        this.parser = parser;
        this.mintClient = mintClient;
        this.properties = properties;
    }

    public CashuValidationResult validate(String token, long minAmount) {
        ParsedCashuToken parsed;
        try {
            parsed = parser.parse(token);
        } catch (Exception ex) {
            return CashuValidationResult.invalid("Invalid Cashu token: " + ex.getMessage(), 0L, null, null);
        }

        if (parsed.proofsByMint().isEmpty()) {
            return CashuValidationResult.invalid("Token contains no proofs", 0L, parsed.unit(), null);
        }

        if (!properties.isAllowMultipleMints() && parsed.proofsByMint().size() > 1) {
            return CashuValidationResult.invalid("Multiple mints not allowed", 0L, parsed.unit(), null);
        }

        long totalAmount;
        try {
            totalAmount = parsed.totalAmount();
        } catch (NumberFormatException ex) {
            return CashuValidationResult.invalid("Invalid proof amount", 0L, parsed.unit(), null);
        }

        if (totalAmount < minAmount) {
            return CashuValidationResult.invalid(
                    "Token amount " + totalAmount + " is below required " + minAmount,
                    totalAmount,
                    parsed.unit(),
                    null);
        }

        Map<String, CashuMintCheckResult> checks = new LinkedHashMap<>();
        boolean allSpendable = true;
        for (Map.Entry<String, List<Proof>> entry : parsed.proofsByMint().entrySet()) {
            CashuMintCheckResult check = mintClient.check(entry.getKey(), entry.getValue());
            checks.put(entry.getKey(), check);
            if (!check.spendable()) {
                allSpendable = false;
            }
        }

        if (!allSpendable) {
            String message = checks.values().stream()
                    .map(CashuMintCheckResult::message)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("Token proofs are not spendable");
            return CashuValidationResult.invalid(message, totalAmount, parsed.unit(), checks);
        }

        return CashuValidationResult.valid(totalAmount, parsed.unit(), checks);
    }
}
