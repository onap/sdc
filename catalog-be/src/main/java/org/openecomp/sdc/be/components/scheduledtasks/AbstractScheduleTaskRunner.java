package org.openecomp.sdc.be.components.scheduledtasks;

import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public abstract class AbstractScheduleTaskRunner {
    private static final Logger log = Logger.getLogger(AbstractScheduleTaskRunner.class);
    public abstract ExecutorService getExecutorService();

    protected void shutdownExecutor() {
        ExecutorService executorService = getExecutorService();
        if (executorService == null)
            return;

        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing
                                                // tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    log.debug("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
