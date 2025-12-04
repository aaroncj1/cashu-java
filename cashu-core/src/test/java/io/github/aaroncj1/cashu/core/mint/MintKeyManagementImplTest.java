package io.github.aaroncj1.cashu.core.mint;

import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.jupiter.api.Test;

class MintKeyManagementImplTest {

    @Test
    void constructKeysetByPath() throws UnreadableWalletException {
        MintKeyManagementImpl mintKeyManagement = new MintKeyManagementImpl("city teach inspire wrap trim hello initial receive name genuine man retreat", "m/44H/0H/0H/0/1");
        Keys keys = mintKeyManagement.constructKeysetByPath("m/44H/0H/0H/0/1");
        System.out.println(keys.getKeysetMap());
        System.out.println(keys.getKeysetId());
    }

    //{
    //  "1": "03a40f20667ed53513075dc51e715ff2046cad64eb68960632269ba7f0210e38bc",
    //  "2": "03fd4ce5a16b65576145949e6f99f445f8249fee17c606b688b504a849cdc452de",
    //  "4": "02648eccfa4c026960966276fa5a4cae46ce0fd432211a4f449bf84f13aa5f8303",
    //  "8": "02fdfd6796bfeac490cbee12f778f867f0a2c68f6508d17c649759ea0dc3547528"
    //}
    @Test
    void calculateKeysetId() throws UnreadableWalletException {
        MintKeyManagementImpl mintKeyManagement = new MintKeyManagementImpl("city teach inspire wrap trim hello initial receive name genuine man retreat", "m/44H/0H/0H/0/1");
        String[] keys = new String[4];
        keys[0] = "03a40f20667ed53513075dc51e715ff2046cad64eb68960632269ba7f0210e38bc";
        keys[1] = "03fd4ce5a16b65576145949e6f99f445f8249fee17c606b688b504a849cdc452de";
        keys[2] = "02648eccfa4c026960966276fa5a4cae46ce0fd432211a4f449bf84f13aa5f8303";
        keys[3] = "02fdfd6796bfeac490cbee12f778f867f0a2c68f6508d17c649759ea0dc3547528";
        String keysetId = mintKeyManagement.calculateKeysetId(keys);
        System.out.println(keysetId);
    }

}