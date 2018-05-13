package org.openecomp.sdc.be.components.distribution.engine;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_DMAAP_ENGINE;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.utils.URIUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("dmaapHealth")
public class DmaapHealth {


    protected static final String DMAAP_HEALTH_LOG_CONTEXT = "dmaap.healthcheck";
    private static final String DMAAP_HEALTH_CHECK_STR = "dmaapHealthCheck";
    private static final Logger log = LoggerFactory.getLogger(DmaapHealth.class);
    private static final Logger logHealth = LoggerFactory.getLogger(DMAAP_HEALTH_LOG_CONTEXT);
    private HealthCheckInfo healthCheckInfo = DmaapHealth.HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo();
    private long healthCheckReadTimeout = 20;
    private long reconnectInterval = 5;
    private HealthCheckScheduledTask healthCheckScheduledTask = null ;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture = null;
    private DmaapConsumerConfiguration configuration = null ;

    private volatile AtomicBoolean lastHealthState = new AtomicBoolean(false);
    private volatile AtomicBoolean reportedHealthState = null;

    public enum HealthCheckInfoResult {
        OK(new HealthCheckInfo(HC_COMPONENT_DMAAP_ENGINE, HealthCheckInfo.HealthCheckStatus.UP, null, DmaapStatusDescription.OK.getDescription())),
        UNAVAILABLE(new HealthCheckInfo(HC_COMPONENT_DMAAP_ENGINE, HealthCheckInfo.HealthCheckStatus.DOWN, null, DmaapStatusDescription.UNAVAILABLE.getDescription())),
        DOWN(new HealthCheckInfo(HC_COMPONENT_DMAAP_ENGINE, HealthCheckInfo.HealthCheckStatus.DOWN, null, DmaapStatusDescription.DOWN.getDescription()));

        private HealthCheckInfo healthCheckInfo;
        HealthCheckInfoResult(HealthCheckInfo healthCheckInfo) {
            this.healthCheckInfo = healthCheckInfo;
        }
        public HealthCheckInfo getHealthCheckInfo() {
            return healthCheckInfo;
        }
    }

    public enum DmaapStatusDescription {
        OK("OK"), UNAVAILABLE("Dmaap is not available"),DOWN("DOWN"), NOT_CONFIGURED("Dmaap configuration is missing/wrong ");

        private String desc;
        DmaapStatusDescription(String desc) {
            this.desc = desc;
        }
        public String getDescription() {
            return desc;
        }

    }

    @PostConstruct
    public DmaapHealth init() {
        log.trace("Enter init method of Dmaap health");
        synchronized (DmaapHealth.class){
            this.configuration = ConfigurationManager.getConfigurationManager().getConfiguration().getDmaapConsumerConfiguration();

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
    public void startHealthCheckTask( boolean startTask ) {
        synchronized (DmaapHealth.class){
            if (startTask && this.scheduledFuture == null) {
                this.scheduledFuture = this.scheduler.scheduleAtFixedRate(this.healthCheckScheduledTask , 0, reconnectInterval, TimeUnit.SECONDS);
            }
        }
    }

    public void report(Boolean isUp){
        if (reportedHealthState == null)
            reportedHealthState = new AtomicBoolean(isUp);
        reportedHealthState.set(isUp);
    }

    public void logAlarm(boolean lastHealthState) {
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

    public DmaapConsumerConfiguration getConfiguration() {
        return configuration;
    }

    public HealthCheckInfo getHealthCheckInfo() {
        return healthCheckInfo;
    }

    /**
     * Health Check Task Scheduler - infinite check.
     */
    public class HealthCheckScheduledTask implements Runnable {
        private final DmaapConsumerConfiguration config;
        private static final int timeout = 8192;

        public HealthCheckScheduledTask(final DmaapConsumerConfiguration config){
            this.config = config;
        }
        @Override
        public void run() {
            logHealth.trace("Executing Dmaap Health Check Task - Start");
            boolean prevIsReachable = false;
            boolean reachable = false;
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
                log.debug("{} | cannot check connectivity -> {}",DMAAP_HEALTH_CHECK_STR, e );
                prevIsReachable = lastHealthState.getAndSet(false);
                healthCheckInfo = HealthCheckInfoResult.UNAVAILABLE.healthCheckInfo;
            }
            if (prevIsReachable != lastHealthState.get())
                logAlarm( lastHealthState.get() );
        }


        /**
         * @deprecated (health is reported outside from EnvironmentEngine consumer fetch)
         */
        @Deprecated
        public boolean isICMPReachable( ) throws IOException{
            try{
                String hostname = getUrlHost(config.getHosts());
                return InetAddress.getByName( hostname ).isReachable(timeout);
            }catch( URISyntaxException e ){
                log.debug("{} | malformed host configuration -> ",DMAAP_HEALTH_CHECK_STR , e);
            }
            return false;
        }
    }

    public static String getUrlHost(String qualifiedHost) throws URISyntaxException{
        //region - parse complex format ex. <http://URL:PORT>
        try{
            UrlValidator validator = new UrlValidator();
            if (validator.isValid(qualifiedHost)){
                return URIUtils.extractHost(new URI(qualifiedHost)).getHostName();
            }else{
                log.debug("{} | invalid url format, continuing ", DMAAP_HEALTH_CHECK_STR );
            }
        }catch(URISyntaxException e){
            log.debug("{} | invalid url format, continuing {} ", DMAAP_HEALTH_CHECK_STR , e);
        }
        //endregion

        //region - try shortcut format <URL> or <URL:PORT>
        if ( countMatches( qualifiedHost , ":") <= 1){
            String[] address = qualifiedHost.split(":");
            if ( address.length>0 && isNotBlank(address[0]) ){
                return address[0];
            }
        }
        //endregion
        throw new URISyntaxException( qualifiedHost , "invalid hostname, expecting a single <host:port> ,  (valid ex. www.google.com:80 | www.google.com | http:\\\\www.google.com:8181)");
    }
}
