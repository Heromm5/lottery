package com.hobart.lottery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VerificationService - 中奖等级判定")
class VerificationServiceTest {

    @ParameterizedTest(name = "前{0}+后{1} = {2}")
    @CsvSource({
        "5, 2, 一等奖",
        "5, 1, 二等奖",
        "5, 0, 三等奖",
        "4, 2, 三等奖",
        "4, 1, 四等奖",
        "4, 0, 五等奖",
        "3, 2, 五等奖",
        "3, 1, 六等奖",
        "2, 2, 六等奖",
        "3, 0, 七等奖",
        "2, 1, 七等奖",
        "1, 2, 七等奖",
        "0, 2, 七等奖",
        "2, 0, 未中奖",
        "1, 1, 未中奖",
        "1, 0, 未中奖",
        "0, 1, 未中奖",
        "0, 0, 未中奖"
    })
    void determinePrizeLevel(int frontHit, int backHit, String expected) {
        assertEquals(expected, VerificationService.determinePrizeLevel(frontHit, backHit));
    }

    @Test
    @DisplayName("边界值：全中应为一等奖")
    void allHitShouldBeFirstPrize() {
        assertEquals("一等奖", VerificationService.determinePrizeLevel(5, 2));
    }

    @Test
    @DisplayName("边界值：全不中应为未中奖")
    void noHitShouldBeNoPrize() {
        assertEquals("未中奖", VerificationService.determinePrizeLevel(0, 0));
    }
}
