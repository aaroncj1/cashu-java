package io.github.aaroncj1.cashu.core.mint;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeysTest {

    @Test
    void testMapToStringArray() {
        Map<String,String> keyMap = new HashMap<>();
        keyMap.put("1", "one");
        keyMap.put("2", "two");
        keyMap.put("4", "three");
        keyMap.put("8", "four");
        keyMap.put("16", "five");
        keyMap.put("32", "six");
        keyMap.put("64", "seven");
        keyMap.put("128", "eight");
        keyMap.put("256", "nine");
        // ...
        keyMap.put("576460752303423488", "big1");
        keyMap.put("1152921504606846976", "big2");
        keyMap.put("2305843009213693952", "big3");
        keyMap.put("4611686018427387904", "big4");
        keyMap.put("9223372036854775808", "big5");

        Keys keys = new Keys(keyMap, "testerTime");
        String[] keysArray = keys.convertMapToStringArray(keyMap);
        System.out.println(Arrays.toString(Arrays.stream(keysArray).toArray()));
        assertEquals(64, keysArray.length);
        assertEquals("testerTime", keys.getKeysetId());
    }
}