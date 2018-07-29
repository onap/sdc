package org.onap.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigurationUtilsTest {
    @Test
    public void testCommaList() {
        List list = Arrays.asList("1", "2", 3);
        String commaSeparatedList = ConfigurationUtils.getCommaSeparatedList(list);
        list.forEach(o -> assertTrue(commaSeparatedList.contains(o.toString())));
    }

    @Test
    public void testCommaListWithNullAndEmptyStrings() {
        List list = Arrays.asList(null, "", " ");
        String commaSeparatedList = ConfigurationUtils.getCommaSeparatedList(list);
        assertTrue(commaSeparatedList.isEmpty());
    }

    @Test
    public void testGetArrayClassFunction() {
        assertEquals(String[].class , ConfigurationUtils.getArrayClass(String.class));
        assertNull(ConfigurationUtils.getArrayClass(ConfigurationUtilsTest.class));
    }
}