package org.openecomp.sdc.be.catalog.impl;





import org.openecomp.sdc.common.api.Constants;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.utils.URIUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.be.config.DmaapProducerConfiguration;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_DMAAP_ENGINE;

@Component("dmaapProducerHealth")
public class DmaapProducerHealth {


    private static final String DMAAP_HEALTH_LOG_CONTEXT = "dmaapProducer.healthcheck";
    private static final String DMAAP_HEALTH_CHECK_STR = "dmaapProducerHealthCheck";
    private static final Logger log = Logger.getLogger(DmaapProducerHealth.class.getName());
    private static final Logger logHealth = Logger.getLogger(DMAAP_HEALTH_LOG_CONTEXT);
    private HealthCheckInfo healthCheckInfo = DmaapProducerHealth.HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo();
    private long healthCheckReadTimeout = 20;
    private long reconnectInterval = 5;
    private HealthCheckScheduledTask healthCheckScheduledTask = null ;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture = null;
    private DmaapProducerConfiguration configuration = null ;

    private volatile AtomicBoolean lastHealthState = new AtomicBoolean(false);
    private volatile AtomicBoolean reportedHealthState = null;

    public enum HealthCheckInfoResult {
        OK(new HealthCheckInfo(Constants.HC_COMPONENT_DMAAP_PRODUCER, HealthCheckInfo.HealthCheckStatus.UP, null, DmaapStatusDescription.OK.getDescription())),
        UNAVAILABLE(new HealthCheckInfo(Constants.HC_COMPONENT_DMAAP_PRODUCER, HealthCheckInfo.HealthCheckStatus.DOWN, null, DmaapStatusDescription.UNAVAILABLE.getDescription())),
        DOWN(new HealthCheckInfo(Constants.HC_COMPONENT_DMAAP_PRODUCER, HealthCheckInfo.HealthCheckStatus.DOWN, null, DmaapStatusDescription.DOWN.getDescription()));

        private HealthCheckInfo healthCheckInfo;
        HealthCheckInfoResult(HealthCheckInfo healthCheckInfo) {
            this.healthCheckInfo = healthCheckInfo;
        }
        public HealthCheckInfo getHealthCheckInfo() {
            return healthCheckInfo;
        }
    }

    public enum DmaapStatusDescription {
        OK("OK"), UNAVAILABLE("DmaapProducer is not available"),DOWN("DOWN"), NOT_CONFIGURED("DmaapProducer configuration is missing/wrong ");

        private String desc;
        DmaapStatusDescription(String desc) {
            this.desc = desc;
        }
        public String getDescription() {
            return desc;
        }

    }

    @PostConstruct
    public DmaapProducerHealth init() {
        log.trace("Enter init method of DmaapProducer health");
        synchronized (DmaapProducerHealth.class){
            this.configuration = ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapProducerConfiguration();

            Integer pollingInterval = configuration.getPollingInterval();
            if (pollingInterval != null && pollingInterval!=0) {
                reconnectInterval = pollingInterval;
            }
            Integer healthCheckReadTimeoutConfig = configuration.getTimeoutMs();
            if (healthCheckReadTimeoutConfig != null) {
                this.healthCheckReadTimeout = healthCheckReadTimeoutConfig;
            }
            this.healthCheckScheduledTask = new HealthCheckScheduledTask( configuration ); //what is the representation? csv? delimiter? json or other
            startHealthCheckTask(true);
        }
        log.trace("Exit init method of DistributionEngineClusterHealth");
        return this;
    }

    @PreDestroy
    protected void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Start health check task.
     *
     * @param startTask
     */
    private void startHealthCheckTask(boolean startTask) {
        synchronized (DmaapProducerHealth.class){
            if (startTask && this.scheduledFuture == null) {
                this.scheduledFuture = this.scheduler.scheduleAtFixedRate(this.healthCheckScheduledTask , 0, reconnectInterval, TimeUnit.SECONDS);
            }
        }
    }

    void report(Boolean isUp){
        if (reportedHealthState == null)
            reportedHealthState = new AtomicBoolean(isUp);
        reportedHealthState.set(isUp);
    }


    public HealthCheckInfo getHealthCheckInfo() {
        return healthCheckInfo;
    }

    /**
     * Health Check Task Scheduler - infinite check.
     */
    public class HealthCheckScheduledTask implements Runnable {
        private final DmaapProducerConfiguration config;
        private static final int TIMEOUT = 8192;

        HealthCheckScheduledTask(final DmaapProducerConfiguration config){
            this.config = config;
        }
        @Override
        public void run() {
            logHealth.trace("Executing Dmaap Health Check Task - Start");
            boolean prevIsReachable;
            boolean reachable;
            //first try simple ping
            try{
                if ( reportedHealthState != null ){
                    reachable = reportedHealthState.get();
                }
                else{
                    reachable =  false;
                }
                prevIsReachable = lastHealthState.getAndSet( reachable );
                healthCheckInfo = reachable ? HealthCheckInfoResult.OK.healthCheckInfo : HealthCheckInfoResult.DOWN.healthCheckInfo;
            }
            catch( Exception e ){
                log.debug("{} - cannot check connectivity -> {}",DMAAP_HEALTH_CHECK_STR, e );
                prevIsReachable = lastHealthState.getAndSet(false);
                healthCheckInfo = HealthCheckInfoResult.UNAVAILABLE.healthCheckInfo;
            }
            if (prevIsReachable != lastHealthState.get())
                logAlarm( lastHealthState.get() );
        }


       

        private void logAlarm(boolean lastHealthState) {
            try{
                if ( lastHealthState ) {
                    BeEcompErrorManager.getInstance().logDmaapHealthCheckRecovery( DMAAP_HEALTH_CHECK_STR );
                } else {
                    BeEcompErrorManager.getInstance().logDmaapHealthCheckError( DMAAP_HEALTH_CHECK_STR );
                }
            }catch( Exception e ){
                log.debug("cannot logAlarm -> {}" ,e );
            }
        }

    }

   
}
