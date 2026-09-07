package com.sap.common;

import org.springframework.dao.DuplicateKeyException;
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
        if (mapper.increment(object) == 0) {
            try {
                NumberRange range = new NumberRange();
                range.setObjectName(object);
                range.setPrefix(prefix);
                range.setCurrentNo(1);
                mapper.insert(range);
                return format(prefix, 1);
            } catch (DuplicateKeyException e) {
                mapper.increment(object);
            }
        }
        Integer currentNo = mapper.currentNo(object);
        return format(prefix, currentNo == null ? 1 : currentNo);
    }

    private String format(String prefix, int number) {
        return prefix + String.format(Locale.ROOT, "%08d", number);
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
