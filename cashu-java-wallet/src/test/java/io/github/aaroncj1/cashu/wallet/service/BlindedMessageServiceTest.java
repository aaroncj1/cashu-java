package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.core.model.BlindingInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlindedMessageServiceTest {

    @Test
    void testDenominationSpliting() {
        BlindedMessageService blindedMessageService = new BlindedMessageService();
        List<Long> expected10 = List.of(8L, 2L);
        assertEquals(expected10, blindedMessageService.splitAmountIntoDenominations(10L));
        List<Long> expected37 = List.of(32L, 4L, 1L);
        assertEquals(expected37, blindedMessageService.splitAmountIntoDenominations(37L));
    }

    @Test
    void testGenerateBlindedMessages() throws Exception {
        BlindedMessageService blindedMessageService = new BlindedMessageService();
        List<BlindingInfo> blindingInfoList = blindedMessageService.generateBlindedMessagesForAmount(11L, null);
        assertEquals(3, blindingInfoList.size());
        assertEquals(8, blindingInfoList.get(0).getAmount());
        assertEquals(2, blindingInfoList.get(1).getAmount());
        assertEquals(1, blindingInfoList.get(2).getAmount());
    }
}