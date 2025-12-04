package io.github.aaroncj1.cashu.core.model.api.swap.v1;

import io.github.aaroncj1.cashu.core.model.BlindedMessage;
import io.github.aaroncj1.cashu.core.model.Proof;

import java.util.List;

public record SwapTokensRequest(List<Proof> inputs, List<BlindedMessage> outputs) {
}
