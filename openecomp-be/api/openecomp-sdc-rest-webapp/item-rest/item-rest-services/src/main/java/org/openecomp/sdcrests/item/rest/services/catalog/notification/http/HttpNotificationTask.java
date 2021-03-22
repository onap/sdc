/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdcrests.item.rest.services.catalog.notification.http;

import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.DONE;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.RETRY;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier;

/**
 * HTTP client for notifying the Catalog of an action on items. The items are referenced by their IDs. The client can run multiple times, in which
 * case only failed IDs will be re-attempted.
 *
 * @author evitaliy
 * @since 21 Nov 2018
 */
@ToString
class HttpNotificationTask implements Callable<AsyncNotifier.NextAction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpNotificationTask.class);
    private static final String APPLICATION_JSON = ContentType.APPLICATION_JSON.getMimeType();
    private static final String USER_ID_HEADER_PARAM = "USER_ID";
    private final String endpoint;
    private final String userId;
    private volatile Collection<String> itemIds;

    HttpNotificationTask(String endpoint, String userId, Collection<String> itemIds) {
        this.endpoint = endpoint;
        this.userId = userId;
        this.itemIds = itemIds;
    }

    @Override
    public synchronized AsyncNotifier.NextAction call() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = createPostRequest(endpoint, itemIds, userId);
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine status = response.getStatusLine();
                LOGGER.debug("Catalog notification on VSP IDs: {}, endpoint: {}, response: {}", itemIds, endpoint, status);
                itemIds = getFailedIds(itemIds, response.getEntity());
                if ((status.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) && (itemIds != null) && !itemIds.isEmpty()) {
                    LOGGER.debug("Catalog notification on VSP IDs {} failed. Endpoint: {}. Retry", itemIds, endpoint);
                    return RETRY;
                }
                return DONE;
            }
        } catch (Exception e) {
            LOGGER.error("Catalog notification on VSP IDs {} failed. Endpoint: {}", itemIds, endpoint, e);
            return DONE;
        }
    }

    private HttpPost createPostRequest(String postUrl, Collection<String> itemIds, String userId) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(postUrl);
        request.addHeader(HttpHeaders.ACCEPT, APPLICATION_JSON);
        request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader(USER_ID_HEADER_PARAM, userId);
        HttpEntity entity = new StringEntity(JsonUtil.object2Json(itemIds));
        request.setEntity(entity);
        return request;
    }

    private Collection<String> getFailedIds(Collection<String> itemIds, HttpEntity responseBody) {
        try {
            NotificationResponse response = JsonUtil.json2Object(responseBody.getContent(), NotificationResponse.class);
            return response != null ? response.failedIds : null;
        } catch (Exception e) {
            LOGGER.error("Error getting failed IDs from response", e);
        }
        return itemIds;
    }

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    private static class NotificationResponse {

        private Collection<String> failedIds;
    }
}
