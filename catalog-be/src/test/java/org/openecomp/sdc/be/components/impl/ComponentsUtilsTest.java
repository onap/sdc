/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.auditing.impl.externalapi.AuditChangeLifecycleExternalApiEventFactory;
import org.openecomp.sdc.be.auditing.impl.externalapi.AuditCreateResourceExternalApiEventFactory;
import org.openecomp.sdc.be.auditing.impl.externalapi.AuditCreateServiceExternalApiEventFactory;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.resources.data.auditing.*;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentsUtilsTest {

    private static User modifier = new User();
    private Component service = new Service();
    private Resource resource = new Resource();

    @Mock
    private AuditingManager manager;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private HttpServletRequest request;

    private ArtifactDefinition artifactDefinition = null;

    @Captor
    private ArgumentCaptor<AuditEventFactory> factoryCaptor;

    @InjectMocks
    private static ComponentsUtils utils;

    @BeforeClass
    public static void setUpClass() {
        modifier.setFirstName(MODIFIER_FIRST_NAME);
        modifier.setLastName(MODIFIER_LAST_NAME);
        modifier.setUserId(MODIFIER_ID);
    }


    @Before
    public void setUp() {
        ThreadLocalsHolder.setUuid(REQUEST_ID);
        utils = new ComponentsUtils(manager);
    }

    @Test
    public void auditComponentWhenAllParamsPassed() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);
        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);

        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.ARTIFACT_DELETE,
                new ResourceCommonInfo(RESOURCE_NAME, service.getComponentType().getValue()),
                ResourceVersionInfo.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceVersionInfo.newBuilder()
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build(),
                COMMENT, artifactDefinition, DIST_ID);
        verifyResourceAdminEvent(AuditingActionEnum.ARTIFACT_DELETE.getName(), STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, false, true, true, true);
    }

    @Test
    public void auditComponentWhenAllParamsPassedAndMostFromComponent() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);
        service.setState(LifecycleStateEnum.CERTIFIED);
        service.setVersion(CURRENT_VERSION);
        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.CREATE_RESOURCE,
                new ResourceCommonInfo(RESOURCE_NAME, service.getComponentType().getValue()),
                ResourceVersionInfo.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build(),
                COMMENT, artifactDefinition, DIST_ID);
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName(), STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, false, true, true, true);

    }

    @Test
    public void auditComponentDistStateWithoutArtDataCommentAndDid() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);
        service.setState(LifecycleStateEnum.CERTIFIED);
        service.setName(RESOURCE_NAME);
        service.setVersion(CURRENT_VERSION);
        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST,
                new ResourceCommonInfo(service.getComponentType().getValue()),
                ResourceVersionInfo.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceVersionInfo.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build());
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(),
                STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, false, false, true, false);
    }

    @Test
    public void auditComponentWhenError() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);
        service.setState(LifecycleStateEnum.CERTIFIED);
        service.setName(RESOURCE_NAME);
        service.setVersion(CURRENT_VERSION);

        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE,
                new ResourceCommonInfo(ComponentTypeEnum.SERVICE.getValue()),
                ResourceVersionInfo.newBuilder().state(PREV_RESOURCE_STATE).version(PREV_RESOURCE_VERSION).artifactUuid(ARTIFACT_UUID).build());
        verifyResourceAdminEvent(AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE.getName(), STATUS_500, DESC_ERROR,
                service.getComponentType().getValue(), false, true, false, false, false, false, false);
    }

    @Test
    public void auditComponentWhenCompIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        utils.auditComponent(responseFormat, modifier, AuditingActionEnum.START_CERTIFICATION_RESOURCE,
                new ResourceCommonInfo(RESOURCE_NAME, ComponentTypeEnum.SERVICE.getValue()), COMMENT);
        verifyResourceAdminEvent(AuditingActionEnum.START_CERTIFICATION_RESOURCE.getName(), STATUS_500, DESC_ERROR, service.getComponentType().getValue(),
                true, false, false, false, true, false, false);
    }

    @Test
    public void auditComponentAdmin() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);
        service.setName(RESOURCE_NAME);
        service.setState(LifecycleStateEnum.CERTIFIED);
        service.setVersion(CURRENT_VERSION);
        utils.auditComponentAdmin(responseFormat, modifier, service, AuditingActionEnum.CREATE_RESOURCE, service.getComponentType());
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName(), STATUS_500, DESC_ERROR, service.getComponentType().getValue(),
                false, false, false, false, false, false, false);
    }

    @Test
    public void auditResourceWhenAllParamsPassed() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        resource.setUUID(SERVICE_INSTANCE_ID);
        resource.setInvariantUUID(INVARIANT_UUID);
        resource.setName(RESOURCE_NAME);
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        resource.setVersion(CURRENT_VERSION);
        resource.setToscaResourceName(TOSCA_NODE_TYPE);
        utils.auditResource(responseFormat, modifier, resource, null, AuditingActionEnum.IMPORT_RESOURCE,
                ResourceVersionInfo.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .build(),
                ARTIFACT_UUID, artifactDefinition);
        verifyResourceAdminEvent(AuditingActionEnum.IMPORT_RESOURCE.getName(), STATUS_OK, DESCRIPTION, resource.getResourceType().name(),
                false, true, true, false, false, false, false);
    }

    @Test
    public void auditResourceWithoutPrevFields() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        resource.setUUID(SERVICE_INSTANCE_ID);
        resource.setInvariantUUID(INVARIANT_UUID);
        resource.setName(RESOURCE_NAME);
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        resource.setVersion(CURRENT_VERSION);

        utils.auditResource(responseFormat, modifier, resource, AuditingActionEnum.UPDATE_RESOURCE_METADATA);
        verifyResourceAdminEvent(AuditingActionEnum.UPDATE_RESOURCE_METADATA.getName(), STATUS_500, DESC_ERROR, resource.getResourceType().name(),
                false, false, false, false, false, false, false);
    }

    @Test
    public void auditResourceWhenResourceIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        utils.auditResource(responseFormat, modifier, RESOURCE_NAME, AuditingActionEnum.CHECKOUT_RESOURCE);
        verifyResourceAdminEvent(AuditingActionEnum.CHECKOUT_RESOURCE.getName(), STATUS_500, DESC_ERROR, ComponentTypeEnum.RESOURCE.getValue(),
                true, false, false, false, false, false, false);
    }

    @Test
    public void auditUserAdminEvent() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        User userAfter = new User();
        userAfter.setFirstName(USER_FIRST_NAME);
        userAfter.setUserId(USER_ID);
        userAfter.setLastName(USER_LAST_NAME);
        userAfter.setRole(TESTER_USER_ROLE);
        userAfter.setEmail(USER_EMAIL);

        utils.auditAdminUserAction(AuditingActionEnum.ADD_USER, modifier, null, userAfter, responseFormat);
        verify(manager).auditEvent(factoryCaptor.capture());
        AuditEventFactory factory = factoryCaptor.getValue();
        UserAdminEvent event = (UserAdminEvent)factory.getDbEvent();
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getStatus()).isEqualTo(STATUS_OK);
        assertThat(event.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(event.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.ADD_USER.getName());
        assertThat(event.getUserBefore()).isNull();
        assertThat(event.getUserAfter()).isEqualTo(UPDATED_USER_EXTENDED_NAME);
    }

    @Test
    public void auditEcompOpEnvEvent() {
        utils.auditEnvironmentEngine(AuditingActionEnum.CREATE_ENVIRONMENT, OP_ENV_ID, OP_ENV_TYPE, OP_ENV_ACTION, OP_ENV_NAME, TENANT_CONTEXT);
        verify(manager).auditEvent(factoryCaptor.capture());
        AuditEventFactory factory = factoryCaptor.getValue();
        EcompOperationalEnvironmentEvent event = (EcompOperationalEnvironmentEvent)factory.getDbEvent();
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.CREATE_ENVIRONMENT.getName());
        assertThat(event.getOperationalEnvironmentId()).isEqualTo(OP_ENV_ID);
        assertThat(event.getOperationalEnvironmentType()).isEqualTo(OP_ENV_TYPE);
        assertThat(event.getOperationalEnvironmentName()).isEqualTo(OP_ENV_NAME);
        assertThat(event.getOperationalEnvironmentAction()).isEqualTo(OP_ENV_ACTION);
        assertThat(event.getTenantContext()).isEqualTo(TENANT_CONTEXT);
    }

    @Test
    public void auditDistrDownloadEvent(){
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        utils.auditDistributionDownload(responseFormat, new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL));

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditEventFactory factory = factoryCaptor.getValue();
        DistributionDownloadEvent event = (DistributionDownloadEvent)factory.getDbEvent();
        assertThat(event.getResourceUrl()).isEqualTo(DIST_RESOURCE_URL);
        assertThat(event.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(event.getStatus()).isEqualTo(STATUS_OK);
        assertThat(event.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(event.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD.getName());
    }

    private void verifyResourceAdminEvent(String action, String status, String desc, String resourceType, boolean isComponentNull,
            boolean isPrevStateAndVersionSet, boolean isCurrFieldsProvided, boolean isArtDataProvided, boolean isCommentProvided,
            boolean isDistStatusProvided, boolean isDidProvided) {
        verify(manager).auditEvent(factoryCaptor.capture());
        AuditEventFactory factory = factoryCaptor.getValue();
        ResourceAdminEvent event = (ResourceAdminEvent)factory.getDbEvent();
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getStatus()).isEqualTo(status);
        assertThat(event.getDesc()).isEqualTo(desc);
        assertThat(event.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(event.getAction()).isEqualTo(action);
        assertThat(event.getResourceName()).isEqualTo(RESOURCE_NAME);

        if (isComponentNull) {
            assertThat(event.getServiceInstanceId()).isNull();
            assertThat(event.getCurrState()).isNull();
            assertThat(event.getCurrVersion()).isNull();
            assertThat(event.getInvariantUUID()).isNull();
            assertThat(event.getResourceType()).isEqualTo(resourceType);
        }
        else {
            assertThat(event.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
            assertThat(event.getCurrState()).isEqualTo(CURRENT_STATE);
            assertThat(event.getCurrVersion()).isEqualTo(CURRENT_VERSION);
            assertThat(event.getInvariantUUID()).isEqualTo(INVARIANT_UUID);
        }
        if (action.equals(AuditingActionEnum.IMPORT_RESOURCE.getName())) {
            assertThat(event.getToscaNodeType()).isEqualTo(TOSCA_NODE_TYPE);
        }
        else {
            assertThat(event.getToscaNodeType()).isEmpty();
        }
        if (isPrevStateAndVersionSet) {
            assertThat(event.getPrevState()).isEqualTo(PREV_RESOURCE_STATE);
            assertThat(event.getPrevVersion()).isEqualTo(PREV_RESOURCE_VERSION);
            assertThat(event.getPrevArtifactUUID()).isEqualTo(ARTIFACT_UUID);
        }
        else {
            assertThat(event.getPrevState()).isNull();
            assertThat(event.getPrevVersion()).isNull();
            assertThat(event.getPrevArtifactUUID()).isNull();
        }
        if (isCurrFieldsProvided) {
            assertThat(event.getCurrArtifactUUID()).isEqualTo(ARTIFACT_UUID);
        }
        else {
            assertThat(event.getCurrArtifactUUID()).isNull();
        }
        if (isArtDataProvided) {
            assertThat(event.getArtifactData()).isEqualTo(ARTIFACT_DATA);
        }
        else {
            assertThat(event.getArtifactData()).isEmpty();
        }
        if (isDidProvided) {
            assertThat(event.getDid()).isEqualTo(DIST_ID);
        }
        else {
            assertThat(event.getDid()).isNull();
        }
        if (isCommentProvided) {
            assertThat(event.getComment()).isEqualTo(COMMENT);
        }
        else {
            assertThat(event.getComment()).isEmpty();
        }
        if (isDistStatusProvided) {
            assertThat(event.getDcurrStatus()).isEqualTo(DCURR_STATUS);
            assertThat(event.getDprevStatus()).isEqualTo(DPREV_STATUS);
        }
        else {
            assertThat(event.getDcurrStatus()).isNull();
            assertThat(event.getDprevStatus()).isNull();
        }
    }

    @Test
    public void auditChangeLifeCycleExternalApiEventWhenComponentAndResponseObjectAreNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        utils.auditChangeLifecycleAction(responseFormat, ComponentTypeEnum.RESOURCE, REQUEST_ID,
                null, null, new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL), modifier);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditChangeLifecycleExternalApiEventFactory factory = (AuditChangeLifecycleExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());
        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getInvariantUuid()).isEmpty();
        assertThat(event.getResourceName()).isNull();
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditChangeLifeCycleExternalApiEventWhenComponentIsNullAndResponseObjectIsNotNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);
        Component responseObject = new Resource();
        responseObject.setVersion(CURRENT_VERSION);
        responseObject.setState(LifecycleStateEnum.CERTIFIED);
        responseObject.setInvariantUUID(INVARIANT_UUID);
        responseObject.setUUID(SERVICE_INSTANCE_ID);

        utils.auditChangeLifecycleAction(responseFormat, ComponentTypeEnum.RESOURCE, REQUEST_ID,
                null, responseObject, new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL), modifier);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditChangeLifecycleExternalApiEventFactory factory = (AuditChangeLifecycleExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());
        verifyCommonDataForExternalApiEvent(event, true);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, false);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        assertThat(event.getResourceName()).isNull();
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditChangeLifeCycleExternalApiEventWhenComponentIsNotNullAndResponseObjectIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);
        Component component = new Resource();
        component.setVersion(PREV_RESOURCE_VERSION);
        component.setState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        component.setInvariantUUID(INVARIANT_UUID);
        component.setName(RESOURCE_NAME);

        utils.auditChangeLifecycleAction(responseFormat, ComponentTypeEnum.RESOURCE, REQUEST_ID,
                component, null, new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL), modifier);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditChangeLifecycleExternalApiEventFactory factory = (AuditChangeLifecycleExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());

        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, false);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        assertThat(event.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditChangeLifeCycleExternalApiEventWhenComponentAndResponseObjectAreNotNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);
        Component responseObject = new Resource();
        responseObject.setVersion(CURRENT_VERSION);
        responseObject.setState(LifecycleStateEnum.CERTIFIED);
        responseObject.setInvariantUUID(INVARIANT_UUID);
        responseObject.setUUID(SERVICE_INSTANCE_ID);

        Component component = new Resource();
        component.setVersion(PREV_RESOURCE_VERSION);
        component.setState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        component.setInvariantUUID(INVARIANT_UUID);
        component.setUUID(SERVICE_INSTANCE_ID);
        component.setName(RESOURCE_NAME);

        utils.auditChangeLifecycleAction(responseFormat, ComponentTypeEnum.RESOURCE, REQUEST_ID,
                null, responseObject, new DistributionData(DIST_CONSUMER_ID, DIST_RESOURCE_URL), modifier);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditChangeLifecycleExternalApiEventFactory factory = (AuditChangeLifecycleExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        assertThat(event.getAction()).isEqualTo(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName());
        verifyCommonDataForExternalApiEvent(event, true);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, false);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(event.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        assertThat(event.getResourceName()).isNull();
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());

    }

    private void verifyDistributionDataForExternalApiEvent(ExternalApiEvent event) {
        assertThat(event.getConsumerId()).isEqualTo(DIST_CONSUMER_ID);
        assertThat(event.getResourceURL()).isEqualTo(DIST_RESOURCE_URL);
    }

    private void verifyDistributionDataNotSetForExternalApiEvent(ExternalApiEvent event) {
        assertThat(event.getConsumerId()).isNull();
        assertThat(event.getResourceURL()).isNull();
    }

    private void verifyCommonDataForExternalApiEvent(ExternalApiEvent event, boolean isSucceeded) {
        if (isSucceeded) {
            assertThat(event.getDesc()).isEqualTo(DESCRIPTION);
            assertThat(event.getStatus()).isEqualTo(STATUS_OK);
            assertThat(event.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        }
        else {
            assertThat(event.getDesc()).isEqualTo(DESC_ERROR);
            assertThat(event.getStatus()).isEqualTo(STATUS_500);
            assertThat(event.getServiceInstanceId()).isNullOrEmpty();
        }
        assertThat(event.getRequestId()).isEqualTo(REQUEST_ID);
    }

    private void verifyCurrentResourceVersionInfoForExternalApiEvent(ExternalApiEvent event, boolean isNull) {
        assertThat(event.getCurrArtifactUuid()).isNull();
        if (isNull) {
            assertThat(event.getCurrState()).isNull();
            assertThat(event.getCurrVersion()).isNull();
        }
        else {
            assertThat(event.getCurrState()).isEqualTo(LifecycleStateEnum.CERTIFIED.name());
            assertThat(event.getCurrVersion()).isEqualTo(CURRENT_VERSION);
        }
    }

    private void verifyPreviousResourceVersionInfoForExternalApiEvent(ExternalApiEvent event, boolean isNull) {
        assertThat(event.getPrevArtifactUuid()).isNull();
        if (isNull) {
            assertThat(event.getPrevState()).isNull();
            assertThat(event.getPrevVersion()).isNull();
        }
        else {
            assertThat(event.getPrevState()).isEqualTo(LifecycleStateEnum.READY_FOR_CERTIFICATION.name());
            assertThat(event.getPrevVersion()).isEqualTo(PREV_RESOURCE_VERSION);
        }
    }

    @Test
    public void auditExternalCreateResourceEventWhenResourceObjectIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(DIST_CONSUMER_ID);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(DIST_RESOURCE_URL);

        utils.auditCreateResourceExternalApi(responseFormat, new ResourceCommonInfo(RESOURCE_NAME, ComponentTypeEnum.RESOURCE.getValue()),
                request, null);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateResourceExternalApiEventFactory factory = (AuditCreateResourceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo("(" + USER_ID + ")");
        assertThat(event.getInvariantUuid()).isNull();
        assertThat(event.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditExternalCreateResourceEventWhenResourceObjectIsNullAndRequestDataIsNotProvided() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(null);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(null);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(null);

        utils.auditCreateResourceExternalApi(responseFormat, new ResourceCommonInfo(ComponentTypeEnum.RESOURCE.getValue()),
                request, null);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateResourceExternalApiEventFactory factory = (AuditCreateResourceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataNotSetForExternalApiEvent(event);
        assertThat(event.getModifier()).isEmpty();
        assertThat(event.getInvariantUuid()).isNull();
        assertThat(event.getResourceName()).isNull();
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditExternalCreateResourceEventWhenResourceObjectAndRequestDataProvided() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(DIST_CONSUMER_ID);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(DIST_RESOURCE_URL);

        Resource resource = new Resource();
        resource.setName(RESOURCE_NAME);
        resource.setInvariantUUID(INVARIANT_UUID);
        resource.setUUID(SERVICE_INSTANCE_ID);

        utils.auditCreateResourceExternalApi(responseFormat, new ResourceCommonInfo(ComponentTypeEnum.RESOURCE.getValue()),
                request, resource);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateResourceExternalApiEventFactory factory = (AuditCreateResourceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, true);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getCurrArtifactUuid()).isNull();
        assertThat(event.getCurrState()).isEqualTo(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
        assertThat(event.getCurrVersion()).isEqualTo(ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION);
        assertThat(event.getModifier()).isEqualTo("(" + USER_ID + ")");
        assertThat(event.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
        assertThat(event.getResourceName()).isEqualTo(RESOURCE_NAME);
        assertThat(event.getResourceType()).isEqualTo(ComponentTypeEnum.RESOURCE.getValue());
    }

    @Test
    public void auditExternalCreateServiceEventWhenResourceObjectIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(DIST_CONSUMER_ID);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(DIST_RESOURCE_URL);

        utils.auditCreateServiceExternalApi(responseFormat, request, null);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateServiceExternalApiEventFactory factory = (AuditCreateServiceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getModifier()).isEqualTo("(" + USER_ID + ")");
        assertThat(event.getInvariantUuid()).isNull();
    }

    @Test
    public void auditExternalCreateServiceEventWhenResourceObjectIsNullAndRequestDataIsNotProvided() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(null);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(null);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(null);

        utils.auditCreateServiceExternalApi(responseFormat, request, null);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateServiceExternalApiEventFactory factory = (AuditCreateServiceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, false);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyCurrentResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataNotSetForExternalApiEvent(event);
        assertThat(event.getModifier()).isEmpty();
        assertThat(event.getInvariantUuid()).isNull();
    }

    @Test
    public void auditExternalCreateServiceEventWhenResourceObjectAndRequestDataProvided() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(USER_ID);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn(DIST_CONSUMER_ID);
        when(request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(REQUEST_ID);
        when(request.getRequestURI()).thenReturn(DIST_RESOURCE_URL);

        Service service = new Service();
        service.setInvariantUUID(INVARIANT_UUID);
        service.setUUID(SERVICE_INSTANCE_ID);

        utils.auditCreateServiceExternalApi(responseFormat, request, service);

        verify(manager).auditEvent(factoryCaptor.capture());
        AuditCreateServiceExternalApiEventFactory factory = (AuditCreateServiceExternalApiEventFactory)factoryCaptor.getValue();

        ExternalApiEvent event = (ExternalApiEvent)factory.getDbEvent();
        verifyCommonDataForExternalApiEvent(event, true);
        verifyPreviousResourceVersionInfoForExternalApiEvent(event, true);
        verifyDistributionDataForExternalApiEvent(event);
        assertThat(event.getCurrArtifactUuid()).isNull();
        assertThat(event.getModifier()).isEqualTo("(" + USER_ID + ")");
        assertThat(event.getInvariantUuid()).isEqualTo(INVARIANT_UUID);
    }

    @Test
    public void checkIfAuditEventIsExternal() {
        assertThat(utils.isExternalApiEvent(AuditingActionEnum.ARTIFACT_UPLOAD_BY_API)).isTrue();
        assertThat(utils.isExternalApiEvent(AuditingActionEnum.ARTIFACT_UPLOAD)).isFalse();
    }

}
