package io.github.aaroncj1.cashu.validation;

import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.model.Proof;
import io.github.aaroncj1.cashu.core.model.serialization.v3.ProofV3;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenGroupV3;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenV3;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4Group;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;
import io.github.aaroncj1.cashu.core.serialization.TokenCodec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CashuTokenParser {
    public ParsedCashuToken parse(String token) throws IOException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Cashu token is empty");
        }

        if (token.startsWith(TokenCodec.V4_PREFIX)) {
            return parseV4(token);
        }
        if (token.startsWith(TokenCodec.V3_PREFIX)) {
            return parseV3(token);
        }

        throw new IllegalArgumentException("Unsupported Cashu token prefix");
    }

    private ParsedCashuToken parseV4(String token) throws IOException {
        TokenV4 tokenV4 = TokenCodec.deserializeV4(token);
        if (tokenV4.mint() == null || tokenV4.mint().isBlank()) {
            throw new IllegalArgumentException("Token is missing mint URL");
        }

        Map<String, List<Proof>> proofsByMint = new LinkedHashMap<>();
        List<Proof> proofs = new ArrayList<>();
        if (tokenV4.tokens() != null) {
            for (ProofV4Group group : tokenV4.tokens()) {
                if (group.id() == null) {
                    throw new IllegalArgumentException("Token is missing keyset id");
                }
                String keysetId = CryptoUtils.bytesToHex(group.id());
                if (group.proofs() == null) {
                    continue;
                }
                for (ProofV4 proof : group.proofs()) {
                    if (proof.C() == null || proof.secret() == null) {
                        throw new IllegalArgumentException("Token proof is missing required fields");
                    }
                    proofs.add(new Proof(
                            proof.amount() == null ? "0" : proof.amount().toString(),
                            CryptoUtils.bytesToHex(proof.C()),
                            keysetId,
                            proof.secret()
                    ));
                }
            }
        }
        proofsByMint.put(tokenV4.mint(), proofs);
        return new ParsedCashuToken(tokenV4.unit(), tokenV4.memo(), proofsByMint);
    }

    private ParsedCashuToken parseV3(String token) throws IOException {
        TokenV3 tokenV3 = TokenCodec.deserializeV3(token);
        Map<String, List<Proof>> proofsByMint = new LinkedHashMap<>();
        if (tokenV3.token() != null) {
            for (TokenGroupV3 group : tokenV3.token()) {
                String mint = group.mint();
                if (mint == null || mint.isBlank()) {
                    throw new IllegalArgumentException("Token group is missing mint URL");
                }
                List<Proof> proofs = proofsByMint.computeIfAbsent(mint, key -> new ArrayList<>());
                if (group.proofs() == null) {
                    continue;
                }
                for (ProofV3 proof : group.proofs()) {
                    if (proof.C() == null || proof.secret() == null || proof.id() == null) {
                        throw new IllegalArgumentException("Token proof is missing required fields");
                    }
                    proofs.add(new Proof(
                            proof.amount() == null ? "0" : proof.amount().toString(),
                            proof.C(),
                            proof.id(),
                            proof.secret()
                    ));
                }
            }
        }
        return new ParsedCashuToken(tokenV3.unit(), tokenV3.memo(), proofsByMint);
    }
}
