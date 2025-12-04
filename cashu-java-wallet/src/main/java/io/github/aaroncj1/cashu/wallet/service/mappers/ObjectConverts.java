package io.github.aaroncj1.cashu.wallet.service.mappers;

import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.model.BlindedMessage;
import io.github.aaroncj1.cashu.core.model.BlindingInfo;
import io.github.aaroncj1.cashu.core.model.MintProof;
import io.github.aaroncj1.cashu.core.model.Proof;
import io.github.aaroncj1.cashu.core.model.serialization.v3.ProofV3;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenGroupV3;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenV3;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4Group;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectConverts {

    public static List<Proof> convertListMintProofs(List<MintProof> mintProofs) {
        List<Proof> proofList = new ArrayList<>();
        for (MintProof mintProof : mintProofs) {
            proofList.add(mintProof.toProof());
        }
        return proofList;
    }

    public static List<BlindedMessage> convertListBlindingInfo(List<BlindingInfo> blindingInfoList) {
        List<BlindedMessage> blindedMessageList = new ArrayList<>();
        for (BlindingInfo blindingInfo : blindingInfoList) {
            blindedMessageList.add(blindingInfo.blindedMessage());
        }
        return blindedMessageList;
    }

    public static Map<String, List<MintProof>> convertV4ToProofMap(TokenV4 tokenV4) {
        Map<String, List<MintProof>> mintProofMap = new HashMap<>();
        List<MintProof> proofList = new ArrayList<>();
        String mint = tokenV4.mint();
        for (ProofV4Group group : tokenV4.tokens()) {
            String id = CryptoUtils.bytesToHex(group.id());
            for (ProofV4 proofV4 : group.proofs()) {
                MintProof proof = new MintProof(mint, proofV4.amount().toString(), CryptoUtils.bytesToHex(proofV4.C()), id, proofV4.secret());
                proofList.add(proof);
            }
        }
        mintProofMap.put(tokenV4.mint(), proofList);
        return mintProofMap;
    }

    public static Map<String, List<MintProof>> convertV3ToProofMap(TokenV3 tokenV3) {
        Map<String, List<MintProof>> mintProofMap = new HashMap<>();
        for (TokenGroupV3 tokenGroupV3 : tokenV3.token()) {
            String mint = tokenGroupV3.mint();
            List<MintProof> mintProofs = new ArrayList<>();
            for (ProofV3 proofV3 : tokenGroupV3.proofs()) {
                MintProof proof = new MintProof(mint, proofV3.amount().toString(), proofV3.C(), proofV3.id(), proofV3.secret());
                mintProofs.add(proof);
            }
            mintProofMap.put(mint, mintProofs);
        }
        return mintProofMap;
    }

}
