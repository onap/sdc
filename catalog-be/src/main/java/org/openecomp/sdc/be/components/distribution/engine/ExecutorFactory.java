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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("executorFactory")
/**
 * Allows to create next kinds of single thread executors: SingleThreadExecutor and SingleThreadScheduledExecutor
 */
public class ExecutorFactory {

    private static final Logger logger = Logger.getLogger(EnvironmentsEngine.class.getName());

    public ExecutorService create(String name, UncaughtExceptionHandler exceptionHandler) {
        logger.info("Going to create single thread executor. ");
        ThreadFactory threadFactory = createThreadFactory(name, exceptionHandler);
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    public ScheduledExecutorService createScheduled(String name) {
        logger.info("Going to create single thread scheduled executor. ");
        ThreadFactory threadFactory = createThreadFactory(name,
            (t, e) -> LoggerFactory.getLogger(UncaughtExceptionHandler.class).error("An error occurred: ", e));
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    private ThreadFactory createThreadFactory(String name, UncaughtExceptionHandler exceptionHandler) {
        String nameFormat = name + "-%d";
        return new ThreadFactoryBuilder().setThreadFactory(Executors.defaultThreadFactory()).setNameFormat(nameFormat)
            .setUncaughtExceptionHandler(exceptionHandler).setDaemon(true).build();
    }
}
