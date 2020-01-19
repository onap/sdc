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
 */

package org.openecomp.sdc.be.distribution;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.CambriaHandler;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineInitTask;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.distribution.api.client.TopicRegistrationResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicUnregistrationResponse;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.Wrapper;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.distribution.engine.DistributionEngineInitTask.buildTopicName;

public class DistributionBusinessLogicTest extends BeConfDependentTest {

    @InjectMocks
    DistributionBusinessLogic distributionBusinessLogic = Mockito.spy(DistributionBusinessLogic.class);

    private CambriaHandler cambriaHandler = Mockito.mock(CambriaHandler.class);
    private AuditHandler auditHandler = Mockito.mock(AuditHandler.class);
    private DistributionEngine distributionEngine = Mockito.mock(DistributionEngine.class);

    private CambriaErrorResponse errorResponse = new CambriaErrorResponse(CambriaOperationStatus.CONNNECTION_ERROR,
            HttpStatus.SC_SERVICE_UNAVAILABLE);
    private CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
    private DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(cambriaHandler);
    }

    @Test
    public void testHandleRegistrationOnTenant() {
        List<String> uebs = Arrays.asList("11","22");
        OperationalEnvironmentEntry environment = new OperationalEnvironmentEntry();
        environment.setEnvironmentId("1");
        environment.setUebApikey("11");
        environment.setUebSecretKey("22");
        RegistrationRequest registrationRequest =
                new RegistrationRequest("myPublicKey", "myEnv",uebs ,false);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        when(distributionEngine.getEnvironmentByDmaapUebAddress(uebs))
                .thenReturn(environment);
        testHandleRegistrationBasic(registrationRequest, responseWrapper);
    }

    @Test
    public void handleUnregistrationOnTenant() {
        List<String> uebs = Arrays.asList("11","22");
        OperationalEnvironmentEntry environment = new OperationalEnvironmentEntry();
        environment.setEnvironmentId("1");
        environment.setUebApikey("11");
        environment.setUebSecretKey("22");
        RegistrationRequest registrationRequest =
                new RegistrationRequest("myPublicKey", "myEnv",uebs ,false);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);

        when(cambriaHandler.unRegisterFromTopic(Mockito.anyCollection(), eq("11"),
                eq("22"), eq("myPublicKey"), Mockito.any(SubscriberTypeEnum.class), Mockito.anyString()))
                .thenReturn(okResponse);
        when(distributionEngine.getEnvironmentByDmaapUebAddress(uebs))
                .thenReturn(environment);

        distributionBusinessLogic.handleUnRegistration(responseWrapper, registrationRequest, auditHandler);

        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER), Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER), Mockito.anyString());

        Mockito.verify(cambriaHandler, Mockito.times(2)).unRegisterFromTopic(Mockito.anyCollection(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class),
                Mockito.anyString());

        assertTrue(!responseWrapper.isEmpty());
        Response response = responseWrapper.getInnerElement();
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

        TopicUnregistrationResponse okTopicUnregisterResponse = (TopicUnregistrationResponse) response.getEntity();

        String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        String actualStatusTopicName = okTopicUnregisterResponse.getDistrStatusTopicName();
        assertEquals(expectedStatusTopicName, actualStatusTopicName);

        String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionNotifTopicName(),
                registrationRequest.getDistrEnvName());
        String actualNotificationTopicName = okTopicUnregisterResponse.getDistrNotificationTopicName();
        assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

        assertEquals(okTopicUnregisterResponse.getNotificationUnregisterResult(), CambriaOperationStatus.OK);
        assertEquals(okTopicUnregisterResponse.getStatusUnregisterResult(), CambriaOperationStatus.OK);
    }

    @Test
    public void testHandleRegistrationNoConsumeStatusTopic() {
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", false);
        Wrapper<Response> responseWrapper = new Wrapper<>();

        testHandleRegistrationBasic(registrationRequest, responseWrapper);

        String expectedStatusTopicName = buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(responseWrapper,
                registrationRequest, SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);

    }

    @Test
    public void testHandleRegistrationConsumeStatusTopic() {
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", true);
        Wrapper<Response> responseWrapper = new Wrapper<>();

        testHandleRegistrationBasic(registrationRequest, responseWrapper);

        String expectedStatusTopicName = buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper,
                registrationRequest, SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);
    }
    /**
     * Registration Fails When registering as consumer to Notification With Consumer Status flag false.
     */
    @Test
    public void testHandleRegistrationFailedScenario() {

        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", false);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        String expectedNotificationTopicName = buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
        String expectedStatusTopicName = buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());

        Runnable failWhen = () -> when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName)).thenReturn(errorResponse);
        testHandleRegistrationFailed(registrationRequest, responseWrapper, failWhen);
        //Registered
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName);
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper, registrationRequest,SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName);
        //Did Not Register
        verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName);
        //Unregistered Activated (rollback)
        verify(cambriaHandler, Mockito.times(1)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.PRODUCER, expectedStatusTopicName);

        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);

    }

    /**
     * Registration Fails When registering as consumer to Notification With Consumer Status flag true.
     */
    @Test
    public void testHandleRegistrationFailedConsumeStatusTopic() {

        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", true);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        String expectedNotificationTopicName = buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
        String expectedStatusTopicName = buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());

        Runnable failWhen = () -> when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName)).thenReturn(errorResponse);
        testHandleRegistrationFailed(registrationRequest, responseWrapper, failWhen);

        //Registered
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName);
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper, registrationRequest,SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName);
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName);
        //Unregistered Activated (rollback)
        verify(cambriaHandler, Mockito.times(1)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.PRODUCER, expectedStatusTopicName);
        verify(cambriaHandler, Mockito.times(1)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);
        //Unregistered Not Activated
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedNotificationTopicName);
    }

    /**
     * Registration Fails When registering as consumer to status With Consumer Status flag true.
     */
    @Test
    public void testHandleRegistrationFailedConsumeStatusTopic2() {

        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", true);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        String expectedNotificationTopicName = buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
        String expectedStatusTopicName = buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());

        //Failing on new registration
        Runnable failWhen = () -> when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName)).thenReturn(errorResponse);
        testHandleRegistrationFailed(registrationRequest, responseWrapper, failWhen);
        //Registered
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName);
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName);
        //Did Not Register
        verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                responseWrapper, registrationRequest,SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName);
        //Unregistered Activated (rollback)
        verify(cambriaHandler, Mockito.times(1)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.PRODUCER, expectedStatusTopicName);
        //Unregistered Not Activated
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedNotificationTopicName);

    }


    /**
     * Registration Fails When registering as PRODUCER to status With Consumer Status flag true.
     */
    @Test
    public void testHandleRegistrationFailedConsumeStatusTopic3() {

        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", true);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        String expectedNotificationTopicName = buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
        String expectedStatusTopicName = buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());

        //Failing on new registration
        Runnable failWhen = () -> when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName)).thenReturn(errorResponse);
        testHandleRegistrationFailed(registrationRequest, responseWrapper, failWhen);
        //Registered
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName);
        //Did Not Register
        verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                responseWrapper,registrationRequest, SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName);
        verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                responseWrapper, registrationRequest,SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName);
        //Unregistered Not Activated
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.PRODUCER, expectedStatusTopicName);
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedNotificationTopicName);
        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(config.getUebServers(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                SubscriberTypeEnum.CONSUMER, expectedStatusTopicName);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleUnRegistrationHappyScenario() {
        CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);

        when(cambriaHandler.unRegisterFromTopic(Mockito.anyCollection(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class), Mockito.anyString()))
                .thenReturn(okResponse);

        Wrapper<Response> responseWrapper = new Wrapper<>();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", false);
        distributionBusinessLogic.handleUnRegistration(responseWrapper, registrationRequest, auditHandler);

        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER), Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER), Mockito.anyString());

        Mockito.verify(cambriaHandler, Mockito.times(2)).unRegisterFromTopic(Mockito.anyCollection(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class),
                Mockito.anyString());

        assertTrue(!responseWrapper.isEmpty());
        Response response = responseWrapper.getInnerElement();
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

        TopicUnregistrationResponse okTopicUnregisterResponse = (TopicUnregistrationResponse) response.getEntity();

        String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        String actualStatusTopicName = okTopicUnregisterResponse.getDistrStatusTopicName();
        assertEquals(expectedStatusTopicName, actualStatusTopicName);

        String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionNotifTopicName(),
                registrationRequest.getDistrEnvName());
        String actualNotificationTopicName = okTopicUnregisterResponse.getDistrNotificationTopicName();
        assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

        assertEquals(okTopicUnregisterResponse.getNotificationUnregisterResult(), CambriaOperationStatus.OK);
        assertEquals(okTopicUnregisterResponse.getStatusUnregisterResult(), CambriaOperationStatus.OK);

    }

    @Test
    public void testHandleUnRegistrationFailedScenario() {
        CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
        CambriaErrorResponse errorResponse = new CambriaErrorResponse(CambriaOperationStatus.AUTHENTICATION_ERROR,
                HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Wrapper<Response> responseWrapper = new Wrapper<>();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv", false);
        DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();
        String expectedStatusTopicName = DistributionEngineInitTask
                .buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
        String expectedNotificationTopicName = DistributionEngineInitTask
                .buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
        when(cambriaHandler.unRegisterFromTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName)).thenReturn(okResponse);
        when(cambriaHandler.unRegisterFromTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER,
                expectedNotificationTopicName)).thenReturn(errorResponse);

        distributionBusinessLogic.handleUnRegistration(responseWrapper, registrationRequest, auditHandler);

        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(
                eq(responseWrapper), eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER),
                Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.PRODUCER), Mockito.anyString());
        Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(
                eq(registrationRequest), eq(SubscriberTypeEnum.CONSUMER), Mockito.anyString());

        assertTrue(!responseWrapper.isEmpty());
        Response response = responseWrapper.getInnerElement();
        assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);

        TopicUnregistrationResponse okTopicUnregisterResponse = (TopicUnregistrationResponse) response.getEntity();

        String actualStatusTopicName = okTopicUnregisterResponse.getDistrStatusTopicName();
        assertEquals(expectedStatusTopicName, actualStatusTopicName);

        String actualNotificationTopicName = okTopicUnregisterResponse.getDistrNotificationTopicName();
        assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

        assertEquals(okTopicUnregisterResponse.getNotificationUnregisterResult(),
                CambriaOperationStatus.AUTHENTICATION_ERROR);
        assertEquals(okTopicUnregisterResponse.getStatusUnregisterResult(), CambriaOperationStatus.OK);

    }

    @SuppressWarnings("unchecked")
    private void testHandleRegistrationBasic(RegistrationRequest registrationRequest,
            Wrapper<Response> responseWrapper) {
        CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
        when(cambriaHandler.registerToTopic(Mockito.anyCollection(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class), Mockito.anyString()))
                .thenReturn(okResponse);

        String expectedStatusTopicName = buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        String expectedNotificationTopicName = buildTopicName(
                configurationManager.getDistributionEngineConfiguration().getDistributionNotifTopicName(),
                registrationRequest.getDistrEnvName());

        distributionBusinessLogic.handleRegistration(responseWrapper, registrationRequest, auditHandler);

        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper,
                registrationRequest, SubscriberTypeEnum.PRODUCER, expectedStatusTopicName);
        verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper,
                registrationRequest, SubscriberTypeEnum.CONSUMER, expectedNotificationTopicName);

        verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(Mockito.anyCollection(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class), Mockito.anyString());

        assertTrue(!responseWrapper.isEmpty());
        Response response = responseWrapper.getInnerElement();
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

        TopicRegistrationResponse okTopicResponse = (TopicRegistrationResponse) response.getEntity();

        String actualStatusTopicName = okTopicResponse.getDistrStatusTopicName();
        assertEquals(expectedStatusTopicName, actualStatusTopicName);

        String actualNotificationTopicName = okTopicResponse.getDistrNotificationTopicName();
        assertEquals(expectedNotificationTopicName, actualNotificationTopicName);
    }

    private void testHandleRegistrationFailed(RegistrationRequest registrationRequest,
            Wrapper<Response> responseWrapper, Runnable failWhen) {
        String expectedStatusTopicName = buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());





        when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER,
                expectedStatusTopicName)).thenReturn(okResponse);

        when(cambriaHandler.registerToTopic(config.getUebServers(), config.getUebPublicKey(),
                config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER,
                expectedStatusTopicName)).thenReturn(okResponse);

        failWhen.run();

        distributionBusinessLogic.handleRegistration(responseWrapper, registrationRequest, auditHandler);

        assertTrue(!responseWrapper.isEmpty());
        Response response = responseWrapper.getInnerElement();
        assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);



    }
}
