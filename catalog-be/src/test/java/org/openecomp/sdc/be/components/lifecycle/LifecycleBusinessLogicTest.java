package org.openecomp.sdc.be.components.lifecycle;

import fj.data.Either;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleBusinessLogicTest extends LifecycleTestBase {

    @Mock
    private IGraphLockOperation graphLockOperation;

    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    @Mock
    private ComponentsUtils componentsUtils;

    @InjectMocks
    LifecycleBusinessLogic lifecycleBusinessLogic = new LifecycleBusinessLogic();

    @Mock
    CertificationChangeTransition certificationChangeTransition;

    @Mock
    CheckinTransition checkinTransition;

    @Mock
    CatalogOperation catalogOperations;

    @Before
    public void before() {
        lifecycleBusinessLogic.init();
        Map<String, LifeCycleTransition> startTransition = lifecycleBusinessLogic.getStartTransition();
        startTransition.put(LifeCycleTransitionEnum.CHECKIN.name(), checkinTransition);
        startTransition.put(LifeCycleTransitionEnum.CERTIFY.name(), certificationChangeTransition);
    }


    @Test
    public void certifyCheckedOutComponent() {
        String ID_BEFORE_CHECKIN = "id";
        String ID_AFTER_CHECKIN = "id2";
        String ID_AFTER_CERTIFY = "id3";
        Service service = createServiceObject();
        fillService(service, ID_BEFORE_CHECKIN);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        User modifier = createUser();

        LifecycleChangeInfoWithAction remarks = new LifecycleChangeInfoWithAction("remarks");

        Service serviceAfterCheckIn = createServiceObject();
        fillService(serviceAfterCheckIn, ID_AFTER_CHECKIN);
        serviceAfterCheckIn.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

        Service serviceAfterCertify = createServiceObject();
        fillService(serviceAfterCertify, ID_AFTER_CERTIFY);
        serviceAfterCertify.setLifecycleState(LifecycleStateEnum.CERTIFIED);

        when(toscaOperationFacade.getToscaElement(ID_BEFORE_CHECKIN)).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(ID_BEFORE_CHECKIN, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(checkinTransition.getComponentOwner(service, ComponentTypeEnum.SERVICE)).thenReturn(Either.left(modifier));
        when(checkinTransition.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, modifier, modifier, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, remarks)).thenReturn(Either.left(true));
        Mockito.doReturn(Either.left(serviceAfterCheckIn)).when(checkinTransition).changeState(ComponentTypeEnum.SERVICE, service, serviceBusinessLogic, modifier, modifier, false, false);

        when(certificationChangeTransition.getComponentOwner(serviceAfterCheckIn, ComponentTypeEnum.SERVICE)).thenReturn(Either.left(modifier));
        when(certificationChangeTransition.validateBeforeTransition(serviceAfterCheckIn, ComponentTypeEnum.SERVICE, modifier, modifier, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, remarks)).thenReturn(Either.left(true));
        Mockito.doReturn(Either.left(serviceAfterCertify)).when(certificationChangeTransition).changeState(ComponentTypeEnum.SERVICE, serviceAfterCheckIn, serviceBusinessLogic, modifier, modifier, false, false);
        when(catalogOperations.updateCatalog(ChangeTypeEnum.LIFECYCLE,serviceAfterCertify)).thenReturn(ActionStatus.OK);
        Either<? extends Component, ResponseFormat> serviceAfterCertificationEither = lifecycleBusinessLogic.changeComponentState(ComponentTypeEnum.SERVICE, ID_BEFORE_CHECKIN, modifier, LifeCycleTransitionEnum.CERTIFY, remarks, false, true);
        Component serviceAfterCertification = serviceAfterCertificationEither.left().value();
        assertThat(serviceAfterCertification.getUniqueId()).isEqualTo(ID_AFTER_CERTIFY);
        assertThat(serviceAfterCertification.getLifecycleState()).isEqualTo(LifecycleStateEnum.CERTIFIED);
    }

    @Test
    public void certifyCheckedInComponent() {
        String ID_BEFORE_CERTIFY = "id";
        String ID_AFTER_CERTIFY = "id2";
        Service service = createServiceObject();
        fillService(service, ID_BEFORE_CERTIFY);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

        Service serviceAfterCertify = createServiceObject();
        fillService(serviceAfterCertify, ID_AFTER_CERTIFY);
        serviceAfterCertify.setLifecycleState(LifecycleStateEnum.CERTIFIED);

        User modifier = createUser();
        LifecycleChangeInfoWithAction remarks = new LifecycleChangeInfoWithAction("remarks");

        when(toscaOperationFacade.getToscaElement(ID_BEFORE_CERTIFY)).thenReturn(Either.left(service));
        when(graphLockOperation.lockComponent(ID_BEFORE_CERTIFY, NodeTypeEnum.Service)).thenReturn(StorageOperationStatus.OK);
        when(certificationChangeTransition.getComponentOwner(service, ComponentTypeEnum.SERVICE)).thenReturn(Either.left(modifier));
        when(certificationChangeTransition.validateBeforeTransition(service, ComponentTypeEnum.SERVICE, modifier, modifier, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN, remarks)).thenReturn(Either.left(true));
        Mockito.doReturn(Either.left(serviceAfterCertify)).when(certificationChangeTransition).changeState(ComponentTypeEnum.SERVICE, service, serviceBusinessLogic, modifier, modifier, false, false);
        when(catalogOperations.updateCatalog(ChangeTypeEnum.LIFECYCLE,serviceAfterCertify)).thenReturn(ActionStatus.OK);
        Either<? extends Component, ResponseFormat> serviceAfterCertificationEither = lifecycleBusinessLogic.changeComponentState(ComponentTypeEnum.SERVICE, ID_BEFORE_CERTIFY, modifier, LifeCycleTransitionEnum.CERTIFY, remarks, false, true);
        Component serviceAfterCertification = serviceAfterCertificationEither.left().value();
        assertThat(serviceAfterCertification.getUniqueId()).isEqualTo(ID_AFTER_CERTIFY);
        assertThat(serviceAfterCertification.getLifecycleState()).isEqualTo(LifecycleStateEnum.CERTIFIED);
    }

    private User createUser() {
        User modifier = new User();
        modifier.setUserId("modifier");
        modifier.setFirstName("Albert");
        modifier.setLastName("Einstein");
        modifier.setRole(Role.DESIGNER.name());
        return modifier;
    }

    private void fillService(Service service, String id) {
        service.setUniqueId(id);
        service.setVersion("0.2");
        service.setHighestVersion(true);
    }
}
