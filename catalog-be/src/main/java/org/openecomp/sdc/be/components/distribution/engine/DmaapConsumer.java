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

package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.mr.client.MRConsumer;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Allows consuming DMAAP topic according to received consumer parameters
 * Allows processing received messages.
 */
@Service
public class DmaapConsumer {
    private static final String LOG_PARTNER_NAME = "SDC.BE";
    private static final Logger logger = Logger.getLogger(DmaapClientFactory.class.getName());
    private final ExecutorFactory executorFactory;
    private final DmaapClientFactory dmaapClientFactory;
    private final DmaapHealth dmaapHealth;
    private static LogFieldsMdcHandler mdcFieldsHandler = new LogFieldsMdcHandler();
    /**
     * Allows to create an object of type DmaapConsumer
     * @param executorFactory
     * @param dmaapClientFactory
     * @param dmaapHealth
     */
    @Autowired
    public DmaapConsumer(ExecutorFactory executorFactory, DmaapClientFactory dmaapClientFactory,
        DmaapHealth dmaapHealth) {
        this.executorFactory = executorFactory;
        this.dmaapClientFactory = dmaapClientFactory;
        this.dmaapHealth = dmaapHealth;
    }

    /**
     * Allows consuming DMAAP topic according to received consumer parameters
     * @param notificationReceived
     * @param exceptionHandler
     * @throws Exception
     */
    public void consumeDmaapTopic(Consumer<String> notificationReceived, UncaughtExceptionHandler exceptionHandler) throws Exception {

        DmaapConsumerConfiguration dmaapConsumerParams = ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapConsumerConfiguration();
        String topic = dmaapConsumerParams.getTopic();
        logger.info("Starting to consume topic {} for DMAAP consumer with the next parameters {}. ", topic, dmaapConsumerParams);
        MRConsumer consumer = dmaapClientFactory.create(dmaapConsumerParams);
        ScheduledExecutorService pollExecutor = executorFactory.createScheduled(topic + "Client");
        ExecutorService notificationExecutor = executorFactory.create(topic + "Consumer", exceptionHandler);

        pollExecutor.scheduleWithFixedDelay(() -> {
            logger.info("Trying to fetch messages from topic: {}", topic);
            boolean isTopicAvailable = false;
            mdcFieldsHandler.addInfoForErrorAndDebugLogging(LOG_PARTNER_NAME);
            try {
                Iterable<String> messages = consumer.fetch();
                isTopicAvailable = true ;
                if (messages != null) {
                    for (String msg : messages) {
                        logger.info("The DMAAP message {} received. The topic is {}.", msg, topic);
                        notificationExecutor.execute(() -> notificationReceived.accept(msg));
                    }
                }
                //successfully fetched
            }
            catch (Exception e) {
                logger.error("The exception occurred upon fetching DMAAP message", e);
            }
            dmaapHealth.report( isTopicAvailable );
        }, 0L, dmaapConsumerParams.getPollingInterval(), TimeUnit.SECONDS);
    }

}
