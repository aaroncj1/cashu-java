package cashu.java.wallet;

import cashu.java.common.BlindedMessage;
import cashu.java.common.Proof;
import cashu.java.common.api.response.MintTokensResponse;
import cashu.java.common.api.response.PRInvoice;

import java.io.IOException;
import java.util.List;

public interface MintClient {

    PRInvoice requestMintInvoice(String amount) throws IOException, InterruptedException;

    boolean checkInvoicePaid(String hash) throws IOException, InterruptedException;

    MintTokensResponse claimTokens(List<BlindedMessage> blindedMessages, String quote) throws IOException, InterruptedException;

    MintTokensResponse swapTokens(List<Proof> inputs, List<BlindedMessage> outputs) throws IOException, InterruptedException;

    void meltTokens();

    void refreshKeysets() throws IOException, InterruptedException;

}
