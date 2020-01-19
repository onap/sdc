package org.openecomp.sdc.fe.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.elements.ErrorLogOptionalData;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.util.HealthCheckUtil;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_CATALOG_FACADE_MS;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_DCAE;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_ON_BOARDING;

public class HealthCheckScheduledTask implements Runnable {
    private static final Logger healthLogger = Logger.getLogger("asdc.fe.healthcheck");
    private static final Logger log = Logger.getLogger(HealthCheckScheduledTask.class.getName());
    private static final String LOG_PARTNER_NAME = "SDC.FE";
    private static final String LOG_TARGET_ENTITY_BE = "SDC.BE";
    private static final String LOG_TARGET_ENTITY_CONFIG = "SDC.FE.Configuration";
    private static final String LOG_TARGET_SERVICE_NAME_OB = "getOnboardingConfig";
    private static final String LOG_TARGET_SERVICE_NAME_DCAE = "getDCAEConfig";
    private static final String LOG_TARGET_SERVICE_NAME_FACADE = "getCatalogFacadeConfig";
    private static final String LOG_SERVICE_NAME = "/rest/healthCheck";
    private static LogFieldsMdcHandler mdcFieldsHandler = new LogFieldsMdcHandler();

    private static final String URL = "%s://%s:%s/sdc2/rest/healthCheck";

    private final List<String> healthCheckFeComponents =
            Arrays.asList(HC_COMPONENT_ON_BOARDING, HC_COMPONENT_DCAE, HC_COMPONENT_CATALOG_FACADE_MS);
    private static final HealthCheckUtil healthCheckUtil = new HealthCheckUtil();
    private static final String DEBUG_CONTEXT = "HEALTH_FE";
    private static final String EXTERNAL_HC_URL = "%s://%s:%s%s";
    private static String ONBOARDING_HC_URL;
    private static String DCAE_HC_URL;
    private static String CATALOG_FACADE_MS_HC_URL;

    private final HealthCheckService service;

    HealthCheckScheduledTask(HealthCheckService service) {
        this.service = service;
    }

    static String getOnboardingHcUrl() {
        return ONBOARDING_HC_URL;
    }

    static String getDcaeHcUrl() {
        return DCAE_HC_URL;
    }

    static String getCatalogFacadeMsHcUrl() {
        return CATALOG_FACADE_MS_HC_URL;
    }


    @Override
    public void run() {
        mdcFieldsHandler.addInfoForErrorAndDebugLogging(LOG_PARTNER_NAME);
        healthLogger.trace("Executing FE Health Check Task - Start");
        HealthCheckService.HealthStatus currentHealth = checkHealth();
        int currentHealthStatus = currentHealth.getStatusCode();
        healthLogger.trace("Executing FE Health Check Task - Status = {}", currentHealthStatus);

        // In case health status was changed, issue alarm/recovery
        if (currentHealthStatus != service.getLastHealthStatus().getStatusCode()) {
            log.trace("FE Health State Changed to {}. Issuing alarm / recovery alarm...", currentHealthStatus);
            logFeAlarm(currentHealthStatus);
        }
        // Anyway, update latest response
        service.setLastHealthStatus(currentHealth);
    }

