/*
 * Copyright © 2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.item.rest.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdcrests.item.types.ItemAction;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

class CatalogNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogNotifier.class);

    private static final String USER_ID_HEADER_PARAM = "USER_ID";
    private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    private static final String CONFIG_FILE = System.getProperty(CONFIG_FILE_PROPERTY);
    private static final String CATALOG_NOTIFICATION_CONFIG = "catalogNotificationsConfig";
    private static final String CATALOG_PROTOCOL_KEY = "catalogBeProtocol";
    private static final String CATALOG_HTTP_PROTOCOL = "http|HTTP";
    private static final String CATALOG_HTTPS_PROTOCOL = "https|HTTPS";
    private static final String CATALOG_HOST_KEY = "catalogBeFqdn";
    private static final String CATALOG_HTTP_PORT_KEY = "catalogBeHttpPort";
    private static final String CATALOG_HTTPS_PORT_KEY = "catalogBeSslPort";
    private static final String CATALOG_NOTIFICATION_URL = "catalogNotificationUrl";
    private static final String URL_DEFAULT_FORMAT = "%s://%s:%s/sdc2/rest/v1/catalog/notif/vsp/";

    private static final CatalogNotifier SINGLETON = new CatalogNotifier();

    private static String notifyCatalogUrl;

    private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static CatalogNotifier getInstance() {
        return SINGLETON;
    }

    private CatalogNotifier() {
        init();
    }

    private void init() {

        LinkedHashMap<String, Object> catalogNotificationConfigurationMap = getCatalogNotificationConfiguration();

        Object protocol = catalogNotificationConfigurationMap.get(CATALOG_PROTOCOL_KEY);
        Object host = catalogNotificationConfigurationMap.get(CATALOG_HOST_KEY);

        if (protocol == null || host == null) {
            throw new ExceptionInInitializerError("Could not read configuration file " + CONFIG_FILE);
        }

        initCatalogNotificationUrl(catalogNotificationConfigurationMap, protocol, host);
    }

    private void initCatalogNotificationUrl(LinkedHashMap<String, Object> catalogNotificationConfigurationMap, Object protocol, Object host) {
        String portString = String.valueOf(protocol).toUpperCase();
        Object port = getPortConfiguration(catalogNotificationConfigurationMap, portString);

        if (catalogNotificationConfigurationMap.get(CATALOG_NOTIFICATION_URL) != null) {
            String urlFormat = String.valueOf(catalogNotificationConfigurationMap.get(CATALOG_NOTIFICATION_URL));
            notifyCatalogUrl =
                    String.format(urlFormat, String.valueOf(protocol), String.valueOf(host), String.valueOf(port));

        } else {
            notifyCatalogUrl = String.format(URL_DEFAULT_FORMAT, String.valueOf(protocol), String.valueOf(host),
                    String.valueOf(port));
        }
    }

    private Object getPortConfiguration(LinkedHashMap<String, Object> catalogNotificationConfigurationMap, String portString) {
        Object port;
        if (portString.matches(CATALOG_HTTP_PROTOCOL)) {
            port = catalogNotificationConfigurationMap.get(CATALOG_HTTP_PORT_KEY);
        } else if (portString.matches(CATALOG_HTTPS_PROTOCOL)) {
            port = catalogNotificationConfigurationMap.get(CATALOG_HTTPS_PORT_KEY);
        }
        else {
            throw new ExceptionInInitializerError("Invalid protocol defined in configuration file" +
                    " " + CONFIG_FILE + ". Notifications will not be sent to Catalog BE");
        }
        return port;
    }

    private static LinkedHashMap<String, Object> getCatalogNotificationConfiguration() {
        Map<String, LinkedHashMap<String, Object>> configurationMap;
        Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader = is -> {
            YamlUtil yamlUtil = new YamlUtil();
            return yamlUtil.yamlToMap(is);
        };

        configurationMap = geConfigurationMap(reader);

        LinkedHashMap<String, Object> catalogNotificationConfigurationMap = configurationMap.get(CATALOG_NOTIFICATION_CONFIG);
        if(catalogNotificationConfigurationMap == null){
            throw new ExceptionInInitializerError("Could not read configuration for catalog notification" +
                    " from file " + CONFIG_FILE + ". Notifications will not be sent to Catalog BE");
        }
        return catalogNotificationConfigurationMap;
    }

    private static Map<String, LinkedHashMap<String, Object>> geConfigurationMap(Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader) {
        Map<String, LinkedHashMap<String, Object>> configurationMap;

        if (CONFIG_FILE == null) {
            throw new ExceptionInInitializerError("Property " + CONFIG_FILE_PROPERTY
                    + " must be specified and point to a configuration file");
        }

        try {
            configurationMap = readFromFile(reader);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "Could not read configuration file " + CONFIG_FILE + "." +
                            "Notifications will not be sent to Catalog BE. Error: " + e.getMessage());
        }
        return configurationMap;
    }


    void execute(Collection<String> itemIds, ItemAction action) {

        String userId = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();

        Callable callable = createCallable(JsonUtil.object2Json(itemIds), action, 2, userId);

        executor.submit(callable);

    }

    private Callable createCallable(String itemIds, ItemAction action, int numOfRetries, String userId) {
        Callable callable = () -> handleHttpRequest(getUrl(action), itemIds, action, userId, numOfRetries);
        LoggingContext.copyToCallable(callable);
        return callable;
    }

    private Void handleHttpRequest(String url, String itemIds, ItemAction action, String userId,
                                   int numOfRetries) {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost request = createPostRequest(url, itemIds, userId);
            HttpResponse response = httpclient.execute(request);
            LOGGER.debug(String.format("Catalog notification on vspId - %s action  - %s. Response: %s", itemIds,
                    action.name(), response.getStatusLine()));

            if (numOfRetries > 1 && response.getStatusLine().getStatusCode() == Response.Status.INTERNAL_SERVER_ERROR
                    .getStatusCode()) {
                Callable callable =
                        createCallable(getFailedIds(itemIds, response.getEntity()), action, --numOfRetries, userId);
                executor.schedule(callable, 5, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            LOGGER.error(String.format("Catalog notification on vspId - %s action  - %s FAILED. Error: %S", itemIds,
                    action.name(), e.getMessage()));
        }
        return null;
    }

    private String getFailedIds(String itemIds, HttpEntity responseBody) {
        try {
            Map jsonBody = JsonUtil.json2Object(responseBody.getContent(), Map.class);
            return jsonBody.get("failedIds").toString();
        } catch (Exception e) {
            LOGGER.error("Catalog Notification RETRY - no failed IDs in response");
        }
        return JsonUtil.object2Json(itemIds);
    }

    private HttpPost createPostRequest(String postUrl, String itemIds, String userId)
            throws UnsupportedEncodingException {

        HttpPost request = new HttpPost(postUrl);

        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.addHeader(USER_ID_HEADER_PARAM, userId);

        HttpEntity entity = new StringEntity(itemIds);
        request.setEntity(entity);

        return request;
    }

    private String getUrl(ItemAction action) {
        String actionStr = "";
        if (action == ItemAction.ARCHIVE) {
            actionStr = "archived";
        } else if (action == ItemAction.RESTORE) {
            actionStr = "restored";
        }
        LOGGER.debug("Catalog notification URL - " + notifyCatalogUrl + actionStr);
        return notifyCatalogUrl + actionStr;
    }

    private static <T> T readFromFile(Function<InputStream, T> reader) throws IOException {
        try (InputStream is = new FileInputStream(CatalogNotifier.CONFIG_FILE)) {
            return reader.apply(is);
        }
    }
}
