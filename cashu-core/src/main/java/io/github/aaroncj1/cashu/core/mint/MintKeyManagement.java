package io.github.aaroncj1.cashu.core.mint;

import java.math.BigInteger;

public interface MintKeyManagement {

    void lookupKeysetById(String id);

    void signForAmountById(BigInteger amoumt, String id);

    Keys constructKeysetByPath(String path);

    String calculateKeysetId(String[] keys);
}