    private List<HealthCheckInfo> addHostedComponentsFeHealthCheck(String baseComponent, boolean requestedByBE) {
        String healthCheckUrl = getExternalComponentHcUrl(baseComponent);
        String serviceName = getExternalComponentHcUri(baseComponent);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(baseComponent)
                .targetServiceName(serviceName).build();

        StringBuilder description = new StringBuilder("");
        int connectTimeoutMs = 3000;
        int readTimeoutMs = service.getConfig().getHealthCheckSocketTimeoutInMs(5000);

        if (healthCheckUrl != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                HttpResponse<String> response = HttpRequest.get(healthCheckUrl, new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs)));
                int beStatus = response.getStatusCode();
                if (beStatus == HttpStatus.SC_OK || beStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    String beJsonResponse = response.getResponse();
                    return convertResponse(beJsonResponse, mapper, baseComponent, description, beStatus);
                } else {
                    description.append("Response code: " + beStatus);
                    log.trace("{} Health Check Response code: {}", baseComponent, beStatus);
                }
            } catch (Exception e) {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, serviceName, errorLogOptionalData, baseComponent + " unexpected response ", e);
                description.append(baseComponent + " Unexpected response: " + e.getMessage());
            }
        } else {
            description.append(baseComponent + " health check Configuration is missing");
        }

        String  compName = requestedByBE ? Constants.HC_COMPONENT_FE : baseComponent;
        return Collections.singletonList(new HealthCheckInfo(
                compName,
                HealthCheckInfo.HealthCheckStatus.DOWN,
                null,
                description.toString()));
    }

    private String getExternalComponentHcUri(String baseComponent) {
        String healthCheckUri = null;
        switch (baseComponent) {
            case HC_COMPONENT_ON_BOARDING:
                healthCheckUri = service.getConfig().getOnboarding().getHealthCheckUriFe();
                break;
            case HC_COMPONENT_DCAE:
                healthCheckUri = service.getConfig().getDcae().getHealthCheckUri();
                break;
            case HC_COMPONENT_CATALOG_FACADE_MS:
                healthCheckUri = service.getConfig().getCatalogFacadeMs().getHealthCheckUri();
                break;
            default:
                log.debug("Unsupported base component {}", baseComponent);
                break;
        }
        return healthCheckUri;
    }


    @VisibleForTesting
    String getExternalComponentHcUrl(String baseComponent) {
        String healthCheckUrl = null;
        switch (baseComponent) {
            case HC_COMPONENT_ON_BOARDING:
                healthCheckUrl = getOnboardingHealthCheckUrl();
                break;
            case HC_COMPONENT_DCAE:
                healthCheckUrl = getDcaeHealthCheckUrl();
                break;
            case HC_COMPONENT_CATALOG_FACADE_MS:
                healthCheckUrl = getCatalogFacadeHealthCheckUrl();
                break;
            default:
                log.debug("Unsupported base component {}", baseComponent);
                break;
        }
        return healthCheckUrl;
    }

    private void logFeAlarm(int lastFeStatus) {
        switch (lastFeStatus) {
            case 200:
                FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT, EcompErrorEnum.FeHealthCheckRecovery, "FE Health Recovered");
                FeEcompErrorManager.getInstance().logFeHealthCheckRecovery("FE Health Recovered");
                break;
            case 500:
                FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT, EcompErrorEnum.FeHealthCheckError, "Connection with ASDC-BE is probably down");
                FeEcompErrorManager.getInstance().logFeHealthCheckError("Connection with ASDC-BE is probably down");
                break;
            default:
                break;
        }
    }

    private HealthCheckService.HealthStatus checkHealth() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Configuration config = service.getConfig();

        HealthCheckWrapper feAggHealthCheck;
        boolean aggregateFeStatus = false;
        String redirectedUrl = String.format(URL, config.getBeProtocol(), config.getBeHost(),
                Constants.HTTPS.equals(config.getBeProtocol()) ? config.getBeSslPort() : config.getBeHttpPort());
        int connectTimeoutMs = 3000;
        int readTimeoutMs = config.getHealthCheckSocketTimeoutInMs(5000);
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(LOG_TARGET_ENTITY_BE)
                .targetServiceName(LOG_SERVICE_NAME).build();

        try {
            HttpResponse<String> response = HttpRequest.get(redirectedUrl, new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs)));
            log.debug("HC call to BE - status code is {}", response.getStatusCode());
            String beJsonResponse = response.getResponse();
            feAggHealthCheck = getFeHealthCheckInfos(gson, beJsonResponse);
            if (response.getStatusCode() != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                aggregateFeStatus = healthCheckUtil.getAggregateStatus(feAggHealthCheck.getComponentsInfo(), getExcludedComponentList());
            }
            //Getting aggregate FE status
            return new HealthCheckService.HealthStatus(aggregateFeStatus ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR, gson.toJson(feAggHealthCheck));

        }
        catch (Exception e) {
            log.debug("Health Check error when trying to connect to BE or external FE. Error: {}", e);
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LOG_SERVICE_NAME, errorLogOptionalData,
                    "Health Check error when trying to connect to BE or external FE.", e.getMessage());
            FeEcompErrorManager.getInstance().processEcompError(DEBUG_CONTEXT,EcompErrorEnum.FeHealthCheckGeneralError, "Unexpected FE Health check error");
            FeEcompErrorManager.getInstance().logFeHealthCheckGeneralError("Unexpected FE Health check error");
            return new HealthCheckService.HealthStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, gson.toJson(getBeDownCheckInfos()));
        }
    }

    @VisibleForTesting
    List<String> getExcludedComponentList() {
        List <String> excludedComponentList = Lists.newArrayList(service.getConfig().getHealthStatusExclude());
        if (isCatalogFacadeMsExcluded()) {
            if (log.isInfoEnabled()) {
                log.info(HC_COMPONENT_CATALOG_FACADE_MS + " has been added to the Healthcheck exclude list");
            }
            excludedComponentList.add(HC_COMPONENT_CATALOG_FACADE_MS);
        }
        return excludedComponentList;
    }

    private boolean isCatalogFacadeMsExcluded() {
        //CATALOG_FACADE_MS is excluded if it is not configured
        return service.getConfig().getCatalogFacadeMs() == null || StringUtils.isEmpty(service.getConfig().getCatalogFacadeMs().getPath());
    }

    private HealthCheckWrapper getFeHealthCheckInfos(Gson gson, String responseString) {
        Type wrapperType = new TypeToken<HealthCheckWrapper>() {
        }.getType();
        HealthCheckWrapper healthCheckWrapper = gson.fromJson(responseString, wrapperType);
        String description = "OK";
        healthCheckWrapper.getComponentsInfo()
                .add(new HealthCheckInfo(Constants.HC_COMPONENT_FE, HealthCheckInfo.HealthCheckStatus.UP, ExternalConfiguration.getAppVersion(), description));

        //add FE hosted components
        for (String component : healthCheckFeComponents) {
            buildHealthCheckListForComponent(component, healthCheckWrapper);
        }
        return healthCheckWrapper;
    }

    private void buildHealthCheckListForComponent(String component, HealthCheckWrapper healthCheckWrapper) {

        HealthCheckInfo componentHCInfoFromBE = getComponentHcFromList(component, healthCheckWrapper.getComponentsInfo());
        List<HealthCheckInfo> componentHCInfoList = addHostedComponentsFeHealthCheck(component, componentHCInfoFromBE != null);
        HealthCheckInfo calculateStatusFor;
        if (componentHCInfoFromBE != null) {
            if (log.isDebugEnabled()) {
                log.debug("{} component healthcheck info has been received from the BE and from the component itself", component);
            }
            //update the subcomponents's HC if exist and recalculate the component status according to the subcomponets HC
            calculateStatusFor = updateSubComponentsInfoOfBeHc(componentHCInfoFromBE, componentHCInfoList);
        }
        else {

            //this component is not in the BE HC response, need to add it and calculate the aggregated status
            if (log.isDebugEnabled()) {
                log.debug("{} component healthcheck info has been received from the component itself, it is not monitored by the BE", component);
            }
            //we assume that response from components which HC is not requested by BE have only one entry in the responded list
            calculateStatusFor = componentHCInfoList.get(0);
            healthCheckWrapper.getComponentsInfo()
                        .add(calculateStatusFor);

        }
        calculateAggregatedStatus(calculateStatusFor);

    }

    @VisibleForTesting
    HealthCheckInfo updateSubComponentsInfoOfBeHc(HealthCheckInfo componentHCInfoFromBE, List<HealthCheckInfo> componentHcReceivedByFE) {
        if (!CollectionUtils.isEmpty(componentHcReceivedByFE)) {
            //this component HC is received from BE, just need to calculate the status for that
            if (componentHCInfoFromBE.getComponentsInfo() == null) {
                componentHCInfoFromBE.setComponentsInfo(new ArrayList<>());
            }
            componentHCInfoFromBE.getComponentsInfo().addAll(componentHcReceivedByFE);
        }
        return componentHCInfoFromBE;
    }

    private HealthCheckInfo getComponentHcFromList(String component, List<HealthCheckInfo>  hcList) {
        return hcList.stream().filter(c -> c.getHealthCheckComponent().equals(component)).findFirst().orElse(null);
    }

    private void calculateAggregatedStatus(HealthCheckInfo baseComponentHCInfo) {
        if (!CollectionUtils.isEmpty(baseComponentHCInfo.getComponentsInfo())) {
            boolean status = healthCheckUtil.getAggregateStatus(baseComponentHCInfo.getComponentsInfo(), getExcludedComponentList());
            baseComponentHCInfo.setHealthCheckStatus(status ?
                    HealthCheckInfo.HealthCheckStatus.UP : HealthCheckInfo.HealthCheckStatus.DOWN);

            String componentsDesc = healthCheckUtil.getAggregateDescription(baseComponentHCInfo.getComponentsInfo());
            if (!StringUtils.isEmpty(componentsDesc)) { //aggregated description contains all the internal components desc
                baseComponentHCInfo.setDescription(componentsDesc);
            }
        }
    }

    private HealthCheckWrapper getBeDownCheckInfos() {
        List<HealthCheckInfo> healthCheckInfos = new ArrayList<>();
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_FE, HealthCheckInfo.HealthCheckStatus.UP,
                ExternalConfiguration.getAppVersion(), "OK"));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_BE, HealthCheckInfo.HealthCheckStatus.DOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_JANUSGRAPH, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_CASSANDRA, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_ON_BOARDING, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_DCAE, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        healthCheckInfos.add(new HealthCheckInfo(HC_COMPONENT_CATALOG_FACADE_MS, HealthCheckInfo.HealthCheckStatus.UNKNOWN, null, null));
        return new HealthCheckWrapper(healthCheckInfos, "UNKNOWN", "UNKNOWN");
    }

    String buildHealthCheckUrl(String protocol, String host, Integer port, String uri) {
        return String.format(EXTERNAL_HC_URL, protocol, host, port, uri);
    }

    private String getOnboardingHealthCheckUrl() {
        Configuration.OnboardingConfig onboardingConfig = service.getConfig().getOnboarding();
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(LOG_TARGET_ENTITY_CONFIG)
                .targetServiceName(LOG_TARGET_SERVICE_NAME_OB).build();

        if (StringUtils.isEmpty(ONBOARDING_HC_URL)) {
            if (onboardingConfig != null) {
                ONBOARDING_HC_URL = buildHealthCheckUrl(
                        onboardingConfig.getProtocolFe(), onboardingConfig.getHostFe(),
                        onboardingConfig.getPortFe(), onboardingConfig.getHealthCheckUriFe());
            }
            else {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LOG_SERVICE_NAME, errorLogOptionalData,
                        "Onboarding health check configuration is missing.");
            }
        }
        return ONBOARDING_HC_URL;
    }

    private String getDcaeHealthCheckUrl() {
        Configuration.DcaeConfig dcaeConfig = service.getConfig().getDcae();
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(LOG_TARGET_ENTITY_CONFIG)
                .targetServiceName(LOG_TARGET_SERVICE_NAME_DCAE).build();

        if (StringUtils.isEmpty(DCAE_HC_URL)) {
            if (dcaeConfig != null) {
                DCAE_HC_URL = buildHealthCheckUrl(
                        dcaeConfig.getProtocol(), dcaeConfig.getHost(),
                        dcaeConfig.getPort(), dcaeConfig.getHealthCheckUri());
            }
            else {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LOG_SERVICE_NAME, errorLogOptionalData,
                        "DCAE health check configuration is missing.");
            }
        }
        return DCAE_HC_URL;
    }

    private String getCatalogFacadeHealthCheckUrl() {
        Configuration.CatalogFacadeMsConfig catalogFacadeMsConfig = service.getConfig().getCatalogFacadeMs();
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(LOG_TARGET_ENTITY_CONFIG)
                .targetServiceName(LOG_TARGET_SERVICE_NAME_FACADE).build();

        if (StringUtils.isEmpty(CATALOG_FACADE_MS_HC_URL)) {
            if (catalogFacadeMsConfig != null) {
                CATALOG_FACADE_MS_HC_URL = buildHealthCheckUrl(
                        catalogFacadeMsConfig.getProtocol(), catalogFacadeMsConfig.getHost(),
                        catalogFacadeMsConfig.getPort(), catalogFacadeMsConfig.getHealthCheckUri());
            }
            else {
                log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LOG_SERVICE_NAME, errorLogOptionalData,
                        "Catalog Facade MS health check configuration is missing.");
            }
        }
        return CATALOG_FACADE_MS_HC_URL;
    }


    private List<HealthCheckInfo> convertResponse(String beJsonResponse, ObjectMapper mapper, String baseComponent, StringBuilder description, int beStatus) {
        ErrorLogOptionalData errorLogOptionalData = ErrorLogOptionalData.newBuilder().targetEntity(baseComponent)
                .targetServiceName(LOG_SERVICE_NAME).build();

        try {
            Map<String, Object> healthCheckMap = mapper.readValue(beJsonResponse, new TypeReference<Map<String, Object>>() {
            });
            if (healthCheckMap.containsKey("componentsInfo")) {
                return mapper.convertValue(healthCheckMap.get("componentsInfo"), new TypeReference<List<HealthCheckInfo>>() {
                });
            } else {
                description.append("Internal components are missing");
            }
        } catch (JsonSyntaxException | IOException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, LOG_SERVICE_NAME, errorLogOptionalData,
                    baseComponent + " Unexpected response body ", e);
            description.append(baseComponent)
                    .append("Unexpected response body. Response code: ")
                    .append(beStatus);
        }
        return new ArrayList<>();
    }
}