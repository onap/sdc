package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;

public class MapComponentDependencyModelRequestToEntityTest {

    private static final String TEST_VALUE = "some_test_id";
    private static final String RELATION_TYPE = "dependsOn";

    @Test
    public void testSourceId() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setSourceId(TEST_VALUE);
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getSourceComponentId(), TEST_VALUE);
    }
    @Test
    public void testTargetId() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setTargetId(TEST_VALUE);
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getTargetComponentId(), TEST_VALUE);
    }

    @Test
    public void testRelationType() {
        ComponentDependencyModel source = new ComponentDependencyModel();
        ComponentDependencyModelEntity target = new ComponentDependencyModelEntity();
        MapComponentDependencyModelRequestToEntity mapper = new   MapComponentDependencyModelRequestToEntity();
        source.setRelationType(RELATION_TYPE);
        mapper.doMapping(source, target);
        assertEquals(target.getRelation(), RELATION_TYPE);
    }
}
