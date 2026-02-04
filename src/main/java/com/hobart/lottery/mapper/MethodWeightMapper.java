package com.hobart.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hobart.lottery.entity.MethodWeight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预测方法权重 Mapper
 */
@Mapper
public interface MethodWeightMapper extends BaseMapper<MethodWeight> {
    
    /**
     * 根据方法代码查询
     */
    @Select("SELECT * FROM prediction_method_weight WHERE method_code = #{methodCode}")
    MethodWeight selectByMethodCode(String methodCode);
    
    /**
     * 查询所有方法权重，按权重降序
     */
    @Select("SELECT * FROM prediction_method_weight ORDER BY weight DESC")
    List<MethodWeight> selectAllOrderByWeight();
}
