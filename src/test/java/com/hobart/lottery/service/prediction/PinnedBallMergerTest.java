package com.hobart.lottery.service.prediction;

import com.hobart.lottery.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PinnedBallMergerTest {

    private final Random random = new Random(42);

    @Test
    void validate_rejectsBothEmpty() {
        assertThrows(BusinessException.class, () -> PinnedBallMerger.validate(Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void validate_rejectsTooManyFront() {
        assertThrows(BusinessException.class,
                () -> PinnedBallMerger.validate(Arrays.asList(1, 2, 3, 4, 5, 6), Collections.emptyList()));
    }

    @Test
    void validate_rejectsDuplicate() {
        assertThrows(BusinessException.class,
                () -> PinnedBallMerger.validate(Arrays.asList(1, 1), Collections.emptyList()));
    }

    @Test
    void validate_rejectsOutOfRange() {
        assertThrows(BusinessException.class,
                () -> PinnedBallMerger.validate(Collections.singletonList(36), Collections.emptyList()));
        assertThrows(BusinessException.class,
                () -> PinnedBallMerger.validate(Collections.emptyList(), Collections.singletonList(0)));
    }

    @Test
    void merge_onlyFrontPins_containsAllPins() {
        Set<Integer> pins = new HashSet<>(Arrays.asList(3, 7));
        int[][] out = PinnedBallMerger.merge(
                new int[]{1, 2, 4, 5, 6},
                new int[]{5, 6},
                pins,
                Collections.emptySet(),
                random);
        assertEquals(5, out[0].length);
        assertEquals(2, out[1].length);
        for (int p : pins) {
            assertTrue(contains(out[0], p), "front should contain " + p);
        }
    }

    @Test
    void merge_onlyBackPins_containsAllBackPins() {
        Set<Integer> bPins = Collections.singleton(11);
        int[][] out = PinnedBallMerger.merge(
                new int[]{10, 11, 12, 13, 14},
                new int[]{3, 4},
                Collections.emptySet(),
                bPins,
                random);
        assertTrue(contains(out[1], 11));
    }

    @Test
    void merge_fullPins_fixed() {
        int[] f = {1, 2, 3, 4, 5};
        int[] b = {6, 7};
        int[][] out = PinnedBallMerger.merge(
                new int[]{10, 11, 12, 13, 14},
                new int[]{1, 2},
                new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)),
                new HashSet<>(Arrays.asList(6, 7)),
                random);
        assertArrayEquals(f, out[0]);
        assertArrayEquals(b, out[1]);
    }

    @Test
    void toLockedCsv_nullWhenEmpty() {
        assertNull(PinnedBallMerger.toLockedCsv(Collections.emptyList()));
        assertNull(PinnedBallMerger.toLockedCsv(null));
    }

    private static boolean contains(int[] arr, int v) {
        for (int x : arr) {
            if (x == v) {
                return true;
            }
        }
        return false;
    }
}
