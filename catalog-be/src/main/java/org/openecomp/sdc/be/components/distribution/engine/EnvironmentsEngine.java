package org.openecomp.sdc.be.components.distribution.engine;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.iterator.DME2EndpointIterator;
import com.att.aft.dme2.iterator.domain.DME2EndpointReference;
import com.att.aft.dme2.manager.registry.DME2Endpoint;
import com.att.nsa.apiClient.credentials.ApiCredential;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.DmaapActionEnum;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.OperationaEnvironmentTypeEnum;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.be.config.DmeConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.OperationalEnvInfo;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.runMethodWithTimeOut;

/**
 * Allows to consume DMAAP topic and handle received notifications
 */
@Service
public class EnvironmentsEngine implements INotificationHandler {

    private static final String MESSAGE_BUS = "MessageBus";
    private static final String UNKNOWN = "Unknown";
    private static final Logger log = LoggerFactory.getLogger(EnvironmentsEngine.class);
    private ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();

    private Map<String, OperationalEnvironmentEntry> environments;
    private Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
    private Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();
    private Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();

    private final DmaapConsumer dmaapConsumer;
    private final OperationalEnvironmentDao operationalEnvironmentDao;
    private final DME2EndpointIteratorCreator epIterCreator;
    private final AaiRequestHandler aaiRequestHandler;
    private final ComponentsUtils componentUtils;
    private final CambriaHandler cambriaHandler;
    private final DistributionEngineClusterHealth distributionEngineClusterHealth;
    private final DistributionCompleteReporter distributionCompleteReporter;

    public EnvironmentsEngine(DmaapConsumer dmaapConsumer, OperationalEnvironmentDao operationalEnvironmentDao, DME2EndpointIteratorCreator epIterCreator, AaiRequestHandler aaiRequestHandler, ComponentsUtils componentUtils, CambriaHandler cambriaHandler, DistributionEngineClusterHealth distributionEngineClusterHealth, DistributionCompleteReporter distributionCompleteReporter) {
        this.dmaapConsumer = dmaapConsumer;
        this.operationalEnvironmentDao = operationalEnvironmentDao;
        this.epIterCreator = epIterCreator;
        this.aaiRequestHandler = aaiRequestHandler;
        this.componentUtils = componentUtils;
        this.cambriaHandler = cambriaHandler;
        this.distributionEngineClusterHealth = distributionEngineClusterHealth;
        this.distributionCompleteReporter = distributionCompleteReporter;
    }

    @VisibleForTesting
    @PostConstruct
    void init() {
        log.trace("Environments engine has been initialized. ");
        try {
            environments = populateEnvironments();
            createUebTopicsForEnvironments();
            dmaapConsumer.consumeDmaapTopic(this::handleMessage,
                        (t, e) -> log.error("An error occurred upon consuming topic by Dmaap consumer client: ", e));
        }
        catch (Exception e) {
            log.error("An error occurred upon consuming topic by Dmaap consumer client." , e);
        }
    }
    public void connectUebTopicTenantIsolation(OperationalEnvironmentEntry opEnvEntry,
            AtomicBoolean status,
            Map<String, DistributionEngineInitTask> envNamePerInitTask, Map<String, DistributionEnginePollingTask> envNamePerPollingTask){
        connectUebTopic(opEnvEntry, status, envNamePerInitTask, envNamePerPollingTask);

    }

    public void connectUebTopicForDistributionConfTopic(String envName,
            AtomicBoolean status,
            Map<String, DistributionEngineInitTask> envNamePerInitTask, Map<String, DistributionEnginePollingTask> envNamePerPollingTask){
        connectUebTopic(environments.get(envName), status, envNamePerInitTask, envNamePerPollingTask);

    }
    /**
     * Allows to create and run UEB initializing and polling tasks
     * @param status
     * @param envNamePerInitTask
     * @param envNamePerPollingTask
     * @param opEnvEntry
     */
    private void connectUebTopic(OperationalEnvironmentEntry opEnvEntry, AtomicBoolean status,
            Map<String, DistributionEngineInitTask> envNamePerInitTask,
            Map<String, DistributionEnginePollingTask> envNamePerPollingTask) {

        String envId = opEnvEntry.getEnvironmentId();

        DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        DistributionEnginePollingTask distributionEnginePollingTask = new DistributionEnginePollingTask(
                distributionEngineConfiguration, distributionCompleteReporter, componentUtils, distributionEngineClusterHealth,
                opEnvEntry);
        String envName = configurationManager.getDistributionEngineConfiguration().getEnvironments().get(0);
        DistributionEngineInitTask distributionEngineInitTask = new DistributionEngineInitTask(0l,
                distributionEngineConfiguration, envName, status, componentUtils, distributionEnginePollingTask,
                opEnvEntry);
        distributionEngineInitTask.startTask();
        envNamePerInitTask.put(envId, distributionEngineInitTask);
        envNamePerPollingTask.put(envId, distributionEnginePollingTask);

        log.debug("Environment envId = {} has been connected to the UEB topic", envId);
    }

