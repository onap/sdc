package org.openecomp.sdc.be.auditing.impl.category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.CategoryEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.CATEGORY;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.DESCRIPTION;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.EXPECTED_ADD_CATEGORY_LOG_STR;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.GROUPING_NAME;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.MODIFIER_UID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.REQUEST_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.RESOURCE_TYPE;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.STATUS_OK;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.SUB_CATEGORY;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.init;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.modifier;

@RunWith(MockitoJUnitRunner.class)
public class AuditCategoryEventFuncTest {
    private AuditingManager auditingManager;

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
        ThreadLocalsHolder.setUuid(REQUEST_ID);
    }

    @Test
    public void testNewAddCategoryEvent() {
        AuditEventFactory builder = new AuditAddCategoryEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .serviceInstanceId(SERVICE_INSTANCE_ID)
                        .build(),
                modifier, CATEGORY, SUB_CATEGORY, GROUPING_NAME, RESOURCE_TYPE);

        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.ADD_CATEGORY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(builder)).isEqualTo(EXPECTED_ADD_CATEGORY_LOG_STR);
        verifyCategoryEvent(AuditingActionEnum.ADD_CATEGORY.getName());
    }

    @Test
    public void testOldAddEcompUserCredEvent() {
        when(auditingDao.addRecord(anyMap(), eq(AuditingActionEnum.ADD_CATEGORY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(fillMap(AuditingActionEnum.ADD_CATEGORY))).isEqualTo(EXPECTED_ADD_CATEGORY_LOG_STR);
        verifyCategoryEvent(AuditingActionEnum.ADD_CATEGORY.getName());

    }

    private EnumMap<AuditingFieldsKeysEnum, Object> fillMap(AuditingActionEnum action) {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action.getName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFirstName() + " " + modifier.getLastName());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, STATUS_OK);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, DESCRIPTION);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME, CATEGORY);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME, SUB_CATEGORY);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME, GROUPING_NAME);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, RESOURCE_TYPE);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID);

        return auditingFields;
    }

    private void verifyCategoryEvent(String action) {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        CategoryEvent storedEvent = (CategoryEvent) eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(MODIFIER_UID);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
//        assertThat(storedEvent.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(storedEvent.getServiceInstanceId()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(storedEvent.getAction()).isEqualTo(action);
        assertThat(storedEvent.getCategoryName()).isEqualTo(CATEGORY);
        assertThat(storedEvent.getSubCategoryName()).isEqualTo(SUB_CATEGORY);
        assertThat(storedEvent.getGroupingName()).isEqualTo(GROUPING_NAME);
        assertThat(storedEvent.getResourceType()).isEqualTo(RESOURCE_TYPE);

    }
}
