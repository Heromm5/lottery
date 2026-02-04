package com.hobart.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hobart.lottery.entity.PredictionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预测记录Mapper
 */
@Mapper
public interface PredictionRecordMapper extends BaseMapper<PredictionRecord> {

    /**
     * 查询某期号的未验证预测记录
     */
    @Select("SELECT * FROM prediction_records WHERE target_issue = #{targetIssue} AND is_verified = 0")
    List<PredictionRecord> selectUnverifiedByIssue(String targetIssue);

    /**
     * 查询某期号的所有预测记录
     */
    @Select("SELECT * FROM prediction_records WHERE target_issue = #{targetIssue} ORDER BY created_at DESC")
    List<PredictionRecord> selectByTargetIssue(String targetIssue);

    /**
     * 查询某预测方法的所有已验证记录
     */
    @Select("SELECT * FROM prediction_records WHERE predict_method = #{method} AND is_verified = 1")
    List<PredictionRecord> selectVerifiedByMethod(String method);

    /**
     * 获取最近的预测记录
     */
    @Select("SELECT * FROM prediction_records ORDER BY created_at DESC LIMIT #{limit}")
    List<PredictionRecord> selectRecentRecords(int limit);

    /**
     * 统计某方法的预测数量
     */
    @Select("SELECT COUNT(*) FROM prediction_records WHERE predict_method = #{method}")
    int countByMethod(String method);
}
