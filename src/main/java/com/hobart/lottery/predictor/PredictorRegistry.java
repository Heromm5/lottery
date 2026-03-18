package com.hobart.lottery.predictor;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PredictorRegistry {
    private final Map<String, BasePredictor> predictors;

    public PredictorRegistry(List<BasePredictor> allPredictors) {
        this.predictors = allPredictors.stream()
            .collect(Collectors.toMap(BasePredictor::getMethodCode, p -> p));
    }

    public BasePredictor get(String code) {
        return predictors.get(code);
    }

    public List<BasePredictor> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(predictors.values()));
    }

    public boolean contains(String code) {
        return predictors.containsKey(code);
    }
}
