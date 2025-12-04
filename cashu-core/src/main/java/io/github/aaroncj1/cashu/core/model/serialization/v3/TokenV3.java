package io.github.aaroncj1.cashu.core.model.serialization.v3;

import java.util.List;

public record TokenV3(List<TokenGroupV3> token, String unit, String memo) { }
