package org.openecomp.sdc.asdctool.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.be.resources.data.auditing.*;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.common.datastructure.AuditingFieldsKey.*;

@RunWith(MockitoJUnitRunner.class)
public class DataMigrationTest {
    private final static String DESCRIPTION = "OK";
    private final static String STATUS = "200";
    private final static String SERVICE_INSTANCE_ID = "SERVICE_INSTANCE_ID";
    private final static String MODIFIER = "MODIFIER";
    private final static String REQUEST_ID = "REQUEST_ID";
    private final static String USER = "USER";
    private final static String USER_BEFORE = "USER_BEFORE";
    private final static String USER_AFTER = "USER_AFTER";
    private final static String ARTIFACT_UUID = "ARTIFACT_UUID";

    private final static String PREV_STATE = "PREV_STATE";
    private final static String CURR_STATE = "CURR_STATE";
    private final static String PREV_VERSION = "PREV_VERSION";
    private final static String CURR_VERSION = "CURR_VERSION";
    private final static String DPREV_STATUS = "DPREV_STATUS";
    private final static String DCURR_STATUS = "CURR_STATUS";
    private final static String INVARIANT_UUID = "INVARIANT_UUID";
    private final static String ARTIFACT_DATA = "ARTIFACT_DATA";
    private final static String COMMENT = "COMMENT";
    private final static String DISTRIBUTION_ID = "DISTRIBUTION_ID";
    private final static String TOSCA_NODE_TYPE = "TOSCA_NODE_TYPE";
    private final static String CONSUMER_ID = "CONSUMER_ID";
    private final static String RESOURCE_URL = "RESOURCE_URL";
    private final static String ENV_ID = "ENV_ID";
    private final static String VNF_WORKLOAD_CONTEXT = "VNF_WORKLOAD_CONTEXT";
    private final static String TENANT = "TENANT";
    private final static String RESOURCE_NAME = "RESOURCE_NAME";
    private final static String RESOURCE_TYPE = "RESOURCE_TYPE";
    private final static String AUTH_URL = "AUTH_URL";
    private final static String AUTH_RELM = "AUTH_RELM";
    private final static String TOPIC_NAME = "TOPIC_NAME";

