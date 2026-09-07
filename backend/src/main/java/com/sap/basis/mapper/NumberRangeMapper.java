package com.sap.basis.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sap.basis.entity.NumberRange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NumberRangeMapper extends BaseMapper<NumberRange> {
    @Update("UPDATE sap_number_range SET current_no = current_no + 1 WHERE object_name = #{object}")
    int increment(String object);

    @Select("SELECT current_no FROM sap_number_range WHERE object_name = #{object}")
    Integer currentNo(String object);
}
