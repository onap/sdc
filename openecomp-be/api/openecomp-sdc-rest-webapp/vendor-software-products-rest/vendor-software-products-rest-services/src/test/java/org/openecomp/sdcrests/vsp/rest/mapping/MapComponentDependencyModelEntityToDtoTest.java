package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;

public class MapComponentDependencyModelEntityToDtoTest {

    private static final String TEST_VALUE = "some_test_id";

    @Test
    public void testSourceId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setSourceComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getSourceId(), TEST_VALUE);
    }
    @Test
    public void testTargetId() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setTargetComponentId(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getTargetId(), TEST_VALUE);
    }

    @Test
    public void testRelationType() {
        ComponentDependencyModelEntity source = new ComponentDependencyModelEntity();
        ComponentDependencyModel target = new ComponentDependencyModel();
        MapComponentDependencyModelEntityToDto mapper = new  MapComponentDependencyModelEntityToDto();
        source.setRelation(TEST_VALUE);
        mapper.doMapping(source, target);
        assertEquals(target.getRelationType(), TEST_VALUE);
    }
}
