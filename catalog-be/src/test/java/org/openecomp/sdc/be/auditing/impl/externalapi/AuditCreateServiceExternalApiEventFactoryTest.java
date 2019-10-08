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

package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;


public class AuditCreateServiceExternalApiEventFactoryTest {

    private AuditCreateServiceExternalApiEventFactory createTestSubject() {
        CommonAuditData.Builder newBuilder = CommonAuditData.newBuilder();
        CommonAuditData commonAuData = newBuilder.build();
        return new AuditCreateServiceExternalApiEventFactory(commonAuData, new ResourceCommonInfo(), new DistributionData("",""),"", new User());
    }

    @Test
    public void testGetLogMessage() throws Exception {
        String result;

        //defaul test
        AuditCreateServiceExternalApiEventFactory testSubject = createTestSubject();
        result = testSubject.getLogMessage();
    }
}

