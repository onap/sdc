package org.openecomp.sdc.common.ecomplog;

import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ELAPSED_TIME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_END_TIMESTAMP;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.openecomp.sdc.common.ecomplog.api.IStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by dd4296 on 12/13/2017.
 * this is local implementation of the stopwatch class from EELF standard with the same interface
 * can be replaced if needed with EELF lib
 */
public class Stopwatch implements IStopWatch {

    private static Logger log = LoggerFactory.getLogger(Stopwatch.class.getName());

    public Stopwatch() {
    }

    public void start() {
        if (MDC.get(MDC_BEGIN_TIMESTAMP) == null || MDC.get(MDC_BEGIN_TIMESTAMP).trim().length() == 0)
            MDC.put(MDC_BEGIN_TIMESTAMP, generatedTimeNow());
    }

    public void stop() {
        if (MDC.get(MDC_BEGIN_TIMESTAMP) == null) {
            log.error("call to stop without calling start first, this is not compliant with EELF format");
        }
        MDC.put(MDC_END_TIMESTAMP, generatedTimeNow());
        setElapsedTime();
    }

    private void setElapsedTime() {

        try {

            final LocalDateTime startTime = LocalDateTime.parse(MDC.get(MDC_BEGIN_TIMESTAMP));
            final LocalDateTime endTime = LocalDateTime.parse(MDC.get(MDC_END_TIMESTAMP));

            final Duration timeDifference = Duration.between(startTime, endTime);

            MDC.put(MDC_ELAPSED_TIME, String.valueOf(timeDifference.toMillis()));

        } catch(Exception ex) {
            log.error("failed to calculate elapsed time",ex);
        }
    }

    private String generatedTimeNow() {
        return String.valueOf(LocalDateTime.now(Clock.systemUTC()));
    }

}