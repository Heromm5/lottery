package com.hobart.lottery.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预测方法枚举
 * 所有预测方法统一注册，便于扩展和前端适配
 */
public enum PredictionMethod {
    
    HOT("HOT", "热号优先", "基于近期出现频率较高的号码进行预测"),
    MISSING("MISSING", "遗漏回补", "根据号码的历史遗漏值，预测回补概率高的号码"),
    BALANCED("BALANCED", "冷热均衡", "综合考虑热号和冷号，追求预测结果的均衡性"),
    ML("ML", "机器学习", "使用机器学习算法分析历史数据，挖掘潜在规律"),
    ADAPTIVE("ADAPTIVE", "自适应预测", "根据当前数据特征自动选择最佳预测策略"),
    BAYESIAN("BAYESIAN", "贝叶斯预测", "基于贝叶斯概率论进行号码预测"),
    MARKOV("MARKOV", "马尔可夫预测", "使用马尔可夫链模型预测号码转移概率"),
    MONTECARLO("MONTECARLO", "蒙特卡洛预测", "基于蒙特卡洛模拟进行随机抽样预测"),
    GRADIENT_BOOST("GRADIENT_BOOST", "梯度提升预测", "使用梯度提升决策树进行预测"),
    ENSEMBLE("ENSEMBLE", "集成预测", "综合多种预测方法的结果进行集成投票");

    /**
     * 方法代码（存入数据库）
     */
    private final String code;

    /**
     * 方法显示名称
     */
    private final String displayName;

    /**
     * 方法描述
     */
    private final String description;

    /**
     * 所有方法的缓存
     */
    private static final Map<String, PredictionMethod> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(PredictionMethod::getCode, m -> m));

    /**
     * 所有方法的列表
     */
    private static final List<PredictionMethod> ALL_METHODS = Arrays.asList(values());

    PredictionMethod(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static PredictionMethod fromCode(String code) {
        return CODE_MAP.getOrDefault(code, null);
    }

    /**
     * 根据代码获取显示名称
     */
    public static String getDisplayName(String code) {
        PredictionMethod method = fromCode(code);
        return method != null ? method.getDisplayName() : code;
    }

    /**
     * 获取所有预测方法代码
     */
    public static List<String> getAllCodes() {
        return ALL_METHODS.stream()
                .map(PredictionMethod::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有预测方法
     */
    public static List<PredictionMethod> getAllMethods() {
        return ALL_METHODS;
    }

    /**
     * 判断是否为有效的预测方法
     */
    public static boolean isValidMethod(String code) {
        return CODE_MAP.containsKey(code);
    }
}
