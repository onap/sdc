package org.openecomp.sdc.common.utils;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class CommonUtilTest {

    @Test
    public void test() {
        Map<String, String> obj = new HashMap<>();
        obj.put("default", new String());
        CommonUtil.getObjectAsMap(obj);
    }
}