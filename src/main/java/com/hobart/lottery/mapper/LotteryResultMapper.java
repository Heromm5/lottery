package com.hobart.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hobart.lottery.entity.LotteryResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 大乐透开奖结果Mapper
 */
@Mapper
public interface LotteryResultMapper extends BaseMapper<LotteryResult> {

    /**
     * 获取最近N期开奖结果
     */
    @Select("SELECT * FROM lottery_results ORDER BY draw_date DESC, issue DESC LIMIT #{limit}")
    List<LotteryResult> selectRecentResults(int limit);

    /**
     * 获取最新一期开奖结果
     */
    @Select("SELECT * FROM lottery_results ORDER BY draw_date DESC, issue DESC LIMIT 1")
    LotteryResult selectLatestResult();

    /**
     * 根据期号查询
     */
    @Select("SELECT * FROM lottery_results WHERE issue = #{issue}")
    LotteryResult selectByIssue(String issue);
}
