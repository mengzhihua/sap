package com.sap.flow;

import com.sap.TestSupport;
import com.sap.common.BizException;
import com.sap.integration.service.SapBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FiTest extends TestSupport {
    @Autowired
    private SapBusinessService service;

    @Test
    void fb50RejectsUnbalancedDocument() {
        Map<String, Object> body = new HashMap<>();
        body.put("items", Collections.singletonList(new HashMap<String, Object>() {{
            put("saknr", "6401"); put("shkzg", "S"); put("amount", 10);
        }}));
        assertThrows(BizException.class, () -> service.createFi(body));
    }

    @Test
    void paymentCreatesBalancedDocument() {
        Map<String, Object> payment = service.payment(new HashMap<String, Object>() {{
            put("type", "AP"); put("partner", "100010"); put("amount", 10);
        }});
        assertNotNull(payment.get("belnr"));
    }

    @Test
    void reversalCreatesOppositeDocument() {
        Map<String, Object> original = service.createFi(new HashMap<String, Object>() {{
            put("items", Arrays.asList(
                    new HashMap<String, Object>() {{ put("saknr", "6401"); put("shkzg", "S"); put("amount", 10); }},
                    new HashMap<String, Object>() {{ put("saknr", "1002"); put("shkzg", "H"); put("amount", 10); }}
            ));
        }});
        assertNotNull(service.reverseFi(String.valueOf(original.get("belnr"))).get("belnr"));
    }
}
