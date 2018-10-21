package org.openecomp.sdc.common.utils;
import org.graalvm.compiler.nodes.memory.Access;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class CommonUtilTest {

    @Test
    public void testGetObjectAsMap() {
        Map<String, String> obj = new HashMap<>(1);
        obj.put(CommonUtil.DEFAULT, "");
        Map<String, Object> newMap = CommonUtil.getObjectAsMap(obj);

        boolean exists = newMap.containsKey(CommonUtil._DEFAULT);

        assertTrue(exists);
    }
}