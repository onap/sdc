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
import org.openecomp.sdc.be.resources.data.auditing.GetCategoryHierarchyEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.auditing.impl.AuditTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditGetCategoryHierarchyEventTest {
    @Mock
    private static AuditCassandraDao cassandraDao;
    @Captor
    private ArgumentCaptor<GetCategoryHierarchyEvent> eventCaptor;
    @Mock
    private static AuditingDao auditingDao;
    @Mock
    private Configuration.ElasticSearchConfig esConfig;

    private AuditingManager auditingManager;

    @Before
    public void setUp() {
        init(esConfig);
        auditingManager = new AuditingManager(auditingDao, cassandraDao);
    }

    @Test
    public void testNewGetCategoryHierarchyEvent() {
        AuditEventFactory factory = new AuditGetCategoryHierarchyEventFactory(
                CommonAuditData.newBuilder()
                        .description(DESCRIPTION)
                        .status(STATUS_OK)
                        .requestId(REQUEST_ID)
                        .build(),
                user, USER_DETAILS);
        when(auditingDao.addRecord(any(AuditingGenericEvent.class), eq(AuditingActionEnum.GET_CATEGORY_HIERARCHY.getAuditingEsType())))
                .thenReturn(ActionStatus.OK);
        when(cassandraDao.saveRecord(any(AuditingGenericEvent.class))).thenReturn(CassandraOperationStatus.OK);

        assertThat(auditingManager.auditEvent(factory)).isEqualTo(EXPECTED_GET_CATEGORY_HIERARCHY_LOG_STR);
        verifyEvent();
    }

    private void verifyEvent() {
        verify(cassandraDao).saveRecord(eventCaptor.capture());
        GetCategoryHierarchyEvent storedEvent = eventCaptor.getValue();
        assertThat(storedEvent.getModifier()).isEqualTo(USER_UID);
        assertThat(storedEvent.getStatus()).isEqualTo(STATUS_OK);
        assertThat(storedEvent.getDesc()).isEqualTo(DESCRIPTION);
        assertThat(storedEvent.getDetails()).isEqualTo(USER_DETAILS);
        assertThat(storedEvent.getRequestId()).isNotBlank();
        assertThat(storedEvent.getServiceInstanceId()).isNull();
        assertThat(storedEvent.getAction()).isEqualTo(AuditingActionEnum.GET_CATEGORY_HIERARCHY.getName());
    }

}
