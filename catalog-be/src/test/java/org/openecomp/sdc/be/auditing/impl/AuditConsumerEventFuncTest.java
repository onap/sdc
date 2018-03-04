package org.openecomp.sdc.be.auditing.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ConsumerEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CONSUMER_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CONSUMER_PASSWORD;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CONSUMER_SALT;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_ADD_ECOMP_USER_CRED_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_GET_ECOMP_USER_CRED_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.USER_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.init;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.modifier;

@RunWith(MockitoJUnitRunner.class)
public class AuditConsumerEventFuncTest {
    private AuditingManager auditingManager;

    private ConsumerDefinition consumer;

    @Mock
    private static AuditCassandraDao cassandraDao;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private static Configuration.ElasticSearchConfig esConfig;

    @Captor
    private ArgumentCaptor<AuditingGenericEvent> eventCaptor;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
        consumer = new ConsumerDefinition();
        consumer.setConsumerName(USER_ID);
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testNewAddEcompUserCredEvent() {
        AuditEventFactory factory = new AuditConsumerEventFactory(
                AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                modifier, consumer);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_ADD_ECOMP_USER_CRED_LOG_STR);
        verifyConsumerEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS.getName());
    }

    @Test
    public void testOldAddEcompUserCredEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS))).isEqualTo(EXPECTED_ADD_ECOMP_USER_CRED_LOG_STR);
        verifyConsumerEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS.getName());

    }

    @Test
    public void testNewGetEcompUserCredEvent() {
        AuditEventFactory factory = new AuditConsumerEventFactory(
                AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS,
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                modifier, consumer);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_ECOMP_USER_CRED_LOG_STR);
        verifyConsumerEvent(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS.getName());
    }

    @Test
    public void testOldGetEcompUserCredEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS))).isEqualTo(EXPECTED_GET_ECOMP_USER_CRED_LOG_STR);
        verifyConsumerEvent(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS.getName());

    }

    @Test
    public void buildConsumerNameWhenAllFieldsAreProvided() {
        consumer.setConsumerName(CONSUMER_NAME);
        consumer.setConsumerSalt(CONSUMER_SALT);
        consumer.setConsumerPassword(CONSUMER_PASSWORD);
        assertEquals(CONSUMER_NAME + "," + CONSUMER_SALT + "," + CONSUMER_PASSWORD, AuditConsumerEventFactory.buildConsumerName(consumer));
    }

    @Test
    public void buildConsumerNameWhenSaltIsNull() {
        consumer.setConsumerName(CONSUMER_NAME);
        consumer.setConsumerPassword(CONSUMER_PASSWORD);
        assertEquals(CONSUMER_NAME + "," + CONSUMER_PASSWORD, AuditConsumerEventFactory.buildConsumerName(consumer));
    }

    @Test
    public void buildConsumerNameWhenNameIsNull() {
        consumer.setConsumerName(null);
        consumer.setConsumerSalt(CONSUMER_SALT);
        consumer.setConsumerPassword(CONSUMER_PASSWORD);
        assertEquals(CONSUMER_SALT + "," + CONSUMER_PASSWORD, AuditConsumerEventFactory.buildConsumerName(consumer));
    }

    @Test
    public void buildConsumerNameWhenNameAndPwAreNull() {
        consumer.setConsumerName(null);
        consumer.setConsumerSalt(CONSUMER_SALT);
        assertEquals(CONSUMER_SALT, AuditConsumerEventFactory.buildConsumerName(consumer));
    }

    @Test
    public void buildConsumerNameWhenNameAndSaltAreNull() {
        consumer.setConsumerName(null);
        consumer.setConsumerPassword(CONSUMER_PASSWORD);
        assertEquals(CONSUMER_PASSWORD, AuditConsumerEventFactory.buildConsumerName(consumer));
    }

    @Test
    public void buildConsumerNameWhenConsumerObjectIsNull() {
        assertEquals("", AuditConsumerEventFactory.buildConsumerName(null));
    }


    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ECOMP_USER, USER_ID);

        return auditingFields;
    }

    private void verifyConsumerEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        ConsumerEvent storedEvent = (ConsumerEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getEcompUser()).isEqualTo(USER_ID);
    }
}
