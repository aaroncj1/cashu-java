package io.github.aaroncj1.cashu.core.mint;

import lombok.Getter;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public class Keys {

    private final String[] keys;
    @Getter
    private final String keysetId;

    public Keys(String[] keys, String keysetId) {
        this.keys = keys;
        this.keysetId = keysetId;
    }

    public Keys(Map<String, String> keys, String keysetId) {
        this.keys = convertMapToStringArray(keys);
        this.keysetId = keysetId;
    }

    public String[] convertMapToStringArray(Map<String, String> keys) {
        String[] keyArray = new String[64];
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            int index = (int) (Math.log(Double.parseDouble(entry.getKey())) / Math.log(2));
            keyArray[index] = entry.getValue();
        }
        return keyArray;

    }

    public Map<BigInteger, String> getKeysetMap() {
        Map<BigInteger, String> keyMap = new LinkedHashMap<>();
        for (int i = 0; i < 64; i++)
            keyMap.put(BigInteger.valueOf(2).pow(i), keys[i]);

        return keyMap;
    }

    public Map<String, String> getKeysetStringMap() {
        Map<String, String> keyMap = new LinkedHashMap<>();
        for (int i = 0; i < 64; i++)
            keyMap.put((BigInteger.valueOf(2).pow(i)).toString(), keys[i]);

        return keyMap;
    }

    public String getKeyForAmount(BigInteger amount) {
        if (amount == null) {
            throw new NullPointerException("big is null");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
        // big.bitLength() = floor(log2(big)) + 1
        int bitLength = amount.bitLength();
        if (!amount.testBit(bitLength - 1) || amount.bitCount() != 1) {
            throw new IllegalArgumentException(amount + " is not a power of two");
        }
        // exponent = bitLength - 1
        int index = bitLength - 1;
        return keys[index];
    }
}
