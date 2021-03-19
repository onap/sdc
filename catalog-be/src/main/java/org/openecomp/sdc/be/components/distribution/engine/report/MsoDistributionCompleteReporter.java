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
package org.openecomp.sdc.be.components.distribution.engine.report;

import javax.annotation.Resource;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotification;
import org.openecomp.sdc.be.components.distribution.engine.rest.MSORestClient;
import org.springframework.stereotype.Component;

@Component
public class MsoDistributionCompleteReporter implements DistributionCompleteReporter {

    @Resource
    private MSORestClient msoClient;

    @Override
    public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
        msoClient.notifyDistributionComplete(distributionStatusNotification.getDistributionID(), distributionStatusNotification.getStatus(),
            distributionStatusNotification.getErrorReason());
    }
}
