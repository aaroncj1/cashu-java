package cashu.java.common.api.request;

import cashu.java.common.BlindedMessage;

import java.util.List;

public record ClaimTokensRequest(String quote, List<BlindedMessage> outputs) {}
