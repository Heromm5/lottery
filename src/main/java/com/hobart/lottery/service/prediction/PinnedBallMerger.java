package com.hobart.lottery.service.prediction;

import com.hobart.lottery.common.exception.BusinessException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * 定胆：校验胆码并将算法原始 5+2 与胆码合并为合法投注（胆码必含于结果中）。
 */
public final class PinnedBallMerger {

    private static final int FRONT_MIN = 1;
    private static final int FRONT_MAX = 35;
    private static final int FRONT_SIZE = 5;
    private static final int BACK_MIN = 1;
    private static final int BACK_MAX = 12;
    private static final int BACK_SIZE = 2;

    private PinnedBallMerger() {
    }

    /**
     * 校验胆码列表：至少一侧非空；前区 ≤5、后区 ≤2；范围与去重。
     */
    public static void validate(List<Integer> lockedFront, List<Integer> lockedBack) {
        List<Integer> front = lockedFront == null ? Collections.emptyList() : lockedFront;
        List<Integer> back = lockedBack == null ? Collections.emptyList() : lockedBack;

        if (front.isEmpty() && back.isEmpty()) {
            throw new BusinessException("请至少选择一枚前区或后区胆码");
        }
        validateZone(front, FRONT_MIN, FRONT_MAX, FRONT_SIZE, "前区胆码");
        validateZone(back, BACK_MIN, BACK_MAX, BACK_SIZE, "后区胆码");
    }

    private static void validateZone(List<Integer> nums, int min, int max, int maxCount, String label) {
        if (nums.size() > maxCount) {
            throw new BusinessException(label + "最多选择 " + maxCount + " 个");
        }
        Set<Integer> seen = new HashSet<>();
        for (Integer n : nums) {
            if (n == null) {
                throw new BusinessException(label + "不能包含空值");
            }
            if (n < min || n > max) {
                throw new BusinessException(label + "必须在 " + min + "～" + max + " 之间");
            }
            if (!seen.add(n)) {
                throw new BusinessException(label + "不能重复");
            }
        }
    }

    /**
     * 合并一注原始预测与胆码。
     *
     * @param rawFront 算法前区 5 个
     * @param rawBack  算法后区 2 个
     * @param frontPins 前区胆码（可空集合表示无）
     * @param backPins  后区胆码（可空集合表示无）
     */
    public static int[][] merge(int[] rawFront, int[] rawBack,
                                Set<Integer> frontPins, Set<Integer> backPins,
                                Random random) {
        int[] front = mergeZone(rawFront, frontPins, FRONT_SIZE, FRONT_MIN, FRONT_MAX, random);
        int[] back = mergeZone(rawBack, backPins, BACK_SIZE, BACK_MIN, BACK_MAX, random);
        return new int[][]{front, back};
    }

    private static int[] mergeZone(int[] raw, Set<Integer> pins, int targetSize, int min, int max, Random random) {
        TreeSet<Integer> selected = new TreeSet<>();
        if (pins != null) {
            selected.addAll(pins);
        }
        if (selected.size() > targetSize) {
            throw new IllegalStateException("胆码数量超过该区位数");
        }
        if (raw != null) {
            for (int x : raw) {
                if (selected.size() >= targetSize) {
                    break;
                }
                if (x >= min && x <= max && !selected.contains(x)) {
                    selected.add(x);
                }
            }
        }
        while (selected.size() < targetSize) {
            int n = min + random.nextInt(max - min + 1);
            selected.add(n);
        }
        return selected.stream().mapToInt(Integer::intValue).toArray();
    }

    public static String toLockedCsv(List<Integer> nums) {
        if (nums == null || nums.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nums.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(nums.get(i));
        }
        return sb.toString();
    }

    public static Set<Integer> toPinSet(List<Integer> nums) {
        if (nums == null || nums.isEmpty()) {
            return Collections.emptySet();
        }
        return new TreeSet<>(nums);
    }
}