    @Override
    public boolean handleMessage(String notification) {
        DmaapConsumerConfiguration dmaapConsumerParams = ConfigurationManager.getConfigurationManager()
                .getConfiguration().getDmaapConsumerConfiguration();
        Supplier<Boolean> supplier = () -> handleMessageLogic(notification);
        Either<Boolean, Boolean> eitherTimeOut = runMethodWithTimeOut(supplier,
                dmaapConsumerParams.getTimeLimitForNotificationHandleMs());

        boolean result;
        if (eitherTimeOut.isRight()) {
            result = false;
        } else {
            result = eitherTimeOut.left().value();
        }
        return result;
    }

    public boolean handleMessageLogic(String notification) {
         Wrapper<Boolean> errorWrapper = new Wrapper<>();
        Wrapper<OperationalEnvironmentEntry> opEnvEntryWrapper = new Wrapper<>();
        try {

            log.debug("handle message - for operational environment notification received: {}", notification);
            Gson gsonObj = new GsonBuilder().create();

            IDmaapNotificationData notificationData = gsonObj.fromJson(notification,
                    DmaapNotificationDataImpl.class);
            IDmaapAuditNotificationData auditNotificationData = gsonObj.fromJson(notification,
                    DmaapNotificationDataImpl.class);

            AuditingActionEnum actionEnum;
            switch(notificationData.getAction()) {
                case CREATE:
                    actionEnum = AuditingActionEnum.CREATE_ENVIRONMENT;
                    break;
                case UPDATE:
                    actionEnum = AuditingActionEnum.UPDATE_ENVIRONMENT;
                    break;
                case DELETE:
                    actionEnum = AuditingActionEnum.DELETE_ENVIRONMENT;
                    break;
                default:
                    actionEnum = AuditingActionEnum.UNKNOWN_ENVIRONMENT_NOTIFICATION;
                    break;
            }
            componentUtils.auditEnvironmentEngine(actionEnum,
                    notificationData.getOperationalEnvironmentId(), notificationData.getOperationalEnvironmentType().getEventTypenName(),
                    notificationData.getAction().getActionName(), auditNotificationData.getOperationalEnvironmentName(),
                    auditNotificationData.getTenantContext());

            if (errorWrapper.isEmpty()) {
                validateNotification(errorWrapper, notificationData, auditNotificationData);
            }
            // Perform Save In-Progress Dao
            if (errorWrapper.isEmpty()) {
                saveEntryWithInProgressStatus(errorWrapper, opEnvEntryWrapper, notificationData);
            }

            if (errorWrapper.isEmpty()) {
                buildOpEnv(errorWrapper, opEnvEntryWrapper.getInnerElement());
            }

        } catch (Exception e) {
            log.debug("handle message for operational environmet failed for notification: {} with error :{}",
                    notification, e.getMessage(), e);
            errorWrapper.setInnerElement(false);

        }
        return errorWrapper.isEmpty();
    }

    private void validateNotification(Wrapper<Boolean> errorWrapper, IDmaapNotificationData notificationData,
                                                    IDmaapAuditNotificationData auditNotificationData) {
        // Check OperationaEnvironmentType
        if (errorWrapper.isEmpty()) {
            validateEnvironmentType(errorWrapper, notificationData, auditNotificationData);
        }
        // Check Action Type
        if (errorWrapper.isEmpty()) {
            validateActionType(errorWrapper, notificationData);
        }
        // Check is valid for create/update (not In-Progress state)
        if (errorWrapper.isEmpty()) {
            validateState(errorWrapper, notificationData);
        }
    }

