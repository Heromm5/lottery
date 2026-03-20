package com.hobart.lottery.service.prediction;

import com.hobart.lottery.domain.model.PredictionMethod;
import com.hobart.lottery.entity.PredictionRecord;

/**
 * 预测方法在 API/列表中的展示名（含定胆后缀）
 */
public final class PredictionDisplayNames {

    private PredictionDisplayNames() {
    }

    public static String forMethodAndMode(String predictMethod, String generationMode) {
        String base = PredictionMethod.getDisplayName(predictMethod);
        if (generationMode != null && PredictionRecord.GENERATION_MODE_PINNED.equals(generationMode)) {
            return base + "（定胆）";
        }
        return base;
    }
}
