package org.openecomp.sdc.common.utils;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class CommonUtilTest {

    @Test
    public void test() {
        Map<String, String> obj = new HashMap<>();
        obj.put(CommonUtil.DEFAULT, new String());
        Map<String, Object> newMap = CommonUtil.getObjectAsMap(obj);

        boolean exists = newMap.containsKey(CommonUtil._DEFAULT);

        assertTrue(exists);
    }
}