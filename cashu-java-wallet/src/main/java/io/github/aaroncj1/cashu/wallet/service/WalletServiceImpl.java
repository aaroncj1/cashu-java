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

    private final Map<String, List<BlindingInfo>> pendingMintBlindingInfos = new HashMap<>();

    @Override
    public String requestMint(String amount, String mintUrl) throws Exception {
        MintFacade mintFacade = new MintFacade(mintUrl);
        RequestMintQuoteResponse response = mintFacade.mintTokens(new RequestMintQuoteRequest("sat", amount));
        // We should store the blinding info for later use when minting
        // For simplicity in this demo, we'll generate it now and store it in memory associated with quoteId
        // In a real app, persist this.
        
        KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit("sat");
        String id = activeKeyset.id();
        List<BlindingInfo> blindingInfos = new BlindedMessageService().generateBlindedMessagesForAmount(Long.valueOf(amount), id);
        pendingMintBlindingInfos.put(response.quote(), blindingInfos);
        
        return response.quote() + "|" + response.request();
    }

    @Override
    public void mintTokens(String quoteId, String amount, String mintUrl) throws Exception {
        MintFacade mintFacade = new MintFacade(mintUrl);
        List<BlindingInfo> blindingInfos = pendingMintBlindingInfos.get(quoteId);
        
        if (blindingInfos == null) {
            // Regenerate if lost (should persist in real app)
             KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit("sat");
             String id = activeKeyset.id();
             blindingInfos = new BlindedMessageService().generateBlindedMessagesForAmount(Long.valueOf(amount), id);
        }

        ExecuteMintQuoteRequest request = new ExecuteMintQuoteRequest(quoteId, ObjectConverts.convertListBlindingInfo(blindingInfos));
        ExecuteMintQuoteResponse response = mintFacade.executeMintTokens(request);
        
        KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit("sat");
        String id = activeKeyset.id();
        KeysetKeys keys = mintFacade.getKeysetDetails(id);
        
        TokenV4 token = new BlindedMessageService().unblindMessagesToSend(keys, mintUrl, "sat", null, response.signatures, blindingInfos);
        saveTokenV4(token);
        pendingMintBlindingInfos.remove(quoteId);
    }

    @Override
    public String requestMelt(String invoice, String mintUrl) throws Exception {
        MintFacade mintFacade = new MintFacade(mintUrl);
        RequestMeltQuoteResponse response = mintFacade.requestMeltQuote(new RequestMeltQuoteRequest("sat", invoice));
        return response.quote();
    }

    @Override
    public void meltTokens(String quoteId, String mintUrl) throws Exception {
        // We need to fetch the quote amount first to know how much to melt
        // But for this interface we assumed just quoteId. 
        // We actually need to pay, so we need to select tokens.
        // Let's assume we fetch the quote again or the user provides the amount? 
        // The MintHttpClient.meltState could tell us the amount but we don't have it exposed nicely.
        // Let's change the flow: The user usually sees the fee and amount.
        // For now, let's look up the quote on the mint to get amount + fee
        
        MintFacade mintFacade = new MintFacade(mintUrl);
        RequestMeltQuoteResponse quote = mintFacade.meltState(quoteId);
        
        if (quote.paid()) {
            throw new Exception("Quote already paid");
        }
        
        long totalAmount = Long.parseLong(quote.amount()) + Long.parseLong(quote.fee_reserve());
        
        // Select tokens
        List<TokenEntity> inputs = new ArrayList<>();
        long currentSum = 0;
        Iterable<TokenEntity> allTokens = tokenRepository.findAll();
        for (TokenEntity t : allTokens) {
            if (t.getMint().equals(mintUrl) && "sat".equals(t.getUnit())) {
                inputs.add(t);
                currentSum += t.getAmount();
                if (currentSum >= totalAmount) break;
            }
        }
        
        if (currentSum < totalAmount) {
            throw new Exception("Insufficient funds");
        }
        
        // Prepare Swap for exact amount (target + fee)
        KeysetSummary activeKeyset = mintFacade.getKeysetSummaryForUnit("sat");
        String id = activeKeyset.id();
        KeysetKeys keys = mintFacade.getKeysetDetails(id);
        BlindedMessageService blindedMessageService = new BlindedMessageService();

        List<Proof> inputProofs = new ArrayList<>();
        inputs.forEach(t -> inputProofs.add(new Proof(
                String.valueOf(t.getAmount()), t.getC(), t.getKeysetId(), t.getSecret()
        )));

        // Outputs: Change only. The target amount is burnt by melt.
        // Wait, melt expects inputs.
        // If we have exact change, great. If not, we need to swap first to get exact proofs?
        // Cashu Melt (Nut-05) accepts inputs. The mint handles the burn.
        // But if inputs > amount + fee, the mint returns change?
        // NUT-05: PostMeltRequest has 'inputs'.
        // "If the inputs are greater than amount + fee, the mint SHOULD return change."
        // We need to provide blinded messages for the change.
        
        long changeAmount = currentSum - totalAmount;
        List<BlindingInfo> changeBlindingInfos = new ArrayList<>();
        if (changeAmount > 0) {
            changeBlindingInfos.addAll(blindedMessageService.generateBlindedMessagesForAmount(changeAmount, id));
        }
        
        ExecuteMeltQuoteRequest request = new ExecuteMeltQuoteRequest(
                quoteId, 
                inputProofs, 
                changeAmount > 0 ? ObjectConverts.convertListBlindingInfo(changeBlindingInfos) : null
        );
        
        ExecuteMeltQuoteResponse response = mintFacade.executeMeltTokens(request);
        
        if (response.paid()) {
            // Delete spent inputs
            tokenRepository.deleteAll(inputs);
            
            // Handle change
            if (response.change() != null && !response.change().isEmpty()) {
                TokenV4 changeToken = blindedMessageService.unblindMessagesToSend(
                        keys, mintUrl, "sat", null, response.change(), changeBlindingInfos
                );
                saveTokenV4(changeToken);
            }
        } else {
             throw new Exception("Payment failed");
        }
    }
