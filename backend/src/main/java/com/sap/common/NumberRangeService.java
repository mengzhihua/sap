package com.sap.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class NumberRangeService {
    private final JdbcTemplate jdbc;

    public NumberRangeService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object) {
        return next(object, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String object, String requestedPrefix) {
        String prefix = requestedPrefix == null ? defaultPrefix(object) : requestedPrefix;
        int updated = jdbc.update("UPDATE sap_number_range SET current_no=current_no+1 WHERE object_name=?", object);
        if (updated == 0) {
            try {
                jdbc.update("INSERT INTO sap_number_range(object_name,prefix,current_no) VALUES(?,?,1)",
                        object, prefix);
            } catch (DuplicateKeyException e) {
                jdbc.update("UPDATE sap_number_range SET current_no=current_no+1 WHERE object_name=?", object);
            }
        }
        Integer n = jdbc.queryForObject("SELECT current_no FROM sap_number_range WHERE object_name=?",
                Integer.class, object);
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
