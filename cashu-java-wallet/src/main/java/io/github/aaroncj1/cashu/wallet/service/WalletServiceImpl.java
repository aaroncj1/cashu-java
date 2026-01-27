package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.model.*;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapTokensRequest;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenV3;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4Group;
import io.github.aaroncj1.cashu.core.serialization.TokenCodec;
import io.github.aaroncj1.cashu.wallet.persisence.TokenEntity;
import io.github.aaroncj1.cashu.wallet.persisence.TokenRepository;
import io.github.aaroncj1.cashu.wallet.service.mappers.ObjectConverts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.aaroncj1.cashu.core.serialization.TokenCodec.V3_PREFIX;
import static io.github.aaroncj1.cashu.core.serialization.TokenCodec.V4_PREFIX;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public boolean receiveEcash(String token) throws Exception {
        Map<String, List<MintProof>> mintProofList;
        String unit;
        String memo = null;
        if (token.startsWith(V3_PREFIX)) {
            TokenV3 tokenV3 = TokenCodec.deserializeV3(token);
            mintProofList = ObjectConverts.convertV3ToProofMap(tokenV3);
            memo = tokenV3.memo();
            unit = tokenV3.unit();
        } else if (token.startsWith(V4_PREFIX)) {
            TokenV4 tokenV4 = TokenCodec.deserializeV4(token);
            mintProofList = ObjectConverts.convertV4ToProofMap(tokenV4);
            memo = tokenV4.memo();
            unit = tokenV4.unit();
        } else {
            throw new IllegalArgumentException("Not a valid token");
        }

        BlindedMessageService blindedMessageService = new BlindedMessageService();

        for (Map.Entry<String, List<MintProof>> entry : mintProofList.entrySet()) {
            String mintUrl = entry.getKey();
            MintFacade mintFacade = new MintFacade(mintUrl);

            // Get active keyset for unit
            KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit(unit);
            String newId = activeKeyset.id();
            KeysetKeys keys = mintFacade.getKeysetDetails(newId);

            List<BlindingInfo> newBlindingInfoList = new ArrayList<>();
            for (MintProof mintProof : entry.getValue()) {
                BlindingInfo blindingInfo = blindedMessageService.createProofToSwap(mintProof, newId);
                newBlindingInfoList.add(blindingInfo);
            }

            SwapTokensRequest swapTokensRequest = new SwapTokensRequest(
                    ObjectConverts.convertListMintProofs(entry.getValue()),
                    ObjectConverts.convertListBlindingInfo(newBlindingInfoList)
            );

            SwapResponse response = mintFacade.swapTokens(swapTokensRequest);

            // Unblind and verify
            TokenV4 newToken = blindedMessageService.unblindMessagesToSend(
                    keys, mintUrl, unit, memo, response.signatures, newBlindingInfoList
            );

            // Save to DB
            saveTokenV4(newToken);
        }

        return true;
    }

    private void saveTokenV4(TokenV4 token) {
        String mint = token.mint();
        String unit = token.unit();
        String memo = token.memo();

        for (ProofV4Group group : token.tokens()) {
            String id = CryptoUtils.bytesToHex(group.id());
            for (ProofV4 proof : group.proofs()) {
                TokenEntity entity = new TokenEntity();
                entity.setMint(mint);
                entity.setUnit(unit);
                entity.setMemo(memo);
                entity.setKeysetId(id);
                entity.setAmount(proof.amount().longValue());
                entity.setSecret(proof.secret());
                entity.setC(CryptoUtils.bytesToHex(proof.C()));
                tokenRepository.save(entity);
            }
        }
    }

    @Override
    public String sendEcash(String amountStr) throws Exception {
        long targetAmount = Long.parseLong(amountStr);
        String unit = "sat"; // Default

        // Find mint with enough funds
        Map<String, List<TokenEntity>> tokensByMint = new HashMap<>();
        tokenRepository.findAll().forEach(t -> {
            if (unit.equals(t.getUnit())) {
                tokensByMint.computeIfAbsent(t.getMint(), k -> new ArrayList<>()).add(t);
            }
        });

        String selectedMint = null;
        List<TokenEntity> inputs = new ArrayList<>();
        long currentSum = 0;

        for (Map.Entry<String, List<TokenEntity>> entry : tokensByMint.entrySet()) {
            long sum = entry.getValue().stream().mapToLong(TokenEntity::getAmount).sum();
            if (sum >= targetAmount) {
                selectedMint = entry.getKey();
                // Select tokens
                for (TokenEntity t : entry.getValue()) {
                    inputs.add(t);
                    currentSum += t.getAmount();
                    if (currentSum >= targetAmount) break;
                }
                break;
            }
        }

        if (selectedMint == null) {
            // For testing purposes, if no tokens, try to mint some first (as per original code logic, or just fail)
            // But strict "send" should fail.
            // However, the original code had "mintTokens" inside sendEcash, which was weird.
            // I will assume we must have funds.
            throw new Exception("Insufficient funds");
        }

        MintFacade mintFacade = new MintFacade(selectedMint);
        KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit(unit);
        String id = activeKeyset.id();
        KeysetKeys keys = mintFacade.getKeysetDetails(id);
        BlindedMessageService blindedMessageService = new BlindedMessageService();

        // 1. Inputs to Proofs
        List<Proof> inputProofs = new ArrayList<>();
        inputs.forEach(t -> inputProofs.add(new Proof(
                String.valueOf(t.getAmount()), t.getC(), t.getKeysetId(), t.getSecret()
        )));

        // 2. Prepare outputs (Target Amount + Change)
        long changeAmount = currentSum - targetAmount;
        
        List<BlindingInfo> outputBlindingInfos = new ArrayList<>();
        
        // Generate Blinding Info for Target
        outputBlindingInfos.addAll(blindedMessageService.generateBlindedMessagesForAmount(targetAmount, id));
        int targetCount = outputBlindingInfos.size();

        // Generate Blinding Info for Change
        if (changeAmount > 0) {
            outputBlindingInfos.addAll(blindedMessageService.generateBlindedMessagesForAmount(changeAmount, id));
        }

        // 3. Perform Swap
        SwapTokensRequest swapRequest = new SwapTokensRequest(
                inputProofs,
                ObjectConverts.convertListBlindingInfo(outputBlindingInfos)
        );

        SwapResponse response = mintFacade.swapTokens(swapRequest);

        // 4. Unblind all
        TokenV4 allTokens = blindedMessageService.unblindMessagesToSend(
                keys, selectedMint, unit, null, response.signatures, outputBlindingInfos
        );

        // 5. Separate Target and Change
        List<ProofV4> allProofs = new ArrayList<>();
        allTokens.tokens().forEach(g -> allProofs.addAll(g.proofs()));

        List<ProofV4> targetProofs = new ArrayList<>();
        List<ProofV4> changeProofs = new ArrayList<>();

        if (allProofs.size() != outputBlindingInfos.size()) {
            throw new Exception("Mismatch in number of signatures received");
        }

        for (int i = 0; i < allProofs.size(); i++) {
            if (i < targetCount) {
                targetProofs.add(allProofs.get(i));
            } else {
                changeProofs.add(allProofs.get(i));
            }
        }

        // 6. Save Change
        if (!changeProofs.isEmpty()) {
            TokenV4 changeToken = new TokenV4(selectedMint, unit, null, List.of(new ProofV4Group(CryptoUtils.hexToBytes(id), changeProofs)));
            saveTokenV4(changeToken);
        }

        // 7. Delete Inputs
        tokenRepository.deleteAll(inputs);

        // 8. Return Target Token
        TokenV4 targetToken = new TokenV4(selectedMint, unit, null, List.of(new ProofV4Group(CryptoUtils.hexToBytes(id), targetProofs)));
        return TokenCodec.serializeV4(targetToken);
    }

    @Override
    public long getBalance(String mint) {
        // add up tokens in db
        long balance = 0;
        Iterable<TokenEntity> tokens = tokenRepository.findAll();
        for (TokenEntity token : tokens) {
            if (mint == null || mint.isEmpty() || token.getMint().equals(mint)) {
                balance += token.getAmount();
            }
        }
        System.out.println("Balance: " + balance);
        return balance;
    }

    @Override
    public void addMint(String mintUrl) {
        // Verify mint exists by fetching info
        try {
             MintFacade mintFacade = new MintFacade(mintUrl);
             // Just checking if we can get keysets, which implies connectivity
             mintFacade.getKeysetSummaryForUnit("sat");
             System.out.println("Mint verified: " + mintUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to mint: " + mintUrl, e);
        }
    }

    public void getMints() {
        // add mint to mint map and db
    }
}
