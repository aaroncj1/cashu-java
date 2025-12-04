package cashu.java.wallet;

import cashu.java.common.BlindedMessage;
import cashu.java.common.Proof;
import cashu.java.common.api.response.MintTokensResponse;
import cashu.java.common.api.response.PRInvoice;
import cashu.java.crypto.BDHKEServiceSecp256k1Impl;
import cashu.java.crypto.BlindingInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class MintClientImplTest {

    @Test
    public void getKeysets() throws IOException, InterruptedException {
        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
        Assertions.assertNotNull(mintClient.getActiveKey("sat"));
    }

    @Test
    public void getInvoice() throws IOException, InterruptedException {
        MintClient mintClient = new MintClientImpl("https://testnut.cashu.space");
        mintClient.requestMintInvoice("1000");
    }

    @Test
    public void checkInvoiceStatus() throws IOException, InterruptedException {
        MintClient mintClient = new MintClientImpl("https://testnut.cashu.space");
        PRInvoice PRInvoice = mintClient.requestMintInvoice("1000");
        System.out.println(PRInvoice.hash);
        Assertions.assertTrue(mintClient.checkInvoicePaid(PRInvoice.hash));
    }

    @Test
    public void getKeys() throws IOException, InterruptedException {
        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
        Map<String, String> keys = mintClient.getKeys("sat");
        System.out.println(keys.get("1"));
        Assertions.assertFalse(keys.isEmpty());
    }

    @Test
    public void claimToken() throws Exception {
        String amount = "1";
        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
        PRInvoice PRInvoice = mintClient.requestMintInvoice(amount);
        Map<String, String> keys = mintClient.getKeys("sat");
        String activeKey = mintClient.getActiveKey("sat");
        String pubkey = keys.get(amount);

        System.out.println(PRInvoice.hash);
        Assertions.assertTrue(mintClient.checkInvoicePaid(PRInvoice.hash));

        BDHKEServiceSecp256k1Impl service = new BDHKEServiceSecp256k1Impl();
        BlindingInfo blindingInfo = service.wallet_blind(pubkey);
        System.out.println(blindingInfo);
        BlindedMessage blindedMessage = new BlindedMessage(amount, activeKey, blindingInfo.blindedPublicKeyHex());
        MintTokensResponse response = mintClient.claimTokens(List.of(blindedMessage), PRInvoice.hash);
        System.out.println(response.signatures.get(0));
    }

    @Test
    public void swapToken() throws Exception {
        String amount = "2";
        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
        PRInvoice PRInvoice = mintClient.requestMintInvoice(amount);
        Map<String, String> keys = mintClient.getKeys("sat");
        String activeKey = mintClient.getActiveKey("sat");
        String pubkey = keys.get(amount);

        System.out.println(PRInvoice.hash);
        Assertions.assertTrue(mintClient.checkInvoicePaid(PRInvoice.hash));

        BDHKEServiceSecp256k1Impl service = new BDHKEServiceSecp256k1Impl();
        BlindingInfo blindingInfo = service.wallet_blind(pubkey);
        System.out.println(blindingInfo);
        BlindedMessage blindedMessage = new BlindedMessage(amount, activeKey, blindingInfo.blindedPublicKeyHex());
        MintTokensResponse response = mintClient.claimTokens(List.of(blindedMessage), PRInvoice.hash);
        System.out.println(response.signatures.get(0));

        String C = service.wallet_unblind(pubkey, blindingInfo.blindingFactorHex(), response.signatures.get(0).C_());
        Proof proof = new Proof(amount, C, activeKey, blindingInfo.secretXHex());

        String amount2 = "1";
        BlindingInfo blindingInfo2 = service.wallet_blind(keys.get(amount2));
        BlindedMessage blindedMessage2 = new BlindedMessage(amount2, activeKey, blindingInfo2.blindedPublicKeyHex());

        MintTokensResponse response2 = mintClient.swapTokens(List.of(proof), List.of(blindedMessage2));
        System.out.println(response2);
    }
}