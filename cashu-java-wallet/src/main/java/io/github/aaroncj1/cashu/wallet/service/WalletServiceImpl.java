package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.model.BlindedMessage;
import io.github.aaroncj1.cashu.core.model.BlindingInfo;
import io.github.aaroncj1.cashu.core.model.KeysetKeys;
import io.github.aaroncj1.cashu.core.model.MintProof;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.ExecuteMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.request.RequestMintQuoteRequest;
import io.github.aaroncj1.cashu.core.model.api.mint.v1.bolt11.response.ExecuteMintQuoteResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapResponse;
import io.github.aaroncj1.cashu.core.model.api.swap.v1.SwapTokensRequest;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenV3;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;
import io.github.aaroncj1.cashu.core.serialization.TokenCodec;
import io.github.aaroncj1.cashu.wallet.service.mappers.ObjectConverts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.aaroncj1.cashu.core.serialization.TokenCodec.V3_PREFIX;
import static io.github.aaroncj1.cashu.core.serialization.TokenCodec.V4_PREFIX;

public class WalletServiceImpl implements WalletService {
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
        Map<String, List<BlindingInfo>> replacementProofs = new HashMap<>();
        for (Map.Entry<String, List<MintProof>> entry : mintProofList.entrySet()) {
            MintFacade mintFacade = new MintFacade(entry.getKey());
            String newId = mintFacade.getKeysetSummaryForUnit(unit).id();
            List<BlindingInfo> newBlindingInfoList = new ArrayList<>();
            for (MintProof mintProof : entry.getValue()) {
                BlindingInfo blindingInfo = blindedMessageService.createProofToSwap(mintProof, newId);
                newBlindingInfoList.add(blindingInfo);
            }
            replacementProofs.put(entry.getKey(), newBlindingInfoList);
            SwapTokensRequest swapTokensRequest = new SwapTokensRequest(ObjectConverts.convertListMintProofs(entry.getValue()), ObjectConverts.convertListBlindingInfo(newBlindingInfoList));

            SwapResponse response = mintFacade.swapTokens(swapTokensRequest);
            System.out.println(response.toString());
        }

        // deserialize token
        // generates new proofs to replace the received proofs
        // swap tokens with mint
        // store in DB
        return false;
    }

    @Override
    public String sendEcash(String amount) throws Exception {
        MintFacade mintFacade = new MintFacade("https://testnut.cashu.space");
        String id = mintFacade.getKeysetSummaryForUnit("sat").id();
        KeysetKeys keys = mintFacade.getKeysetDetails(id);
        System.out.println(keys.toString());
        String quote = mintFacade.mintTokens(new RequestMintQuoteRequest("sat", amount)).quote();

        BlindedMessageService blindedMessageService = new BlindedMessageService();
        List<BlindingInfo> blindingInfoList = blindedMessageService.generateBlindedMessagesForAmount(Long.valueOf(amount), id);

        List<BlindedMessage> blindedMessages = ObjectConverts.convertListBlindingInfo(blindingInfoList);
        ExecuteMintQuoteRequest request = new ExecuteMintQuoteRequest(quote, blindedMessages);
        ExecuteMintQuoteResponse response = mintFacade.executeMintTokens(request);
        System.out.println(response.signatures.get(0));
        TokenV4 token = blindedMessageService.unblindMessagesToSend(keys, mintFacade.getMintUrl(), "sat", null, response.signatures, blindingInfoList);

        return TokenCodec.serializeV4(token);



        // select tokens from db
        // make change with mint if needed
        // serialize and return
    }

    @Override
    public void getBalance(String mint) {
        // add up tokens in db

    }

    @Override
    public void addMint(String mintUrl) {
        // add mint to mint map and db

    }

    public void getMints() {
        // add mint to mint map and db

    }
}
