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

package org.openecomp.sdc.be.components;

        import org.junit.Before;
        import org.junit.Test;
        import org.junit.runner.RunWith;
        import org.mockito.Mock;
        import org.mockito.Mockito;
        import org.mockito.junit.MockitoJUnitRunner;
        import org.openecomp.sdc.be.auditing.impl.AuditingManager;
        import org.openecomp.sdc.be.dao.api.ActionStatus;
        import org.openecomp.sdc.be.dao.impl.AuditingDao;
        import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
        import org.openecomp.sdc.common.util.ThreadLocalsHolder;

        import java.util.EnumMap;
        import java.util.UUID;

        import static org.assertj.core.api.Assertions.assertThat;
        import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditingManagerTest extends BeConfDependentTest {

    private AuditingManager auditingManager;
    @Mock
    private AuditingDao auditingDao;

    @Before
    public void setup() {
        auditingManager = new AuditingManager(auditingDao, null);
        when(auditingDao.addRecord(Mockito.anyMap(), Mockito.anyString())).thenReturn(ActionStatus.OK);

    }

    @Test
    public void testNormalizeEmptyStringValuesAndUuid() {
        EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<>(AuditingFieldsKeysEnum.class);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, "Create");
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_DESC, null);
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT, "      null ");

        String randomUUID = UUID.randomUUID().toString();
        ThreadLocalsHolder.setUuid(randomUUID);

        auditingManager.auditEvent(auditingFields);
        // Checking normalization
        assertThat(auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC).toString()).isEmpty();
        assertThat(auditingFields.get(AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT).toString()).isEmpty();
        assertThat(auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID)).isEqualTo(randomUUID);
    }
}

