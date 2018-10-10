package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;

public class MapComponentDependencyEntityToDtoTest {

    private static final String TEST_VALUE = "some_test_id";

    @Test
    public void testSourceId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyResponseDto target = new ComponentDependencyResponseDto();
        MapComponentDependencyEntityToDto mapper = new  MapComponentDependencyEntityToDto();
        source.setSourceComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getSourceId(), TEST_VALUE);
    }
    @Test
    public void testTargetId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyResponseDto target = new ComponentDependencyResponseDto();
        MapComponentDependencyEntityToDto mapper = new  MapComponentDependencyEntityToDto();
        source.setTargetComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getTargetId(), TEST_VALUE);
    }

    @Test
    public void testRelationType() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyResponseDto target = new ComponentDependencyResponseDto();
        MapComponentDependencyEntityToDto mapper = new  MapComponentDependencyEntityToDto();
        source.setRelation(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getRelationType(), TEST_VALUE);
    }

    @Test
    public void testId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyResponseDto target = new ComponentDependencyResponseDto();
        MapComponentDependencyEntityToDto mapper = new  MapComponentDependencyEntityToDto();
        source.setId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getId(), TEST_VALUE);
    }
}
