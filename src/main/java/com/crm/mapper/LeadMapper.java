package com.crm.mapper;

import com.crm.entity.Lead;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;

/**
 * <p>
 * @since 2025-10-12
 */
public interface LeadMapper extends BaseMapper<Lead> {
    int countByCreateDate(@Param("date") LocalDate date);
}