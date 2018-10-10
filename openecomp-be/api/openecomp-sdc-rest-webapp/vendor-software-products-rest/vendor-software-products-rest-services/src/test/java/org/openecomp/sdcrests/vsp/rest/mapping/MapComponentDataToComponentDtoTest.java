package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;

public class MapComponentDataToComponentDtoTest {

    @Test
    public void testDisplayName() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String displayName = "some_test_name";
        source.setDisplayName(displayName);
        mapper.doMapping(source, target);
        assertEquals(target.getDisplayName(), displayName);
    }

    @Test
    public void testName() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String name = "component_name_1";
        source.setName(name);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), name);
    }

    @Test
    public void testDescription() {
        ComponentData source = new ComponentData();
        ComponentDto target = new ComponentDto();
        MapComponentDataToComponentDto mapper = new MapComponentDataToComponentDto();
        String description = "my_test_component";
        source.setDescription(description);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), description);
    }
}
