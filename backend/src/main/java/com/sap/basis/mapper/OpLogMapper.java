package com.sap.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sap.basis.entity.OpLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpLogMapper extends BaseMapper<OpLog> {
}
