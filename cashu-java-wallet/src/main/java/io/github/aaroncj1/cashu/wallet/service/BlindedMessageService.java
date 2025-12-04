package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.crypto.impl.BlindDHKEServiceImpl;
import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.model.*;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4Group;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlindedMessageService {

    public BlindedMessageService() {
    }

    public List<BlindingInfo> generateBlindedMessagesToSwapProofs(List<Proof> proofs) throws Exception {
        List<BlindingInfo> blindingInfoList = new ArrayList<>();
        proofs.forEach(proof -> {
            try {
                blindingInfoList.addAll(generateBlindedMessagesForAmount(Long.valueOf(proof.amount()), proof.id()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return blindingInfoList;

    }

        public List<BlindingInfo> generateBlindedMessagesForAmount(Long amount, String id) throws Exception {
        List<Long> denominations = splitAmountIntoDenominations(amount);
        List<BlindingInfo> blindingInfoList = new ArrayList<>();
        for (Long denomination : denominations) {
            String secret = CryptoUtils.randomHex();
            BigInteger r = CryptoUtils.randomScalar();
            BlindingInfo blindingInfo = BlindDHKEServiceImpl.createBlindMessage(secret, r);
            blindingInfo.setAmount(denomination);
            blindingInfo.setId(id);
            blindingInfoList.add(blindingInfo);
        }
        return blindingInfoList;
    }

    public TokenV4 unblindMessagesToSend(KeysetKeys keys, String mint, String unit, String memo, List<BlindSignature> blindSignatures, List<BlindingInfo> blindingInfoList) throws Exception {
        List<ProofV4> proofV4List = new ArrayList<>();
        blindingInfoList.forEach(blindingInfo -> {
            blindSignatures.forEach(blindSignature -> {
                if (blindingInfo.getAmount().toString().equals(blindSignature.amount()) && blindingInfo.getId().equals(blindSignature.id())) {
                    System.out.println("Unblinding: " + blindingInfo.getId());
                    System.out.println("Unblinding: " + blindingInfo.getBlindingFactorHex());
                    System.out.println("Unblinding: " + blindSignature.C_());
                    String key = keys.keys().get(blindSignature.amount());
                    String C = BlindDHKEServiceImpl.unblindSignature(key, blindingInfo.getBlindingFactorHex(), blindSignature.C_());
                    ProofV4 proofV4 = new ProofV4(blindingInfo.getAmount(), blindingInfo.getSecretXHex(), C.getBytes(), null, null);
                    proofV4List.add(proofV4);
                }
            });
        });
        List<ProofV4Group> groupList = List.of(new ProofV4Group(blindingInfoList.get(0).getId().getBytes(), proofV4List));
        return new TokenV4(mint, unit, memo, groupList);
    }

    public BlindingInfo createProofToSwap(MintProof proof, String newId) throws Exception {
        String secret = CryptoUtils.randomHex();
        BigInteger r = CryptoUtils.randomScalar();
        BlindingInfo blindingInfo = BlindDHKEServiceImpl.createBlindMessage(secret, r);
        blindingInfo.setAmount(Long.valueOf(proof.amount()));
        blindingInfo.setId(newId);

        return blindingInfo;
    }

    public List<Long> splitAmountIntoDenominations(long amount) {
        List<Long> denominations = new ArrayList<>();
        for (int i = 64; i >= 0; i--) {
            if (amount >= (long) Math.pow(2, i)) {
                amount -= (long) Math.pow(2, i);
                denominations.add((long) Math.pow(2, i));
                if (amount == 0) {
                    return denominations;
                }
            }
        }
        return null;
    }
}
