package com.sap.sd.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sap.sd.entity.Delivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {
    @Update("UPDATE sap_delivery SET status='PGI' WHERE vbeln=#{vbeln} AND status='PICKED'")
    int markPgi(String vbeln);
}
