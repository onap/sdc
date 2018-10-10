package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyCreationDto;

public class MapComponentDependencyEntityToCreationDtoTest {

    @Test
    public void testId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyCreationDto target = new ComponentDependencyCreationDto();
        MapComponentDependencyEntityToCreationDto mapper = new  MapComponentDependencyEntityToCreationDto();
        String id = "some_test_id";
        source.setId(id);
        mapper.doMapping(source, target);
        assertEquals(target.getId(), id);
    }


}
