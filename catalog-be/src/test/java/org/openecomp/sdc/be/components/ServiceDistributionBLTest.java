package org.openecomp.sdc.be.components;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.components.impl.ActivationRequestInformation;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by chaya on 10/26/2017.
 */
public class ServiceDistributionBLTest {

    @InjectMocks
    ServiceBusinessLogic bl = new ServiceBusinessLogic();

    @Mock
    ServiceDistributionValidation serviceDistributionValidation;

    @Mock
    HealthCheckBusinessLogic healthCheckBusinessLogic;

    @Mock
    ToscaOperationFacade toscaOperationFacade;

    ComponentsUtils componentsUtils;

    @Mock
    DistributionEngine distributionEngine;

    private Service serviceToActivate;
    private ActivationRequestInformation activationRequestInformation;
    private String WORKLOAD_CONTEXT = "vnfContext";
    private String TENANT = "tenant";
    private String DID = "distributionId";
    private User modifier;


    public ServiceDistributionBLTest() {
    }

    @Before
    public void setup() {

        ExternalConfiguration.setAppName("catalog-be");
        MockitoAnnotations.initMocks(this);
        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
        bl.setComponentsUtils(componentsUtils);
        serviceToActivate = new Service();
        serviceToActivate.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
        activationRequestInformation = new ActivationRequestInformation(serviceToActivate, WORKLOAD_CONTEXT, TENANT);
        when(toscaOperationFacade.getToscaElement(anyString(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(serviceToActivate));
        when(distributionEngine.buildServiceForDistribution(any(Service.class), anyString(), anyString()))
                .thenReturn(new NotificationDataImpl());
        modifier = new User();
        modifier.setUserId("uid");
        modifier.setFirstName("user");
    }

    @Test
    public void testActivateServiceOnTenantValidationFails() {
        int VALIDATION_FAIL_STATUS = 666;
        when(serviceDistributionValidation.validateActivateServiceRequest
                (anyString(), anyString(),any(User.class), any(ServiceDistributionReqInfo.class)))
                .thenReturn(Either.right(new ResponseFormat(VALIDATION_FAIL_STATUS)));
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals((int) stringResponseFormatEither.right().value().getStatus(), VALIDATION_FAIL_STATUS);
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test
    public void testDistributionAuthenticationFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(),any(User.class)))
                .thenReturn(ActionStatus.AUTHENTICATION_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test
    public void testDistributionUnknownHostFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
                .thenReturn(ActionStatus.UNKNOWN_HOST);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test
    public void testDistributionConnectionErrorFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
                .thenReturn(ActionStatus.CONNNECTION_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test
    public void testDistributionObjectNotFoundFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
                .thenReturn(ActionStatus.OBJECT_NOT_FOUND);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    @Test
    public void testDistributionGeneralFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
                .thenReturn(ActionStatus.GENERAL_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    @Test
    public void testDistributionOk() {
        mockAllMethodsUntilDENotification();
        ThreadLocalsHolder.setUuid(DID);
        when(distributionEngine.notifyService(anyString(),any(Service.class), any(INotificationData.class), anyString(), anyString(), any(User.class)))
                .thenReturn(ActionStatus.OK);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isLeft());
        assertEquals(stringResponseFormatEither.left().value(), DID);
    }

    private void mockAllMethodsUntilDENotification() {
        when(serviceDistributionValidation.validateActivateServiceRequest
                (anyString(), anyString(),any(User.class), any(ServiceDistributionReqInfo.class)))
                .thenReturn(Either.left(activationRequestInformation));
        when(healthCheckBusinessLogic.isDistributionEngineUp()).thenReturn(true);
    }

    private Either<String, ResponseFormat> callActivateServiceOnTenantWIthDefaults() {
        return bl.activateServiceOnTenantEnvironment("serviceId", "envId", modifier, new ServiceDistributionReqInfo("workload"));
    }
}
