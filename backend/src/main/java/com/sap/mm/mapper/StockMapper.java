package com.sap.mm.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sap.mm.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
    @Update("UPDATE sap_stock SET unrestricted_qty = unrestricted_qty + #{qty}, " +
            "value = value + #{value} WHERE matnr=#{matnr} AND werks=#{werks} AND lgort=#{lgort} " +
            "AND unrestricted_qty + #{qty} >= 0")
    int adjust(String matnr, String werks, String lgort, java.math.BigDecimal qty, java.math.BigDecimal value);
}
