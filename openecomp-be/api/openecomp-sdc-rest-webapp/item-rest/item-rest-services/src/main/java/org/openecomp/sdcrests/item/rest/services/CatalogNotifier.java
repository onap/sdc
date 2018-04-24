package org.openecomp.sdcrests.item.rest.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.types.ItemAction;

public class CatalogNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogNotifier.class);

    private static final String USER_ID_HEADER_PARAM = "USER_ID";
    private static final String CONFIG_FILE = "configuration.yaml";
    private static final String PROTOCOL_KEY = "beProtocol";
    private static final String HOST_KEY = "beFqdn";
    private static final String PORT_KEY = "beHttpPort";
    private static final String URL_KEY = "onboardCatalogNotificationUrl";
    private static final String URL_DEFAULT_FORMAT = "%s://%s:%s/sdc2/rest/v1/catalog/notif/vsp/";

    private String configurationYamlFile = System.getProperty(CONFIG_FILE);
    private String notifyCatalogUrl;

    {
        Function<InputStream, Map<String, LinkedHashMap<String, Object>>> reader = is -> {
            YamlUtil yamlUtil = new YamlUtil();
            return yamlUtil.yamlToMap(is);
        };

        Map<String, LinkedHashMap<String, Object>> configurationMap;

        try {
            configurationMap = readFromFile(configurationYamlFile, reader);

        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file configuration.yaml");
        }

        Map<String, String> configValues = new HashMap<>();
        configValues.put(PROTOCOL_KEY, String.valueOf(configurationMap.get(PROTOCOL_KEY)));
        configValues.put(HOST_KEY, String.valueOf(configurationMap.get(HOST_KEY)));
        configValues.put(PORT_KEY, String.valueOf(configurationMap.get(PORT_KEY)));

        configValues.forEach((key, value) -> {
            if (value == null) {
                throw new RuntimeException(
                        String.format("Missing value %s in configuration file configuration.yaml", key));
            }
        });

        if (configurationMap.get(URL_KEY) != null) {
            String urlFormat = String.valueOf(configurationMap.get(URL_KEY));
            notifyCatalogUrl = String.format(urlFormat, configValues.get(PROTOCOL_KEY), configValues.get(HOST_KEY),
                    configValues.get(PORT_KEY));

        } else {
            notifyCatalogUrl =
                    String.format(URL_DEFAULT_FORMAT, configValues.get(PROTOCOL_KEY), configValues.get(HOST_KEY),
                            configValues.get(PORT_KEY));
        }
    }


    public void execute(Collection<String> itemIds, ItemAction action) {

        String userId = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();

        Runnable runnable = () -> handleHttpRequest(getUrl(action), itemIds, action, userId);

        Thread thread = new Thread(runnable);

        thread.start();
    }

    private void handleHttpRequest(String url, Collection<String> itemIds, ItemAction action, String userId) {
        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpclient = null;
        try {
            HttpPost request = createPostRequest(url, JsonUtil.object2Json(itemIds), userId);
            httpclient = HttpClients.custom().setConnectionManager(pool).build();
            HttpResponse response = httpclient.execute(request);
            LOGGER.error(
                    String.format("Catalog notification on vspId - %s action  - %s. Response: %s", itemIds.toString(),
                            action.name(), response.getStatusLine()));
            if (response.getStatusLine().getStatusCode() == 500) {
                TimeUnit.SECONDS.sleep(5);
                LOGGER.error("Catalog notification response: " + response.getEntity());
                HttpPost requestRetry = createPostRequest(url, getFailedIds(itemIds, response.getEntity()), userId);
                response = httpclient.execute(requestRetry);
                LOGGER.error(String.format("Catalog notification RETRY on vspId - %s action  - %s. Response: %s",
                        itemIds.toString(), action.name(), response.getStatusLine()));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Catalog notification on vspId - %s action  - %s FAILED. Error: %S",
                    itemIds.toString(), action.name(), e.getMessage()));
        } finally {
            pool.close();
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    private String getFailedIds(Collection<String> itemIds, HttpEntity responseBody) {
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
        LOGGER.error("Catalog notification on URL - " + notifyCatalogUrl + actionStr);
        return notifyCatalogUrl + actionStr;
    }

    private <T> T readFromFile(String file, Function<InputStream, T> reader) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return reader.apply(is);
        }
    }
}