    public void buildOpEnv(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        // Get Env Info From A&AI
        if (errorWrapper.isEmpty()) {
            retrieveOpEnvInfoFromAAI(errorWrapper, opEnvEntry);
        }

        if (errorWrapper.isEmpty()) {
            // Get List Of UEB Addresses From AFT_DME
            retrieveUebAddressesFromAftDme(errorWrapper, opEnvEntry);
        }

        // Create UEB keys and set them on EnvEntry
        if (errorWrapper.isEmpty()) {
            createUebKeys(errorWrapper, opEnvEntry);
        }

        // Create Topics
        if (errorWrapper.isEmpty()) {
            log.debug("handle message - Create Topics");
            createUebTopicsForEnvironment(opEnvEntry);
        }

        // Save Status Complete and Add to Map
        if (errorWrapper.isEmpty()) {
            saveEntryWithCompleteStatus(errorWrapper, opEnvEntry);
        }

        // Update Environments Map
        if (errorWrapper.isEmpty()) {
            environments.put(opEnvEntry.getEnvironmentId(), opEnvEntry);
        }
        else{
            saveEntryWithFailedStatus(errorWrapper, opEnvEntry);
        }
    }

    private void saveEntryWithFailedStatus(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        log.debug("handle message - save OperationalEnvironment Failed Status");
        opEnvEntry.setStatus(EnvironmentStatusEnum.FAILED);
        saveOpEnvEntry(errorWrapper, opEnvEntry);
    }

    void saveEntryWithCompleteStatus(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        log.debug("handle message - save OperationalEnvironment Complete Dao");
        opEnvEntry.setStatus(EnvironmentStatusEnum.COMPLETED);
        saveOpEnvEntry(errorWrapper, opEnvEntry);

    }

    void retrieveUebAddressesFromAftDme(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        log.debug("handle message - Get List Of UEB Addresses From AFT_DME");
        try {
            boolean isKeyFieldsValid = !isEmpty(opEnvEntry.getTenant()) && !isEmpty(opEnvEntry.getEcompWorkloadContext());
            if( isKeyFieldsValid ){
                String opEnvKey = map2OpEnvKey(opEnvEntry);
                String environmentId = opEnvEntry.getEnvironmentId();
                List<String> uebHosts = discoverUebHosts(opEnvKey, environmentId);
                opEnvEntry.setDmaapUebAddress(uebHosts.stream().collect(Collectors.toSet()));
            }
            else{
                errorWrapper.setInnerElement(false);
                log.debug("Can Not Build AFT DME Key from workLoad & Tenant Fields.");
            }

        } catch (DME2Exception e) {
            errorWrapper.setInnerElement(false);
            log.error("Failed to retrieve Ueb Addresses From DME. ", e);
        }
    }

    void createUebKeys(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        log.debug("handle message - Create UEB keys");
        List<String> discoverEndPoints = opEnvEntry.getDmaapUebAddress().stream()
                .collect(Collectors.toList());
        Either<ApiCredential, CambriaErrorResponse> eitherCreateUebKeys = cambriaHandler
                .createUebKeys(discoverEndPoints);
        if (eitherCreateUebKeys.isRight()) {
            errorWrapper.setInnerElement(false);
            log.debug("handle message - failed to create UEB Keys");
        } else {
            ApiCredential apiCredential = eitherCreateUebKeys.left().value();
            opEnvEntry.setUebApikey(apiCredential.getApiKey());
            opEnvEntry.setUebSecretKey(apiCredential.getApiSecret());
        }
    }

    void retrieveOpEnvInfoFromAAI(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry opEnvEntry) {
        log.debug("handle message - Get Env Info From A&AI");
        Either<OperationalEnvInfo, Integer> eitherOperationalEnvInfo = getOperationalEnvById(
                opEnvEntry.getEnvironmentId());
        if (eitherOperationalEnvInfo.isRight()) {
            errorWrapper.setInnerElement(false);
            log.debug("handle message - failed to retrieve details from A&AI");
        } else {
            OperationalEnvInfo operationalEnvInfo = eitherOperationalEnvInfo.left().value();
            opEnvEntry.setEcompWorkloadContext(operationalEnvInfo.getWorkloadContext());
            opEnvEntry.setTenant(operationalEnvInfo.getTenantContext());
        }
    }