    private final static String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);

    private static DataMigration dataMigration = new DataMigration();

    private final static String ES_STRING = "{\"" + AuditingFieldsKey.AUDIT_ACTION + "\":\"%s\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_NAME + "\":\"" + RESOURCE_NAME + "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_TOSCA_NODE_TYPE + "\":\"" + TOSCA_NODE_TYPE +
            "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_PREV_VERSION + "\":\"" + PREV_VERSION + "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_PREV_STATE + "\":\"" + PREV_STATE +
            "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_TYPE + "\":\"" + RESOURCE_TYPE + "\", \"" + AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID + "\":\"" + SERVICE_INSTANCE_ID +
            "\", \"" + AuditingFieldsKey.AUDIT_INVARIANT_UUID + "\":\"" + INVARIANT_UUID + "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION + "\":\"" + CURR_VERSION +
            "\", \"" + AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE + "\":\"" + CURR_STATE + "\", \"" + AuditingFieldsKey.AUDIT_MODIFIER_UID + "\":\"" + MODIFIER +
            "\", \"" + AuditingFieldsKey.AUDIT_DESC + "\":\"" + DESCRIPTION + "\", \"" + AuditingFieldsKey.AUDIT_STATUS + "\":\"" + STATUS +
            "\", \"" + AuditingFieldsKey.AUDIT_REQUEST_ID + "\":\"" + REQUEST_ID + "\", \"" + AuditingFieldsKey.AUDIT_CURR_ARTIFACT_UUID + "\":\"" + ARTIFACT_UUID +
            "\", \"" + AuditingFieldsKey.AUDIT_PREV_ARTIFACT_UUID + "\":\"" + ARTIFACT_UUID + "\", \"" + AuditingFieldsKey.AUDIT_ARTIFACT_DATA + "\":\"" + ARTIFACT_DATA +
            "\", \"" + AuditingFieldsKey.AUDIT_TIMESTAMP + "\":\"%s\"}";


    private String timestampStr;

    private HashMap<AuditingFieldsKey, String> dataMap = new HashMap<>();

    @Before
    public void setUp() {
        dataMap.put(AUDIT_DESC, DESCRIPTION);
        dataMap.put(AUDIT_STATUS, STATUS);
        dataMap.put(AUDIT_REQUEST_ID, REQUEST_ID);
        dataMap.put(AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);
        dataMap.put(AUDIT_MODIFIER_UID, MODIFIER);
        dataMap.put(AUDIT_USER_BEFORE, USER_BEFORE);
        dataMap.put(AUDIT_USER_UID, USER);
        dataMap.put(AUDIT_USER_AFTER, USER_AFTER);
        dataMap.put(AUDIT_AUTH_URL, AUTH_URL);
        dataMap.put(AUDIT_AUTH_REALM, AUTH_RELM);
        dataMap.put(AUDIT_PREV_ARTIFACT_UUID, ARTIFACT_UUID);
        dataMap.put(AUDIT_CURR_ARTIFACT_UUID, ARTIFACT_UUID);
        dataMap.put(AUDIT_RESOURCE_PREV_STATE, PREV_STATE);
        dataMap.put(AUDIT_RESOURCE_PREV_VERSION, PREV_VERSION);
        dataMap.put(AUDIT_RESOURCE_CURR_STATE, CURR_STATE);
        dataMap.put(AUDIT_RESOURCE_CURR_VERSION, CURR_VERSION);
        dataMap.put(AUDIT_RESOURCE_DPREV_STATUS, DPREV_STATUS);
        dataMap.put(AUDIT_RESOURCE_DCURR_STATUS, DCURR_STATUS);
        dataMap.put(AUDIT_INVARIANT_UUID, INVARIANT_UUID);
        dataMap.put(AUDIT_ARTIFACT_DATA, ARTIFACT_DATA);
        dataMap.put(AUDIT_RESOURCE_COMMENT, COMMENT);
        dataMap.put(AUDIT_DISTRIBUTION_ID, DISTRIBUTION_ID);
        dataMap.put(AUDIT_RESOURCE_TOSCA_NODE_TYPE, TOSCA_NODE_TYPE);
        dataMap.put(AUDIT_DISTRIBUTION_CONSUMER_ID, CONSUMER_ID);
        dataMap.put(AUDIT_RESOURCE_URL, RESOURCE_URL);
        dataMap.put(AUDIT_DISTRIBUTION_ENVIRONMENT_ID, ENV_ID);
        dataMap.put(AUDIT_DISTRIBUTION_VNF_WORKLOAD_CONTEXT, VNF_WORKLOAD_CONTEXT);
        dataMap.put(AUDIT_DISTRIBUTION_TENANT, TENANT);
        dataMap.put(AUDIT_RESOURCE_NAME, RESOURCE_NAME);
        dataMap.put(AUDIT_RESOURCE_TYPE, RESOURCE_TYPE);
        timestampStr = simpleDateFormat.format(new Date());
        dataMap.put(AUDIT_TIMESTAMP, timestampStr);
        dataMap.put(AUDIT_DISTRIBUTION_TOPIC_NAME, TOPIC_NAME);

    }

    @Test
    public void createUserAdminEvent() {
        dataMap.put(AUDIT_ACTION, AuditingActionEnum.ADD_USER.getName());
        AuditingGenericEvent event = dataMigration.createAuditEvent(dataMap, Table.USER_ADMIN_EVENT);
        assertThat(AuditingActionEnum.ADD_USER.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, true);
        verifyUserAdminEvent((UserAdminEvent) event);
    }

    @Test
    public void createResourceAdminEvent() {
        dataMap.put(AUDIT_ACTION, AuditingActionEnum.UPDATE_RESOURCE_METADATA.getName());
        AuditingGenericEvent event = dataMigration.createAuditEvent(dataMap, Table.RESOURCE_ADMIN_EVENT);
        assertThat(AuditingActionEnum.UPDATE_RESOURCE_METADATA.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, true);
        verifyResourceAdminEvent((ResourceAdminEvent)event);
    }

    @Test
    public void createDistributionNotificationEvent() {
        dataMap.put(AUDIT_ACTION, AuditingActionEnum.DISTRIBUTION_NOTIFY.getName());
        AuditingGenericEvent event = dataMigration.createAuditEvent(dataMap, Table.DISTRIBUTION_NOTIFICATION_EVENT);
        assertThat(AuditingActionEnum.DISTRIBUTION_NOTIFY.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, true);
        verifyDistributionNotificationEvent((DistributionNotificationEvent)event);
    }


    @Test
    public void createEventForNoneAuditTable() {
        assertThat(dataMigration.createAuditEvent(dataMap, Table.COMPONENT_CACHE)).isNull();

    }

    @Test
    public void createEventWhenSomeFieldValuesNotSet() {
        dataMap.clear();
        dataMap.put(AUDIT_ACTION, AuditingActionEnum.AUTH_REQUEST.getName());
        AuditingGenericEvent event = dataMigration.createAuditEvent(dataMap, Table.AUTH_EVENT);
        assertThat(AuditingActionEnum.AUTH_REQUEST.getName()).isEqualTo(event.getAction());
        assertThat(event.getStatus()).isNull();
        assertThat(event.getDesc()).isNull();
        assertThat(event.getRequestId()).isNull();
    }

    @Test
    public void createAuthEvent() {
        dataMap.put(AUDIT_ACTION, AuditingActionEnum.AUTH_REQUEST.getName());
        AuditingGenericEvent event = dataMigration.createAuditEvent(dataMap, Table.AUTH_EVENT);
        assertThat(AuditingActionEnum.AUTH_REQUEST.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, false);
        verifyAuthEvent((AuthEvent) event);
    }

    @Test
    public void createImportResourceEventFromEsObject() throws IOException{
        AuditingGenericEvent event = dataMigration.createAuditRecordForCassandra(String.format(ES_STRING, AuditingActionEnum.IMPORT_RESOURCE.getName(), timestampStr), Table.RESOURCE_ADMIN_EVENT);
        assertThat(AuditingActionEnum.IMPORT_RESOURCE.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, true);
        verifyResourceAdminEvent((ResourceAdminEvent)event);
    }

    @Test
    public void createGetUserListEventFromEsObject() throws IOException{
        AuditingGenericEvent event = dataMigration.createAuditRecordForCassandra(String.format(ES_STRING, AuditingActionEnum.GET_USERS_LIST.getName(), timestampStr),
                Table.GET_USERS_LIST_EVENT);
        assertThat(AuditingActionEnum.GET_USERS_LIST.getName()).isEqualTo(event.getAction());
        verifyCommonData(event, false);
        assertThat(((GetUsersListEvent)event).getModifier()).isEqualTo(MODIFIER);
    }

    @Test(expected = NullPointerException.class)
    public void createEventFromEsFailedWhenActionDoesNotExist() throws IOException {
        dataMigration.createAuditRecordForCassandra(String.format(ES_STRING, "WRONG", timestampStr),
                Table.CONSUMER_EVENT);
    }

    @Test(expected = NullPointerException.class)
    public void createRecordWhenJsonIsEmpty() throws IOException{
        dataMigration.createAuditRecordForCassandra("{}",
                Table.CONSUMER_EVENT);
    }

    private void verifyCommonData(AuditingGenericEvent event, boolean isServiceInstanceProvided) {
        assertThat(STATUS).isEqualTo(event.getStatus());
        if (isServiceInstanceProvided) {
            assertThat(SERVICE_INSTANCE_ID).isEqualTo(event.getServiceInstanceId());
        }
        else {
            assertThat(event.getServiceInstanceId()).isNull();
        }
        assertThat(DESCRIPTION).isEqualTo(event.getDesc());
        assertThat(REQUEST_ID).isEqualTo(event.getRequestId());
    }

    private void verifyUserAdminEvent(UserAdminEvent event) {
        assertThat(USER_AFTER).isEqualTo(event.getUserAfter());
        assertThat(USER_BEFORE).isEqualTo(event.getUserBefore());
        verifyTimestamp(event.getTimestamp1());
    }

    private void verifyAuthEvent(AuthEvent event) {
        assertThat(USER).isEqualTo(event.getUser());
        assertThat(AUTH_URL).isEqualTo(event.getUrl());
        assertThat(event.getAuthStatus()).isNull();
        assertThat(AUTH_RELM).isEqualTo(event.getRealm());
    }

    private void verifyTimestamp(Date date) {
        assertThat(timestampStr).isEqualTo(simpleDateFormat.format(date));
    }

    private void verifyResourceAdminEvent(ResourceAdminEvent event) {
        assertThat(CURR_STATE).isEqualTo(event.getCurrState());
        assertThat(CURR_VERSION).isEqualTo(event.getCurrVersion());
        assertThat(ARTIFACT_UUID).isEqualTo(event.getCurrArtifactUUID());
        assertThat(PREV_STATE).isEqualTo(event.getPrevState());
        assertThat(PREV_VERSION).isEqualTo(event.getPrevVersion());
        assertThat(ARTIFACT_UUID).isEqualTo(event.getPrevArtifactUUID());
        assertThat(INVARIANT_UUID).isEqualTo(event.getInvariantUUID());
        assertThat(ARTIFACT_DATA).isEqualTo(event.getArtifactData());
        assertThat(RESOURCE_NAME).isEqualTo(event.getResourceName());
        assertThat(RESOURCE_TYPE).isEqualTo(event.getResourceType());
        verifyTimestamp(event.getTimestamp1());
        assertThat(TOSCA_NODE_TYPE).isEqualTo( event.getToscaNodeType());
    }

    private void verifyDistributionNotificationEvent(DistributionNotificationEvent event) {
        assertThat(CURR_STATE).isEqualTo(event.getCurrState());
        assertThat(CURR_VERSION).isEqualTo(event.getCurrVersion());
        assertThat(TOPIC_NAME).isEqualTo(event.getTopicName());
        assertThat(DISTRIBUTION_ID).isEqualTo(event.getDid());
        assertThat(ENV_ID).isEqualTo(event.getEnvId());
        assertThat(VNF_WORKLOAD_CONTEXT).isEqualTo(event.getVnfWorkloadContext());
        assertThat(TENANT).isEqualTo(event.getTenant());
        verifyTimestamp(event.getTimestamp1());
    }

}