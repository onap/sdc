/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.notification.workers.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.config.ConfigurationManager;
import org.openecomp.sdc.notification.types.NotificationsStatusDto;
import org.openecomp.sdc.notification.workers.NewNotificationsReader;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.InputStreamReader;
import java.util.UUID;

public class NewNotificationsReaderRestImpl implements NewNotificationsReader {

    private static final String USER_ID_HEADER_PARAM = "USER_ID";
    private static final String LAST_DELIVERED_QUERY_PARAM = "LAST_DELIVERED_EVENT_ID";
    private static final String LIMIT_QUERY_PARAM = "NOTIFICATION_ROWS_LIMIT";
    private static final String BE_HOST = "beHost";
    private static final String BE_PORT = "beHttpPort";
    private static final String DEFAULT_BE_HOST = "localhost";
    private static final int DEFAULT_BE_PORT = 8080;
    private static final String URL = "http://%s:%d/onboarding-api/v1.0/notifications/worker?";
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String beHost;
    private static int bePort;

    private static final Logger LOGGER = LoggerFactory.getLogger(NewNotificationsReaderRestImpl.class);

    public NewNotificationsReaderRestImpl() {
        ConfigurationManager cm = ConfigurationManager.getInstance();
        bePort = cm.getConfigValue(BE_PORT, DEFAULT_BE_PORT);
        beHost = cm.getConfigValue(BE_HOST, DEFAULT_BE_HOST);
    }

    public NotificationsStatusDto getNewNotifications(String ownerId, UUID eventId, int limit) {
        HttpClient client = HttpClientBuilder.create().build();
        String url = String.format(URL, beHost, bePort);

        url = url + LIMIT_QUERY_PARAM + "=" + limit;
        if (eventId != null) {
            url = url + "&" + LAST_DELIVERED_QUERY_PARAM + "=" + eventId;
        }

        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.addHeader(USER_ID_HEADER_PARAM, ownerId);

        try {
            HttpResponse response = client.execute(request);
            return mapper.readValue(new InputStreamReader(response.getEntity().getContent()), NotificationsStatusDto.class);
        } catch (Exception e) {
            LOGGER.error("Failed to execute the request {}", url, e);
            return null;
        }
    }
}
