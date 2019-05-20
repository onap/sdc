package org.openecomp.sdc.be.components.merge.path;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceForwardingPathMerge;
import org.openecomp.sdc.be.components.path.BaseForwardingPathVersionChangeTest;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

public class ComponentInstanceForwardingPathMergeTest extends BaseForwardingPathVersionChangeTest {

    @InjectMocks
    private ComponentInstanceForwardingPathMerge testInstance;

    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    private User user;

    @Before
    public void setUpData() {
        MockitoAnnotations.initMocks(this);
        user = new User();
        user.setUserId("44");
        user.setRole(UserRoleEnum.ADMIN.getName());
    }

    @Test
    public void testIgnoreMergeSinceItIsNotService() {

        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
        assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());
        Either<Component, ResponseFormat> componentResponseFormatEither = testInstance
            .mergeDataAfterCreate(user, dataHolder, newNodeAC, "3344");
        assertNotNull(componentResponseFormatEither);
        assertTrue(componentResponseFormatEither.isLeft());
        assertEquals(newNodeAC, componentResponseFormatEither.left().value());
    }

    @Test
    public void mergeShouldDelete() {
        Set<String> forwardingPathNamesToDeleteOnComponentInstanceDeletion = new ForwardingPathUtils()
            .findForwardingPathNamesToDeleteOnComponentInstanceDeletion(service, nodeACI.getUniqueId());
        nodeACI.getCapabilities().clear();
        newNodeAC.getCapabilities().clear();
        Either<Set<String>, ResponseFormat> returnValue = Either.left(forwardingPathNamesToDeleteOnComponentInstanceDeletion);
        when(serviceBusinessLogic.deleteForwardingPaths(any(), any(), any(), anyBoolean()))
            .thenReturn(returnValue);
        when(toscaOperationFacade.getToscaFullElement(any())).thenReturn(Either.left(newNodeAC));

        // Change internal ci, just like change version do
        service.getComponentInstances().remove(nodeACI);
        service.getComponentInstances().add(newNodeACI);

        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
        assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());
        Either<Component, ResponseFormat> componentResponseFormatEither = testInstance
            .mergeDataAfterCreate(user, dataHolder, service, newNodeA);
        assertNotNull(componentResponseFormatEither);
        assertTrue(componentResponseFormatEither.isLeft());
        assertEquals(0, ((Service) componentResponseFormatEither.left().value()).getForwardingPaths().size());
    }

    @Test
    public void mergeShouldUpdate() {
          when(serviceBusinessLogic.updateForwardingPath(any(), any(), any(), anyBoolean()))
              .then(invocationOnMock -> Either.left(service));
           when(toscaOperationFacade.getToscaFullElement(any())).thenReturn(Either.left(newNodeAC));
          testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
          assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());

          // Change internal ci, just like change version do
          service.getComponentInstances().remove(nodeACI);
          service.getComponentInstances().add(newNodeACI);

          Either<Component, ResponseFormat> componentResponseFormatEither = testInstance
              .mergeDataAfterCreate(user, dataHolder, service, newNodeA);
          assertNotNull(componentResponseFormatEither);
          assertTrue(componentResponseFormatEither.isLeft());
    }

    @Test
    public void handleNullCapailities() {
        nodeACI.setCapabilities(null);
        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
    }
}