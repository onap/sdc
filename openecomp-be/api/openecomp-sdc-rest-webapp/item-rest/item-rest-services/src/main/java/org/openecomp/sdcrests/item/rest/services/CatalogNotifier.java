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
    private static final String CONFIG_FILE = "configuration.yaml";
    private static final String PROTOCOL_KEY = "beProtocol";
    private static final String HTTP_PROTOCOL = "http|HTTP";
    private static final String HTTPS_PROTOCOL = "https|HTTPS";
    private static final String HOST_KEY = "beFqdn";
    private static final String HTTP_PORT_KEY = "beHttpPort";
    private static final String HTTPS_PORT_KEY = "beSslPort";
    private static final String URL_KEY = "onboardCatalogNotificationUrl";
    private static final String URL_DEFAULT_FORMAT = "%s://%s:%s/sdc2/rest/v1/catalog/notif/vsp/";

    private static String configurationYamlFile = System.getProperty(CONFIG_FILE);
    private static String notifyCatalogUrl;

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);


    static {
        Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader = is -> {
            YamlUtil yamlUtil = new YamlUtil();
            return yamlUtil.yamlToMap(is);
        };

        Map<String, LinkedHashMap<String, Object>> configurationMap;

        try {
            configurationMap = readFromFile(configurationYamlFile, reader);
            Object protocol = configurationMap.get(PROTOCOL_KEY);
            Object host = configurationMap.get(HOST_KEY);

            if (protocol == null || host == null) {
                throw new ExceptionInInitializerError("Could not read configuration file configuration.yaml.");
            }

            Object port = null;
            if (String.valueOf(protocol).matches(HTTP_PROTOCOL)) {
                port = configurationMap.get(HTTP_PORT_KEY);
            }
            if (String.valueOf(protocol).matches(HTTPS_PROTOCOL)) {
                port = configurationMap.get(HTTPS_PORT_KEY);
            }

            if (configurationMap.get(URL_KEY) != null) {
                String urlFormat = String.valueOf(configurationMap.get(URL_KEY));
                notifyCatalogUrl =
                        String.format(urlFormat, String.valueOf(protocol), String.valueOf(host), String.valueOf(port));

            } else {
                notifyCatalogUrl = String.format(URL_DEFAULT_FORMAT, String.valueOf(protocol), String.valueOf(host),
                        String.valueOf(port));
            }

        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "Could not read configuration file configuration.yaml. Error: " + e.getMessage());

        }
    }


    public void execute(Collection<String> itemIds, ItemAction action, int numOfRetries) {

        String userId = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();

        Callable callable = createCallable(JsonUtil.object2Json(itemIds), action, numOfRetries, userId);

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

    private static <T> T readFromFile(String file, Function<InputStream, T> reader) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return reader.apply(is);
        }
    }
}
