//package cashu.java.crypto;
//
//import cashu.java.common.api.response.PRInvoice;
//import cashu.java.wallet.MintClientImpl;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.Map;
//
//class ECBlindTest {
//
//    @Test
//    public void blinder() throws IOException, InterruptedException {
//        MintClientImpl mintClient = new MintClientImpl("https://testnut.cashu.space");
//        PRInvoice PRInvoice = mintClient.requestMintInvoice("1");
//        Map<String, String> keys = mintClient.getKeys("sat");
//        String pubkey = keys.get("1");
////        String activeKey = mintClient.getActiveKey("1");
//
//        ECBlind.BlindResult blindResult = ECBlind.blind(pubkey);
//        System.out.println(blindResult);
//        //BlindResult{blindedPublicKeyHex='0472d32312db213841657f007c9c4f70ac0374505e36c6973aed75abb76557e27c6242a67b02cc50ee688163b3f22e8f59c43dfe84075906d8d6506333ab588c9c', blindingFactorHex='55d10b6b7b7748db76e033b53ed91c211c1eada4f6c7ecdc9334b32f8fc38937', secretScalarHex='63045fc084f1d48cc08f18372b0d6a25afab3e3668e5af6ab13011ebad17b47e'}
//        // 03fb2e2651e58ecae06f0d87a966749c36c0a46e73ef886ca766da8f946b5f534c
////
////        ECBlind.UnblindResult unblindResult = ECBlind.unblind(blindResult.blindedPublicKeyHex, blindResult.blindingFactorHex);
////        System.out.println(unblindResult);
////
////        ECBlind.UnblindResult unblindResult2 = ECBlind.unblind("0220cb4349b2eeb1750da00e373e9c9fdd31a549802fbc0a5bafde31460ade341f", blindResult.blindingFactorHex);
////        System.out.println(unblindResult2);
////BlindResult{blindedPublicKeyHex='044581e4ca2d5b595b050257856d2d3fafbdeefb7ae1f5434f3591ad792a0e36fb7491289ce14d6952dcd3aa79adeb84730ee82d4f28977bc28871d7050d35ebd9', blindingFactorHex='cd639b99ccaa97b168d4b93de720d76cdb198b75ddeefe3c402e6bbe5110034f', secretScalarHex='f018e307c26b33d1945a5bda8a0bccbda752ac0a017b9d0756d1cc41cd8b8423'}
//    }
//
//    @Test
//    public void unblinder() throws IOException, InterruptedException {
////        String activeKey = mintClient.getActiveKey("1");
//
////        ECBlind.BlindResult blindResult = ECBlind.blind(pubkey);
////        System.out.println(blindResult);
//
//        ECBlind.UnblindResult unblindResult = ECBlind.unblind("03fb2e2651e58ecae06f0d87a966749c36c0a46e73ef886ca766da8f946b5f534c", "55d10b6b7b7748db76e033b53ed91c211c1eada4f6c7ecdc9334b32f8fc38937");
//        System.out.println(unblindResult);
//    }
//
//}