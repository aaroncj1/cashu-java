//package cashu.java.crypto;
//
//import cashu.java.common.api.response.PRInvoice;
//import cashu.java.wallet.MintClientImpl;
//import org.junit.jupiter.api.Test;
//
//import java.util.Map;
//
//class CashuSwapTest {
////2 ::: BlindInfo{secretXHex='12344fade', blindingFactorHex='fc9f170e3b485f718c0fe1811d08baa8f7589947fbef57f90ec37db7233abc2c', blindedPublicKeyHex='0233e2ed8b01554fe2162eeac3337744811061270e1754fa3ce007eca71209ee9b'}
////1 ::: BlindInfo{secretXHex='38387832792', blindingFactorHex='dcfe0b94e72c67cd1d24f52a5187d6a69222466481207ad555fc339f8c2dbaf8', blindedPublicKeyHex='02041dcc419f97240d3fc13979c731cf1c8e0756897b5f7f0fb0fee4061f68cef5'}
//    @Test
//    void blind() throws Exception {
//        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
//        PRInvoice PRInvoice = mintClient.requestMintInvoice("2");
//        Map<String, String> keys = mintClient.getKeys("sat");
//        String activeKey = mintClient.getActiveKey("sat");
//
//        CashuSwap.BlindInfo blindResult = CashuSwap.createBlindInfo(keys.get("2"), "12344fade");
//        System.out.println("2 ::: " + blindResult);
//
//        CashuSwap.BlindInfo blindResult1 = CashuSwap.createBlindInfo(keys.get("1"), "38387832792");
//        System.out.println("1 ::: " + blindResult1);
//    }
//
//    @Test
//    public void unblinder() throws Exception {
////        String activeKey = mintClient.getActiveKey("1");
//
////        ECBlind.BlindResult blindResult = ECBlind.blind(pubkey);
////        System.out.println(blindResult);
//        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
//        PRInvoice PRInvoice = mintClient.requestMintInvoice("1");
//        Map<String, String> keys = mintClient.getKeys("sat");
//        String pubkey = keys.get("2");
//
//        String C_ = "020839e3517ef286d7f34d764f940cb2e518acae1c2b30f4c99dae82d0b0a077d6";
//        String r = "fc9f170e3b485f718c0fe1811d08baa8f7589947fbef57f90ec37db7233abc2c";
//        String s = "12344fade";
//
//        CashuSwap.Token unblindResult = CashuSwap.unblind(pubkey, new CashuSwap.BlindInfo(s, r, ""), C_);
//        System.out.println(unblindResult);
//    }
//
//}