    void saveEntryWithInProgressStatus(Wrapper<Boolean> errorWrapper, Wrapper<OperationalEnvironmentEntry> opEnvEntryWrapper, IDmaapNotificationData notificationData) {
        log.debug("handle message - save OperationalEnvironment In-Progress Dao");
        OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
        // Entry Environment ID holds actually the environment NAME
        opEnvEntry.setEnvironmentId(notificationData.getOperationalEnvironmentId());
        opEnvEntry.setStatus(EnvironmentStatusEnum.IN_PROGRESS);
        opEnvEntry.setIsProduction(false);
        saveOpEnvEntry(errorWrapper, opEnvEntry);
        opEnvEntryWrapper.setInnerElement(opEnvEntry);

    }


    void validateState(Wrapper<Boolean> errorWrapper, IDmaapNotificationData notificationData) {
        log.debug("handle message - verify OperationalEnvironment not In-Progress");
        String opEnvId = notificationData.getOperationalEnvironmentId();

        Either<OperationalEnvironmentEntry, CassandraOperationStatus> eitherOpEnv = operationalEnvironmentDao
                .get(opEnvId);
        if (eitherOpEnv.isLeft()) {
            final OperationalEnvironmentEntry opEnvEntry = eitherOpEnv.left().value();
            if (StringUtils.equals(opEnvEntry.getStatus(), EnvironmentStatusEnum.IN_PROGRESS.getName())) {
                errorWrapper.setInnerElement(false);
                log.debug("handle message - validate State Failed Record Found With Status : {} Flow Stopped!", opEnvEntry.getStatus());
            }
        } else {
            CassandraOperationStatus operationStatus = eitherOpEnv.right().value();
            if (operationStatus != CassandraOperationStatus.NOT_FOUND) {
                errorWrapper.setInnerElement(false);
                log.debug("failed to retrieve operationa environment with id:{} cassandra error was :{}", opEnvId,
                        operationStatus);
            }
        }

    }

    void validateActionType(Wrapper<Boolean> errorWrapper, IDmaapNotificationData notificationData) {
        log.debug("handle message - verify Action Type");
        DmaapActionEnum action = notificationData.getAction();
        if (action == DmaapActionEnum.DELETE) {
            errorWrapper.setInnerElement(false);
            log.debug("handle message - validate Action Type Failed With Action Type: {} Flow Stopped!", action);
        }
    }

    void validateEnvironmentType(Wrapper<Boolean> errorWrapper, IDmaapNotificationData notificationData,
                                 IDmaapAuditNotificationData auditNotificationData) {
        log.debug("handle message - verify OperationaEnvironmentType");
        OperationaEnvironmentTypeEnum envType = notificationData.getOperationalEnvironmentType();
        if (envType != OperationaEnvironmentTypeEnum.ECOMP) {
            errorWrapper.setInnerElement(false);
            log.debug("handle message - validate Environment Type Failed With Environment Type: {} Flow Stopped!", envType);
            componentUtils.auditEnvironmentEngine(AuditingActionEnum.UNSUPPORTED_ENVIRONMENT_TYPE,
                    notificationData.getOperationalEnvironmentId(), notificationData.getOperationalEnvironmentType().getEventTypenName(),
                    notificationData.getAction().getActionName(), auditNotificationData.getOperationalEnvironmentName(),
                    auditNotificationData.getTenantContext());
        }
    }


    private void saveOpEnvEntry(Wrapper<Boolean> errorWrapper, OperationalEnvironmentEntry entry) {
        entry.setLastModified(new Date(System.currentTimeMillis()));
        CassandraOperationStatus saveStaus = operationalEnvironmentDao.save(entry);
        if (saveStaus != CassandraOperationStatus.OK) {
            errorWrapper.setInnerElement(false);
            log.debug("handle message saving  operational environmet failed for id :{} with error : {}",
                    entry.getEnvironmentId(), saveStaus);
        }
    }

    public List<String> discoverUebHosts(String opEnvKey, String env) throws DME2Exception {
        DmeConfiguration dmeConfiguration = configurationManager.getConfiguration().getDmeConfiguration();
        List<String> uebHosts = new LinkedList<>();

        String lookupURI = String.format("http://%s/service=%s/version=1.0.0/envContext=%s/partner=*", dmeConfiguration.getDme2Search(), opEnvKey,
                env);
        DME2EndpointIterator iterator = epIterCreator.create(lookupURI);

        // Beginning iteration
        while (iterator.hasNext()) {
            DME2EndpointReference ref = iterator.next();
            DME2Endpoint dmeEndpoint = ref.getEndpoint();
            log.debug("DME returns EP with UEB host {}, UEB port: {}", dmeEndpoint.getHost(), dmeEndpoint.getPort());
            uebHosts.add(dmeEndpoint.getHost());
        }

        return uebHosts;
    }

