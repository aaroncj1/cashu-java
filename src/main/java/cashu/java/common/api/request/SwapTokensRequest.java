package cashu.java.common.api.request;

import cashu.java.common.BlindedMessage;
import cashu.java.common.Proof;

import java.util.List;

public record SwapTokensRequest(List<Proof> inputs, List<BlindedMessage> outputs) {
}
