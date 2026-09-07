package com.sap.fi.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sap.fi.entity.AccountingDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface AccountingDocumentMapper extends BaseMapper<AccountingDocument> {
    @Select("SELECT COALESCE(SUM(debit),0) AS debit, COALESCE(SUM(credit),0) AS credit " +
            "FROM sap_acc_document_item WHERE gjahr=#{gjahr}")
    Map<String, Object> balanceSummary(String gjahr);
}
