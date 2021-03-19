/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.catalog.impl;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.openecomp.sdc.be.catalog.api.IMessageQueueHandlerProducer;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.api.ITypeMessage;
import org.openecomp.sdc.be.catalog.enums.ResultStatusEnum;
import org.openecomp.sdc.be.components.distribution.engine.DmaapClientFactory;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapProducerConfiguration;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component
public class DmaapProducer implements IMessageQueueHandlerProducer {

    private static final Logger LOG = Logger.getLogger(DmaapProducer.class.getName());
    private final DmaapClientFactory dmaapClientFactory;
    private final DmaapProducerHealth dmaapHealth;
    private final ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();
    private MRBatchingPublisher publisher;

    public DmaapProducer(final DmaapClientFactory dmaapClientFactory, final DmaapProducerHealth dmaapHealth) {
        this.dmaapClientFactory = dmaapClientFactory;
        this.dmaapHealth = dmaapHealth;
    }

    @Override
    public IStatus pushMessage(ITypeMessage message) {
        try {
            DmaapProducerConfiguration producerConfiguration = configurationManager.getConfiguration().getDmaapProducerConfiguration();
            if (!producerConfiguration.getActive()) {
                LOG.info("[Microservice DMAAP] producer is disabled [re-enable in configuration->isActive],message not sent.");
                dmaapHealth.report(false);
                return IStatus.getServiceDisabled();
            }
            if (publisher == null) {
                IStatus initStatus = init();
                if (initStatus.getResultStatus() != ResultStatusEnum.SUCCESS) {
                    return initStatus;
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(message);
            if (publisher != null) {
                LOG.info("before send message . response {}", jsonInString);
                LOG.invoke("Dmaap Producer", "DmaapProducer-pushMessage", DmaapProducer.class.getName(), message.toString());
                int pendingMsg = publisher.send(jsonInString);
                LOG.info("sent message . response {}", pendingMsg);
                LOG.invokeReturn(producerConfiguration.getConsumerId(), "Dmaap Producer", StatusCode.COMPLETE.getStatusCode(),
                    "DmaapProducer-pushMessage", message.toString(), pendingMsg);
            }
            dmaapHealth.report(true);
        } catch (Exception e) {
            LOG.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, "Failed to send message . Exception {}", e.getMessage());
            return IStatus.getFailStatus();
        }
        return IStatus.getSuccessStatus();
    }

    @PostConstruct
    @Override
    public IStatus init() {
        LOG.debug("MessageQueueHandlerProducer:: Start initializing");
        DmaapProducerConfiguration configuration = configurationManager.getConfiguration().getDmaapProducerConfiguration();
        if (configuration.getActive()) {
            try {
                publisher = dmaapClientFactory.createProducer(configuration);
                if (publisher == null) {
                    LOG.error("Failed to connect to topic ");
                    dmaapHealth.report(false);
                    return IStatus.getFailStatus();
                }
            } catch (Exception e) {
                LOG.error("Failed to connect to topic . Exeption {}", e.getMessage());
                dmaapHealth.report(false);
                return IStatus.getFailStatus();
            }
            dmaapHealth.report(true);
            return IStatus.getSuccessStatus();
        }
        LOG.info("[Microservice DMAAP] producer is disabled [re-enable in configuration->isActive],message not sent.");
        dmaapHealth.report(false);
        return IStatus.getServiceDisabled();
    }

    @PreDestroy
    public void shutdown() {
        LOG.debug("DmaapProducer::shutdown...");
        try {
            if (publisher != null) {
                publisher.close();
            }
        } catch (Exception e) {
            LOG.error("Failed to close  messageQ . Exeption {}", e.getMessage());
        }
    }
}
