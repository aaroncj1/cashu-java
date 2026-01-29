package io.github.aaroncj1.cashu.validation;

import java.util.Collections;
import java.util.List;

public record CashuMintCheckResult(boolean spendable,
                                   List<Boolean> spendableList,
                                   List<Boolean> pendingList,
                                   List<String> states,
                                   String message) {
    public static CashuMintCheckResult spendable(List<Boolean> spendableList, List<Boolean> pendingList) {
        return new CashuMintCheckResult(true,
                spendableList == null ? Collections.emptyList() : spendableList,
                pendingList == null ? Collections.emptyList() : pendingList,
                Collections.emptyList(),
                null);
    }

    public static CashuMintCheckResult spendableFromStates(List<String> states) {
        return new CashuMintCheckResult(true,
                Collections.emptyList(),
                Collections.emptyList(),
                states == null ? Collections.emptyList() : states,
                null);
    }

    public static CashuMintCheckResult notSpendable(String message,
                                                    List<Boolean> spendableList,
                                                    List<Boolean> pendingList,
                                                    List<String> states) {
        return new CashuMintCheckResult(false,
                spendableList == null ? Collections.emptyList() : spendableList,
                pendingList == null ? Collections.emptyList() : pendingList,
                states == null ? Collections.emptyList() : states,
                message);
    }
}
