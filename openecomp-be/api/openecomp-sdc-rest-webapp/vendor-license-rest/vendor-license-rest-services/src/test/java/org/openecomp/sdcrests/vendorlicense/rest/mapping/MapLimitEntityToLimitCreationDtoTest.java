package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;

public class MapLimitEntityToLimitCreationDtoTest {

    @Test
    public void testId() {
        LimitEntity source = new LimitEntity();
        LimitCreationDto target = new LimitCreationDto();
        MapLimitEntityToLimitCreationDto mapper = new MapLimitEntityToLimitCreationDto();
        String param = "52d4d919-015a-4a46-af04-4d0dec17e88d";
        source.setId(param);
        mapper.doMapping(source, target);
        assertEquals(target.getLimitId(), param);
    }

}
