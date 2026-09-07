package com.sap.common;

import org.springframework.dao.DuplicateKeyException;
import com.sap.basis.entity.NumberRange;
import com.sap.basis.mapper.NumberRangeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Locale;

@Service
public class NumberRangeService {
    private final NumberRangeMapper mapper;
    private final TransactionTemplate requiresNew;

    public NumberRangeService(NumberRangeMapper mapper, PlatformTransactionManager transactionManager) {
        this.mapper = mapper;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object) {
        return next(object, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object, String requestedPrefix) {
        String prefix = requestedPrefix == null ? defaultPrefix(object) : requestedPrefix;
        if (mapper.currentNo(object) == null) {
            ensureRow(object, prefix);
        }

        if (mapper.increment(object) != 1) {
            throw new IllegalStateException("单号范围不存在: " + object);
        }

        Integer currentNo = mapper.currentNo(object);
        if (currentNo == null) {
            throw new IllegalStateException("单号范围不存在: " + object);
        }
        return format(prefix, currentNo);
    }

    private void ensureRow(String object, String prefix) {
        requiresNew.execute(status -> {
            NumberRange range = new NumberRange();
            range.setObjectName(object);
            range.setPrefix(prefix);
            range.setCurrentNo(0);
            try {
                mapper.insert(range);
            } catch (DuplicateKeyException ignored) {
            }
            return null;
        });
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
