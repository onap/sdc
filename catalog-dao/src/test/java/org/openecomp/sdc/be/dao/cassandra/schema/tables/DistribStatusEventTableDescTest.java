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

package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;

class DistribStatusEventTableDescTest {

    private DistribStatusEventTableDesc createTestSubject() {
        return new DistribStatusEventTableDesc();
    }

    @Test
    void testUpdateColumnDistribDescription() throws Exception {
        final DistribStatusEventTableDesc testSubject = createTestSubject();
        final Map<String, ImmutablePair<DataType, Boolean>> columnDescription = testSubject.getColumnDescription();
        Assertions.assertNotNull(columnDescription);
        Assertions.assertEquals(10, columnDescription.size());
    }

    @Test
    void testGetTableName() throws Exception {
        final DistribStatusEventTableDesc testSubject = createTestSubject();
        Assertions.assertEquals(AuditingTypesConstants.DISTRIBUTION_STATUS_EVENT_TYPE, testSubject.getTableName());
    }

}
