
package org.openecomp.sdc.be.components.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fj.data.Either;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/paths/path-context.xml")
public class ForwardingPathBussinessLogicTest extends BaseForwardingPathTest {


    @Test
    public void shouldFailToUpdateForwardingPathSincePathDoesNotExist() {
        Service service = initForwardPath();
        Either<Service, ResponseFormat> serviceResponseFormatEither = bl.updateForwardingPath(FORWARDING_PATH_ID, service, user, true);
        assertEquals(true, serviceResponseFormatEither.isRight());
    }

    @Test
    public void shouldFailToDeleteForwardingPathSincePathDoesNotExist() {
        Service service = initForwardPath();
        Either<Set<String>, ResponseFormat> serviceResponseFormatEither = bl.deleteForwardingPaths("delete_forward_test", Sets.newHashSet(FORWARDING_PATH_ID), user, true);
        assertEquals(true, serviceResponseFormatEither.isRight());
    }

    @Test
    public void shouldSucceedCreateAndDeleteForwardingPath() {
        Service createdService = createService();
        Service service = initForwardPath();
        assertNotNull(service);
        Either<Service, ResponseFormat> serviceResponseFormatEither = bl.createForwardingPath(createdService.getUniqueId(), service, user, true);
        assertEquals(true, serviceResponseFormatEither.isLeft());
        Map<String, ForwardingPathDataDefinition> forwardingPathsMap = serviceResponseFormatEither.left().value().getForwardingPaths();
        Set<String> pathIds = forwardingPathsMap.keySet();
        assertEquals(1, pathIds.size());
        String toscaResourceName = forwardingPathsMap.values().iterator().next().getToscaResourceName();

        // should return the created path
        Either<UiComponentDataTransfer, ResponseFormat> uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user, Lists.newArrayList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertEquals(true, uiResaponse.isLeft());
        UiServiceDataTransfer uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        Map<String, ForwardingPathDataDefinition> forwardingPaths = uiServiceDataTransfer.getForwardingPaths();
        assertTrue(forwardingPaths.keySet().equals(pathIds));
        Map<String, ForwardingPathDataDefinition> updatedForwardingPaths = new HashMap<>(forwardingPaths);
        String newProtocol = "https";
        ForwardingPathDataDefinition forwardingPathDataDefinition = updatedForwardingPaths.values().stream().findAny().get();
        assertEquals(forwardingPathDataDefinition.getProtocol(), HTTP_PROTOCOL);
        assertEquals(toscaResourceName, forwardingPathDataDefinition.getToscaResourceName());
        ForwardingPathDataDefinition forwardingPathDataDefinitionUpdate = updatedForwardingPaths.values().iterator().next();
        // updated values
        forwardingPathDataDefinitionUpdate.setProtocol(newProtocol);
        forwardingPathDataDefinitionUpdate.setPathElements(new ListDataDefinition<>());

        // should update value
        service.getForwardingPaths().clear();
        service.getForwardingPaths().put(forwardingPathDataDefinitionUpdate.getUniqueId(), forwardingPathDataDefinitionUpdate);
        serviceResponseFormatEither = bl.updateForwardingPath(createdService.getUniqueId(), service, user, true);
        assertTrue(serviceResponseFormatEither.isLeft());

        // make sure changes were applied
        uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user, Lists.newArrayList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertEquals(true, uiResaponse.isLeft());
        uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        Map<String, ForwardingPathDataDefinition> forwardingPathsUpdated = uiServiceDataTransfer.getForwardingPaths();
        ForwardingPathDataDefinition updatedData = forwardingPathsUpdated.values().iterator().next();
        assertEquals(newProtocol, updatedData.getProtocol());
        assertTrue(updatedData.getPathElements().isEmpty());

        Service createdData = serviceResponseFormatEither.left().value();
        Set<String> paths = createdData.getForwardingPaths().keySet();
        Either<Set<String>, ResponseFormat> setResponseFormatEither = bl.deleteForwardingPaths(createdService.getUniqueId(), paths, user, true);
        assertEquals(true, setResponseFormatEither.isLeft());

        // nothing to return now
        uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user, Lists.newArrayList(ComponentFieldsEnum.COMPONENT_INSTANCES.getValue(),ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertEquals(true, uiResaponse.isLeft());
        uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        forwardingPaths = uiServiceDataTransfer.getForwardingPaths();
        assertTrue(forwardingPaths == null || forwardingPaths.isEmpty());

    }




}

