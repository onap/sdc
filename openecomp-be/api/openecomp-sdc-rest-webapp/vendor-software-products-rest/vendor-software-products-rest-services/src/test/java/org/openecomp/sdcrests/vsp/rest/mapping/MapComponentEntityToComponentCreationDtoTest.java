package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;

public class MapComponentEntityToComponentCreationDtoTest {

    @Test
    public void testVfcId() {
        ComponentEntity source = new ComponentEntity();
        ComponentCreationDto target = new ComponentCreationDto();
        MapComponentEntityToComponentCreationDto mapper = new  MapComponentEntityToComponentCreationDto();
        source.setId("some_test_id");
        mapper.doMapping(source, target);
        assertEquals(target.getVfcId(), "some_test_id");
    }
}
