package com.hobart.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hobart.lottery.entity.PredictionAccuracy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预测准确率统计Mapper
 */
@Mapper
public interface PredictionAccuracyMapper extends BaseMapper<PredictionAccuracy> {

    /**
     * 根据预测方法查询
     */
    @Select("SELECT * FROM prediction_accuracy WHERE predict_method = #{method}")
    PredictionAccuracy selectByMethod(String method);

    /**
     * 获取所有准确率统计
     */
    @Select("SELECT * FROM prediction_accuracy ORDER BY front_avg_hit DESC")
    List<PredictionAccuracy> selectAllStats();
}
