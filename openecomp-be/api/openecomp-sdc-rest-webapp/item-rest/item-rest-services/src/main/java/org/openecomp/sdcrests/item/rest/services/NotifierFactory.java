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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

class NotifierFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierFactory.class);

    interface Notifier {

        void execute(Collection<String> itemIds, ItemAction action);

    }

    private static final Notifier INSTANCE;


    static {
        INSTANCE = init();
    }

    private static Notifier init() {
        try {
            return new CatalogNotifier();
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize notifier. Notification cannot be sent", e);
            return new UnsupportedConfigurationNotifier();
        }
    }

    public static Notifier getInstance() {
        return INSTANCE;
    }

    private static class CatalogNotifier implements Notifier {

        private static final String USER_ID_HEADER_PARAM = "USER_ID";
        private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
        private static final String CATALOG_NOTIFICATION_CONFIG = "catalogNotificationsConfig";
        private static final String CATALOG_PROTOCOL_KEY = "catalogBeProtocol";
        private static final String CATALOG_HTTP_PROTOCOL = "HTTP";
        private static final String CATALOG_HTTPS_PROTOCOL = "HTTPS";
        private static final String CATALOG_HOST_KEY = "catalogBeFqdn";
        private static final String CATALOG_HTTP_PORT_KEY = "catalogBeHttpPort";
        private static final String CATALOG_HTTPS_PORT_KEY = "catalogBeSslPort";
        private static final String CATALOG_NOTIFICATION_URL = "catalogNotificationUrl";

        private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        private static final int NUM_OF_RETRIES = 2;

        private String notifyCatalogUrl;

        private CatalogNotifier() throws IOException {

            String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
                    "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);

            LinkedHashMap<String, Object> configurationMap = getNotificationConfiguration(file);

            Object protocol = configurationMap.get(CATALOG_PROTOCOL_KEY);
            if (protocol == null) {
                throw new IllegalArgumentException("Protocol for notification not configured in " + file);
            }

            Object host = configurationMap.get(CATALOG_HOST_KEY);
            if (host == null) {
                throw new IllegalArgumentException("Host for notification not configured in " + file);
            }

            this.notifyCatalogUrl =
                    getCatalogNotificationUrl(configurationMap, String.valueOf(protocol), String.valueOf(host));
        }

        private String getCatalogNotificationUrl(LinkedHashMap<String, Object> configurationMap, String protocol,
                String host) {

            String port = getPortConfiguration(configurationMap, protocol);
            Object endpoint = configurationMap.get(CATALOG_NOTIFICATION_URL);
            if (endpoint == null) {
                throw new IllegalArgumentException(CATALOG_NOTIFICATION_URL + " not defined in notification config");
            }

            return String.format(String.valueOf(endpoint), protocol, host, port);
        }

        private static String getPortConfiguration(LinkedHashMap<String, Object> catalogNotificationConfigurationMap,
                String protocol) {

            Object portKey;
            if (protocol.equalsIgnoreCase(CATALOG_HTTP_PROTOCOL)) {
                portKey = catalogNotificationConfigurationMap.get(CATALOG_HTTP_PORT_KEY);
            } else if (protocol.equalsIgnoreCase(CATALOG_HTTPS_PROTOCOL)) {
                portKey = catalogNotificationConfigurationMap.get(CATALOG_HTTPS_PORT_KEY);
            } else {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }

            return String.valueOf(portKey);
        }

        private static LinkedHashMap<String, Object> getNotificationConfiguration(String file) throws IOException {

            Map<String, LinkedHashMap<String, Object>> configurationMap = readConfiguration(file);

            LinkedHashMap<String, Object> catalogNotificationConfig = configurationMap.get(CATALOG_NOTIFICATION_CONFIG);

            if (catalogNotificationConfig == null) {
                throw new IllegalArgumentException(
                        "Configuration file '" + file + "' does not contain section " + CATALOG_NOTIFICATION_CONFIG);
            }

            return catalogNotificationConfig;
        }

        private static Map<String, LinkedHashMap<String, Object>> readConfiguration(String file) throws IOException {

            try (InputStream fileInput = new FileInputStream(file)) {
                YamlUtil yamlUtil = new YamlUtil();
                return yamlUtil.yamlToMap(fileInput);
            }
        }

        @Override
        public void execute(Collection<String> itemIds, ItemAction action) {

            String userId = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();

            Callable callable = createCallable(JsonUtil.object2Json(itemIds), action, NUM_OF_RETRIES, userId);

            executor.submit(callable);

        }

        private Callable createCallable(String itemIds, ItemAction action, int numOfRetries, String userId) {
            Callable callable = () -> handleHttpRequest(getUrl(action), itemIds, action, userId, numOfRetries);
            LoggingContext.copyToCallable(callable);
            return callable;
        }

        private Void handleHttpRequest(String url, String itemIds, ItemAction action, String userId, int numOfRetries) {

            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPost request = createPostRequest(url, itemIds, userId);
                HttpResponse response = httpclient.execute(request);
                LOGGER.debug(String.format("Catalog notification on vspId - {} action  - {}. Response: {}", itemIds,
                        action.name(), response.getStatusLine()));

                if (numOfRetries > 1
                            && response.getStatusLine().getStatusCode() == Response.Status.INTERNAL_SERVER_ERROR
                                                                                   .getStatusCode()) {
                    Callable callable =
                            createCallable(getFailedIds(itemIds, response.getEntity()), action, --numOfRetries, userId);
                    executor.schedule(callable, 5, TimeUnit.SECONDS);
                }

            } catch (Exception e) {
                LOGGER.error("Catalog notification on vspId - {} action  - {} failed", itemIds, action.name(), e);
            }
            return null;
        }

        private String getFailedIds(String itemIds, HttpEntity responseBody) {
            try {
                Map jsonBody = JsonUtil.json2Object(responseBody.getContent(), Map.class);
                return jsonBody.get("failedIds").toString();
            } catch (Exception e) {
                LOGGER.error("Catalog Notification RETRY - no failed IDs in response", e);
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
    }

    private static class UnsupportedConfigurationNotifier implements Notifier {

        @Override
        public void execute(Collection<String> itemIds, ItemAction action) {
            throw new IllegalStateException("Cannot send notifications. The notifier was not properly initialized");
        }
    }
}
