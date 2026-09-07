package com.sap.common;

import org.springframework.dao.DuplicateKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.basis.entity.NumberRange;
import com.sap.basis.mapper.NumberRangeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class NumberRangeService {
    private final NumberRangeMapper mapper;

    public NumberRangeService(NumberRangeMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object) {
        return next(object, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object, String requestedPrefix) {
        String prefix = requestedPrefix == null ? defaultPrefix(object) : requestedPrefix;
        NumberRange range = mapper.selectOne(new LambdaQueryWrapper<NumberRange>()
                .eq(NumberRange::getObjectName, object));
        if (range == null) {
            try {
                range = new NumberRange();
                range.setObjectName(object);
                range.setPrefix(prefix);
                range.setCurrentNo(1);
                mapper.insert(range);
            } catch (DuplicateKeyException e) {
                range = mapper.selectOne(new LambdaQueryWrapper<NumberRange>()
                        .eq(NumberRange::getObjectName, object));
            }
        } else {
            range.setCurrentNo(range.getCurrentNo() + 1);
            mapper.updateById(range);
        }
        Integer n = range == null ? 1 : range.getCurrentNo();
        return prefix + String.format(Locale.ROOT, "%08d", n == null ? 1 : n);
    }

    private String defaultPrefix(String object) {
        if ("PO".equals(object)) return "45";
        if ("MATERIAL".equals(object)) return "50";
        if ("INVOICE".equals(object)) return "51";
        if ("FI".equals(object)) return "1";
        if ("SO".equals(object)) return "1";
        if ("DN".equals(object)) return "8";
        if ("BILLING".equals(object)) return "9";
        if ("PRODORD".equals(object)) return "1000";
        if ("PR".equals(object)) return "10";
        return "";
    }
}
