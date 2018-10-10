package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;

public class MapComponentEntityToComponentDtoTest {

    @Test
    public void testId() {
        ComponentEntity source = new ComponentEntity();
        ComponentDto target = new ComponentDto();
        MapComponentEntityToComponentDto mapper = new  MapComponentEntityToComponentDto();
        source.setId("some_test_id");
        mapper.doMapping(source, target);
        assertEquals(target.getId(),"some_test_id");
    }
}
