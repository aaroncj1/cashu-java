package io.github.aaroncj1.cashu.validation;

import java.util.Collections;
import java.util.Map;

public record CashuValidationResult(boolean valid,
                                    long totalAmount,
                                    String unit,
                                    Map<String, CashuMintCheckResult> mintChecks,
                                    String message) {
    public static final String REQUEST_ATTRIBUTE = "cashu.validation.result";

    public static CashuValidationResult valid(long totalAmount,
                                              String unit,
                                              Map<String, CashuMintCheckResult> mintChecks) {
        return new CashuValidationResult(true, totalAmount, unit,
                mintChecks == null ? Collections.emptyMap() : mintChecks, null);
    }

    public static CashuValidationResult invalid(String message,
                                                long totalAmount,
                                                String unit,
                                                Map<String, CashuMintCheckResult> mintChecks) {
        return new CashuValidationResult(false, totalAmount, unit,
                mintChecks == null ? Collections.emptyMap() : mintChecks, message);
    }
}
