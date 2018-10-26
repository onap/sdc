package org.openecomp.sdc.be.model.operations.utils;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;

import static org.junit.Assert.assertTrue;

public class ComponentValidationUtilsTest {

    private Resource resource;

    @Before
    public void setup() {
        resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setLastUpdaterUserId("jh003");
        resource.setIsDeleted(false);
    }

    @Test
    public void testCanWorkOnResource() {
        assertTrue (ComponentValidationUtils.canWorkOnResource(resource, "jh003"));
    }

    @Test
    public void testCanWorkOnComponent() {
        assertTrue (ComponentValidationUtils.canWorkOnComponent(resource, "jh003"));
    }
}