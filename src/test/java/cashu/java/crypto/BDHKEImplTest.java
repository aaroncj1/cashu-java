//package cashu.java.crypto;
//
//import cashu.java.common.api.response.PRInvoice;
//import cashu.java.wallet.MintClientImpl;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.Map;
//
//class BDHKEImplTest {
////    BlindResult{blindedPublicKeyHex='048266ff76aafba553c66dc4275496bb9949abb097a67d23352e5189360ec4cd99594a1c901cf73afbaa868b1c90ce87f0f9e721e5208d11c15babff3d990898be', blindingFactorHex='e68072f77895b84beec38f7416f026f938b5ec91f7b565fe4f1dc245e32a0a96', secretHex='dfe46a43ad9644047c78255a6cd96d8144af102a18153377843015fc1ac7ce74'}
//
//
//    @Test
//    public void blinder() throws IOException, InterruptedException {
//        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
//        PRInvoice PRInvoice = mintClient.requestMintInvoice("1");
//        Map<String, String> keys = mintClient.getKeys("sat");
//        String pubkey = "025e8c3efd8207ca9c335ce72957138c267e8c5b70734b301c82a1b0c2fbd1f3e9";
////        String activeKey = mintClient.getActiveKey("1");
//
//        BDHKEImpl.BlindResult blindResult = BDHKEImpl.blind(pubkey);
//        System.out.println(blindResult);
//    }
//
//    @Test
//    public void unblinder() throws IOException, InterruptedException {
////        String activeKey = mintClient.getActiveKey("1");
//
////        ECBlind.BlindResult blindResult = ECBlind.blind(pubkey);
////        System.out.println(blindResult);
//        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
//        PRInvoice PRInvoice = mintClient.requestMintInvoice("1");
//        Map<String, String> keys = mintClient.getKeys("sat");
//        String pubkey = "025e8c3efd8207ca9c335ce72957138c267e8c5b70734b301c82a1b0c2fbd1f3e9";
//
//        String C_ = "024825313fc9041cafbc8296e53d1726be377671ef2c0a6764e9abb201b2ba4f94";
//        String r = "e68072f77895b84beec38f7416f026f938b5ec91f7b565fe4f1dc245e32a0a96";
//        String s = "dfe46a43ad9644047c78255a6cd96d8144af102a18153377843015fc1ac7ce74";
//
//
//        BDHKEImpl.UnblindResult unblindResult = BDHKEImpl.unblind(pubkey, s,r,C_);
//        System.out.println(unblindResult);
//    }
//
//
//}