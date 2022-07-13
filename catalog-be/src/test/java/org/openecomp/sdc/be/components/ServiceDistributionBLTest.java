/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.components.distribution.engine.NotificationDataImpl;
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.components.impl.ActivationRequestInformation;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.utils.ComponentBusinessLogicMock;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

class ServiceDistributionBLTest extends ComponentBusinessLogicMock {

    private final ServiceDistributionValidation serviceDistributionValidation = Mockito.mock(ServiceDistributionValidation.class);
    private final DistributionEngine distributionEngine = Mockito.mock(DistributionEngine.class);
    private final HealthCheckBusinessLogic healthCheckBusinessLogic = Mockito.mock(HealthCheckBusinessLogic.class);
    private final ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private final ForwardingPathValidator forwardingPathValidator = Mockito.mock(ForwardingPathValidator.class);
    private final UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    private final ModelOperation modelOperation = Mockito.mock(ModelOperation.class);

    @InjectMocks
    private final ServiceBusinessLogic bl = new ServiceBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
        groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation,
        artifactsBusinessLogic, distributionEngine, componentInstanceBusinessLogic,
        serviceDistributionValidation, forwardingPathValidator, uiComponentDataConverter,
        artifactToscaOperation, componentContactIdValidator,
        componentNameValidator, componentTagsValidator, componentValidator, componentIconValidator,
        componentProjectCodeValidator, componentDescriptionValidator, modelOperation, null, null,
        null, null, null);

    private Service serviceToActivate;
    private ActivationRequestInformation activationRequestInformation;
    private String WORKLOAD_CONTEXT = "vnfContext";
    private String TENANT = "tenant";
    private String DID = "distributionId";
    private User modifier;

    @BeforeEach
    public void setup() {

        ExternalConfiguration.setAppName("catalog-be");
        MockitoAnnotations.openMocks(this);
        final ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
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
    void testActivateServiceOnTenantValidationFails() {
        int VALIDATION_FAIL_STATUS = 666;
        when(serviceDistributionValidation.validateActivateServiceRequest
            (anyString(), anyString(), any(User.class), any(ServiceDistributionReqInfo.class)))
            .thenReturn(Either.right(new ResponseFormat(VALIDATION_FAIL_STATUS)));
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals((int) stringResponseFormatEither.right().value().getStatus(), VALIDATION_FAIL_STATUS);
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test()
    void testDistributionAuthenticationFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
            .thenReturn(ActionStatus.AUTHENTICATION_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test()
    void testDistributionUnknownHostFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
            .thenReturn(ActionStatus.UNKNOWN_HOST);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test()
    void testDistributionConnectionErrorFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
            .thenReturn(ActionStatus.CONNNECTION_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    //TODO see if we want to add ActionStatus.AUTHENTICATION_ERROR to error-configuration.yaml
    @Test()
    void testDistributionObjectNotFoundFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
            .thenReturn(ActionStatus.OBJECT_NOT_FOUND);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    @Test()
    void testDistributionGeneralFails() {
        mockAllMethodsUntilDENotification();
        when(distributionEngine.notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), any(User.class)))
            .thenReturn(ActionStatus.GENERAL_ERROR);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isRight());
        assertEquals(502, (int) stringResponseFormatEither.right().value().getStatus());
        assertEquals("SVC4676", stringResponseFormatEither.right().value().getMessageId());
    }

    @Test
    void testDistributionOk() {
        mockAllMethodsUntilDENotification();
        ThreadLocalsHolder.setUuid(DID);
        when(distributionEngine
            .notifyService(anyString(), any(Service.class), any(INotificationData.class), anyString(), anyString(), any(User.class)))
            .thenReturn(ActionStatus.OK);
        Either<String, ResponseFormat> stringResponseFormatEither = callActivateServiceOnTenantWIthDefaults();
        assertTrue(stringResponseFormatEither.isLeft());
        assertEquals(stringResponseFormatEither.left().value(), DID);
    }

    private void mockAllMethodsUntilDENotification() {
        when(serviceDistributionValidation.validateActivateServiceRequest
            (anyString(), anyString(), any(User.class), any(ServiceDistributionReqInfo.class)))
            .thenReturn(Either.left(activationRequestInformation));
        when(healthCheckBusinessLogic.isDistributionEngineUp()).thenReturn(true);
    }

    private Either<String, ResponseFormat> callActivateServiceOnTenantWIthDefaults() {
        return bl.activateServiceOnTenantEnvironment("serviceId", "envId", modifier, new ServiceDistributionReqInfo("workload"));
    }
}
