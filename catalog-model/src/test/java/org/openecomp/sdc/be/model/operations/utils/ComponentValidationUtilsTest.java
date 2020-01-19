/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.operations.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import fj.data.Either;

@RunWith(MockitoJUnitRunner.class)
public class ComponentValidationUtilsTest {

    private static final String USER_ID = "jh003";
    private static final String BAD_USER_ID = "badID";
    private static final String COMPONENT_ID = "componentID";

    private Resource resource;

    @Mock
    ToscaOperationFacade toscaOperationFacade;
    @Mock
    Component component;

    @Before
    public void setup() {
        resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setLastUpdaterUserId("jh003");
        resource.setIsDeleted(false);
    }

    @Test
    public void testCanWorkOnResource() {
        assertTrue(ComponentValidationUtils.canWorkOnResource(resource, USER_ID));
    }

    @Test
    public void testCanWorkOnBadResourceAndBadUser() {
        assertFalse(ComponentValidationUtils.canWorkOnResource(resource, BAD_USER_ID));

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertFalse(ComponentValidationUtils.canWorkOnResource(resource, USER_ID));

        resource.setIsDeleted(true);
        assertFalse(ComponentValidationUtils.canWorkOnResource(resource, USER_ID));
    }

    @Test
    public void testCanWorkOnComponent() {
        // given
        when(component.getLifecycleState()).thenReturn(resource.getLifecycleState());
        when(component.getLastUpdaterUserId()).thenReturn(resource.getLastUpdaterUserId());

        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID),
            eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.left(component));

        // when
        boolean resultFromExtractComponent = ComponentValidationUtils
            .canWorkOnComponent(COMPONENT_ID, toscaOperationFacade, USER_ID);
        boolean resultFromComponent =
            ComponentValidationUtils.canWorkOnComponent(component, USER_ID);

        // then
        assertTrue(resultFromExtractComponent);
        assertTrue(resultFromComponent);
    }

    @Test
    public void testCanWorkOnBadComponent() {
        // given
        when(component.getLifecycleState())
            .thenReturn(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(component.getLastUpdaterUserId()).thenReturn(resource.getLastUpdaterUserId());

        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID),
            eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.left(component));

        // when
        boolean resultFromExtractComponent = ComponentValidationUtils
            .canWorkOnComponent(COMPONENT_ID, toscaOperationFacade, USER_ID);
        boolean resultFromComponent =
            ComponentValidationUtils.canWorkOnComponent(component, USER_ID);

        // then
        assertFalse(resultFromExtractComponent);
        assertFalse(resultFromComponent);
    }

    @Test
    public void testCanWorkOnComponentWithBadUser() {
        // given
        when(component.getLifecycleState()).thenReturn(resource.getLifecycleState());
        when(component.getLastUpdaterUserId()).thenReturn(resource.getLastUpdaterUserId());

        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID),
            eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.left(component));

        // when
        boolean resultFromExtractComponent = ComponentValidationUtils
            .canWorkOnComponent(COMPONENT_ID, toscaOperationFacade, BAD_USER_ID);
        boolean resultFromComponent =
            ComponentValidationUtils.canWorkOnComponent(component, BAD_USER_ID);
        boolean resultFromComponentWithNullUser =
            ComponentValidationUtils.canWorkOnComponent(component, null);

        // then
        assertFalse(resultFromExtractComponent);
        assertFalse(resultFromComponent);
        assertFalse(resultFromComponentWithNullUser);
    }
}
