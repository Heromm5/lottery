package com.hobart.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hobart.lottery.entity.AnomalyAlertEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常告警Mapper
 */
@Mapper
public interface AnomalyAlertMapper extends BaseMapper<AnomalyAlertEntity> {
}