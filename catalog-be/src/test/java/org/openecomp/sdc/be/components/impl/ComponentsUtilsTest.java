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
import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDownloadEvent;
import org.openecomp.sdc.be.resources.data.auditing.EcompOperationalEnvironmentEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.ARTIFACT_DATA;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.ARTIFACT_UUID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.COMMENT;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_STATE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CURRENT_VERSION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DCURR_STATUS;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESC_ERROR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_CONSUMER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DIST_RESOURCE_URL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DPREV_STATUS;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.INVARIANT_UUID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_FIRST_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_LAST_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.OP_ENV_ACTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.OP_ENV_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.OP_ENV_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.OP_ENV_TYPE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.PREV_RESOURCE_STATE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.PREV_RESOURCE_VERSION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_500;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TENANT_CONTEXT;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TESTER_USER_ROLE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.TOSCA_NODE_TYPE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.UPDATED_USER_EXTENDED_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_EMAIL;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_FIRST_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ComponentsUtilsTest {

    private static User modifier = new User();
    private Component service = new Service();
    private Resource resource = new Resource();

    @Mock
    private AuditingManager manager;

    @Mock
    private ResponseFormat responseFormat;

    @Captor
    private ArgumentCaptor<AuditBaseEventFactory> factoryCaptor;

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

        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.ARTIFACT_DELETE, service.getComponentType(),
                ResourceAuditData.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceAuditData.newBuilder()
                        .state(CURRENT_STATE)
                        .version(CURRENT_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build(),
                RESOURCE_NAME, COMMENT, ARTIFACT_DATA, DIST_ID);
        verifyResourceAdminEvent(AuditingActionEnum.ARTIFACT_DELETE.getName(), STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, true, true, true);
    }

    @Test
    public void auditComponentWhenAllParamsPassedAndMostFromComponent() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_OK));
        when(responseFormat.getFormattedMessage()).thenReturn(DESCRIPTION);

        service.setUUID(SERVICE_INSTANCE_ID);
        service.setInvariantUUID(INVARIANT_UUID);
        service.setState(LifecycleStateEnum.CERTIFIED);
        service.setVersion(CURRENT_VERSION);
        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.CREATE_RESOURCE, service.getComponentType(),
                ResourceAuditData.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build(),
                RESOURCE_NAME, COMMENT, ARTIFACT_DATA, DIST_ID);
        verifyResourceAdminEvent(AuditingActionEnum.CREATE_RESOURCE.getName(), STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, true, true, true);

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
        utils.auditComponent(responseFormat, modifier, service, AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST, service.getComponentType(),
                ResourceAuditData.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DPREV_STATUS).build(),
                ResourceAuditData.newBuilder()
                        .artifactUuid(ARTIFACT_UUID)
                        .distributionStatus(DCURR_STATUS).build());
        verifyResourceAdminEvent(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName(),
                STATUS_OK, DESCRIPTION, service.getComponentType().getValue(),
                false, true, true, false, false, true);
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
                ComponentTypeEnum.SERVICE,
                ResourceAuditData.newBuilder().state(PREV_RESOURCE_STATE).version(PREV_RESOURCE_VERSION).artifactUuid(ARTIFACT_UUID).build());
        verifyResourceAdminEvent(AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE.getName(), STATUS_500, DESC_ERROR,
                service.getComponentType().getValue(), false, true, false, false, false, false);
    }

    @Test
    public void auditComponentWhenCompIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        utils.auditComponent(responseFormat, modifier, AuditingActionEnum.START_CERTIFICATION_RESOURCE,
                RESOURCE_NAME, ComponentTypeEnum.SERVICE, COMMENT);
        verifyResourceAdminEvent(AuditingActionEnum.START_CERTIFICATION_RESOURCE.getName(), STATUS_500, DESC_ERROR, service.getComponentType().getValue(),
                true, false, false, false, true, false);
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
                false, false, false, false, false, false);
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
                ResourceAuditData.newBuilder()
                        .state(PREV_RESOURCE_STATE)
                        .version(PREV_RESOURCE_VERSION)
                        .artifactUuid(ARTIFACT_UUID)
                        .build(),
                ARTIFACT_UUID, ARTIFACT_DATA);
        verifyResourceAdminEvent(AuditingActionEnum.IMPORT_RESOURCE.getName(), STATUS_OK, DESCRIPTION, resource.getResourceType().name(),
                false, true, true, true, false, false);
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
                false, false, false, false, false, false);
    }

    @Test
    public void auditResourceWhenResourceIsNull() {
        when(responseFormat.getStatus()).thenReturn(Integer.valueOf(STATUS_500));
        when(responseFormat.getFormattedMessage()).thenReturn(DESC_ERROR);

        utils.auditResource(responseFormat, modifier, RESOURCE_NAME, AuditingActionEnum.CHECKOUT_RESOURCE);
        verifyResourceAdminEvent(AuditingActionEnum.CHECKOUT_RESOURCE.getName(), STATUS_500, DESC_ERROR, ComponentTypeEnum.RESOURCE.getValue(),
                true, false, false, false, false, false);
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
            boolean isDistStatusProvided) {
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
            if (resourceType.equals(ResourceTypeEnum.VFC.name())) {
                assertThat(event.getDid()).isNull();
            }
            else {
                assertThat(event.getDid()).isEqualTo(DIST_ID);
            }
        }
        else {
            assertThat(event.getArtifactData()).isNull();
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
}