    private String map2OpEnvKey(OperationalEnvironmentEntry entry) {
        return String.format("%s.%s.%s", entry.getTenant(), entry.getEcompWorkloadContext(), MESSAGE_BUS);
    }

    private Map<String, OperationalEnvironmentEntry> populateEnvironments() {
        Map<String, OperationalEnvironmentEntry> envs = getEnvironmentsFromDb();
        OperationalEnvironmentEntry confEntry = readEnvFromConfig();
        envs.put(confEntry.getEnvironmentId(), confEntry);
        return envs;
    }

    private OperationalEnvironmentEntry readEnvFromConfig() {
        OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
        DistributionEngineConfiguration distributionEngineConfiguration = configurationManager
                .getDistributionEngineConfiguration();
        entry.setUebApikey(distributionEngineConfiguration.getUebPublicKey());
        entry.setUebSecretKey(distributionEngineConfiguration.getUebSecretKey());

        Set<String> puebEndpoints = new HashSet<>();
        puebEndpoints.addAll(distributionEngineConfiguration.getUebServers());
        entry.setDmaapUebAddress(puebEndpoints);

        String envName = distributionEngineConfiguration.getEnvironments().size() == 1
                ? distributionEngineConfiguration.getEnvironments().get(0) : UNKNOWN;
        entry.setEnvironmentId(envName);

        if(log.isDebugEnabled()) {
            log.debug("Enviroment read from configuration: {}", entry.toString());
        }

        return entry;
    }

    private Map<String, OperationalEnvironmentEntry> getEnvironmentsFromDb() {
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> opEnvResult = operationalEnvironmentDao
                .getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED);

        if (opEnvResult.isLeft()) {
            Map<String, OperationalEnvironmentEntry> resultMap =  opEnvResult.left().value().stream()
                    .collect(Collectors.toMap(OperationalEnvironmentEntry::getEnvironmentId, Function.identity()));
            resultMap.forEach( (key, value) -> log.debug("Enviroment loaded from DB: {}", value.toString()) );
            return resultMap;
        } else {
            CassandraOperationStatus status = opEnvResult.right().value();
            log.debug("Failed to populate Operation Envirenments Map from Cassandra, DB status: {}", status);
            return new HashMap<>();
        }
    }

    void createUebTopicsForEnvironments() {
        environments.values().forEach(this::createUebTopicsForEnvironment);
    }

    public void createUebTopicsForEnvironment(OperationalEnvironmentEntry opEnvEntry) {
        String envId = opEnvEntry.getEnvironmentId();
        log.debug("Create Environment {} on UEB Topic.", envId);
        AtomicBoolean status = new AtomicBoolean(false);
        envNamePerStatus.put(envId, status);

        connectUebTopicTenantIsolation(opEnvEntry, status, envNamePerInitTask, envNamePerPollingTask);
    }

    @VisibleForTesting
    void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public Map<String, OperationalEnvironmentEntry> getEnvironments() {
        return environments;
    }


    public Either<OperationalEnvInfo, Integer> getOperationalEnvById(String id) {
        HttpResponse<String> resp = aaiRequestHandler.getOperationalEnvById(id);
        if (resp.getStatusCode() == HttpStatus.SC_OK) {
            try {
                OperationalEnvInfo operationalEnvInfo = OperationalEnvInfo.createFromJson(resp.getResponse());

                log.debug("Get \"{}\" operational environment. {}", id, operationalEnvInfo);
                return Either.left(operationalEnvInfo);
            } catch (Exception e) {
                log.debug("Json convert to OperationalEnvInfo failed with exception ", e);
                return Either.right(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            log.debug("Get \"{}\" operational environment failed with statusCode: {}, description: {}", id,
                    resp.getStatusCode(), resp.getDescription());
            return Either.right(resp.getStatusCode());
        }
    }

    public OperationalEnvironmentEntry getEnvironmentById (String envId) {
        return environments.get(envId);
    }

    public boolean isInMap(OperationalEnvironmentEntry env) {
        return isInMap(env.getEnvironmentId());
    }

    public boolean isInMap(String envId) {
        return environments.containsKey(envId);
    }

    public void addToMap(OperationalEnvironmentEntry opEnvEntry) {
        environments.put(opEnvEntry.getEnvironmentId(), opEnvEntry);

    }
}
