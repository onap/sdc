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

package org.openecomp.sdc.be.components.scheduledtasks;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.CleanComponentsConfiguration;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component("asdcComponentsCleaner")
public class AsdcComponentsCleanerTask extends AbstractScheduleTaskRunner implements Runnable {

    private static final Logger log = Logger.getLogger(AsdcComponentsCleanerTask.class);

    @javax.annotation.Resource
    private ComponentsCleanBusinessLogic componentsCleanBusinessLogic = null;

    private List<NodeTypeEnum> componentsToClean;
    private long cleaningIntervalInMinutes;

    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1,
            new BasicThreadFactory.Builder().namingPattern("ComponentsCleanThread-%d").build());
    ScheduledFuture<?> scheduledFuture = null;

    @PostConstruct
    public void init() {
        log.info("Enter init method of AsdcComponentsCleaner");
        Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
        CleanComponentsConfiguration cleanComponentsConfiguration = configuration.getCleanComponentsConfiguration();

        if (cleanComponentsConfiguration == null) {
            log.info("ERROR - configuration is not valid!!! missing cleanComponentsConfiguration");
            BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-init",
                    "fecth configuration");
            return;

        }
        componentsToClean = new ArrayList<>();
        List<String> components = cleanComponentsConfiguration.getComponentsToClean();
        if (components == null) {
            log.info("no component were configured for cleaning");
        }
        for (String component : components) {
            NodeTypeEnum typeEnum = NodeTypeEnum.getByNameIgnoreCase(component);
            if (typeEnum != null)
                componentsToClean.add(typeEnum);
        }

        long intervalInMinutes = cleanComponentsConfiguration.getCleanIntervalInMinutes();

        if (intervalInMinutes < 1) {
            log.warn("cleaningIntervalInMinutes value should be greater than or equal to 1 minute. use default");
            intervalInMinutes = 60;
        }
        cleaningIntervalInMinutes = intervalInMinutes;

        startTask();

        log.info("End init method of AsdcComponentsCleaner");
    }

    @PreDestroy
    public void destroy() {
        this.stopTask();
        shutdownExecutor();
    }

    public void startTask() {

        log.debug("start task for cleaning components");

        try {

            if (scheduledService != null) {
                log.debug("Start Cleaning components task. interval {} minutes", cleaningIntervalInMinutes);
                scheduledFuture = scheduledService.scheduleAtFixedRate(this, 5, cleaningIntervalInMinutes,
                        TimeUnit.MINUTES);

            }
        } catch (Exception e) {
            log.debug("unexpected error occured", e);
            BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-startTask",
                    e.getMessage());

        }
    }

    private void stopTask() {
        if (scheduledFuture != null) {
            boolean cancelTaskSuccessfully = scheduledFuture.cancel(true);
            log.debug("Stop cleaning task. result = {}", cancelTaskSuccessfully);
            if (!cancelTaskSuccessfully) {
                BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-stopTask",
                        "try to stop the polling task");
            }
            scheduledFuture = null;
        }

    }

    @Override
    public void run() {
        try {
            componentsCleanBusinessLogic.cleanComponents(componentsToClean);
        } catch (Exception e) {
            log.error("unexpected error occured", e);
            BeEcompErrorManager.getInstance().logBeComponentCleanerSystemError("AsdcComponentsCleanerTask-run",
                    e.getMessage());
        }

    }

    @Override
    public ExecutorService getExecutorService() {
        return scheduledService;
    }
}
