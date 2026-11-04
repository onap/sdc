/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.distribution;

import fj.data.Either;
import org.openecomp.sdc.be.components.distribution.engine.DistributionTopics;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.KafkaDataResponse;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component("distributionBusinessLogic")
public class DistributionBusinessLogic {

    private ResponseFormatManager responseFormatManager = ResponseFormatManager.getInstance();

    public Either<KafkaDataResponse, ResponseFormat> getKafkaData() {
        DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager()
            .getDistributionEngineConfiguration();
        String bootStrapServers = distributionEngineConfiguration.getKafkaBootStrapServers();
        if (bootStrapServers != null) {
            String statusTopicName = DistributionTopics
                .buildTopicName(distributionEngineConfiguration.getDistributionStatusTopicName(), distributionEngineConfiguration.getEnvironments().get(0));
            String notificationTopicName = DistributionTopics
                .buildTopicName(distributionEngineConfiguration.getDistributionNotifTopicName(), distributionEngineConfiguration.getEnvironments().get(0));
            KafkaDataResponse kafkaDataResponse = new KafkaDataResponse();
            kafkaDataResponse.setKafkaBootStrapServer(bootStrapServers);
            kafkaDataResponse.setDistrStatusTopicName(statusTopicName);
            kafkaDataResponse.setDistrNotificationTopicName(notificationTopicName);
            return Either.left(kafkaDataResponse);
        } else {
            ResponseFormat errorResponseWrapper = getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return Either.right(errorResponseWrapper);
        }
    }

    public ResponseFormatManager getResponseFormatManager() {
        return responseFormatManager;
    }
}
