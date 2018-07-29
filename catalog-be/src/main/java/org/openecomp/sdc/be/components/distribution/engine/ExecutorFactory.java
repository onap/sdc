package org.openecomp.sdc.be.components.distribution.engine;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
@Component("executorFactory")
/**
 * Allows to create next kinds of single thread executors: SingleThreadExecutor and SingleThreadScheduledExecutor
 */
public class ExecutorFactory {

    private static final Logger logger = Logger.getLogger(EnvironmentsEngine.class.getName());

    public ExecutorService create(String name, UncaughtExceptionHandler exceptionHandler){
        logger.info("Going to create single thread executor. ");
        ThreadFactory threadFactory = createThreadFactory(name, exceptionHandler);
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    public ScheduledExecutorService createScheduled(String name){
        logger.info("Going to create single thread scheduled executor. ");
        ThreadFactory threadFactory = createThreadFactory(name,
                (t, e) -> LoggerFactory.getLogger(UncaughtExceptionHandler.class).error("An error occurred: ", e));
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    private ThreadFactory createThreadFactory(String name, UncaughtExceptionHandler exceptionHandler) {
        String nameFormat = name + "-%d";
        return new ThreadFactoryBuilder()
                .setThreadFactory(Executors.defaultThreadFactory())
                .setNameFormat(nameFormat)
                .setUncaughtExceptionHandler(exceptionHandler)
                .setDaemon(true)
                .build();
    }
}
