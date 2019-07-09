/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.DONE;
import static org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier.NextAction.RETRY;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author evitaliy
 * @since 22 Nov 2018
 */
public class HttpNotificationTaskTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private static final String NOTIFICATION_PATH = "/notification";
    private static final String USER_ID = "d75360e1-f393-480f-b39e-fbbdf38a22c1";
    private static final String RETRY_SCENARIO = "RETRY_SCENARIO";
    private static final String MALFORMED_RESPONSE_SCENARIO = "MALFORMED_RESPONSE_SCENARIO";

    private static String endpoint;

    @BeforeClass
    public static void initConfiguration() {
        endpoint = "http://localhost:" + wireMockRule.port() + NOTIFICATION_PATH;
    }

    @Test
    public void doneWhenResponseOk() {
        assertDone(200, arrayToJson(UUID.randomUUID().toString()));
    }

    private void assertDone(int responseStatus, String body) {
        final String itemId = UUID.randomUUID().toString();
        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(responseStatus).withBody(body)));
        HttpNotificationTask task = new HttpNotificationTask(endpoint, USER_ID, Collections.singleton(itemId));
        assertEquals(DONE, task.call());
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH))
                       .withRequestBody(new EqualToJsonPattern(arrayToJson(itemId), true, false)));
    }

    private String arrayToJson(String... ids) {
        return ids.length == 0 ? "[]" : "[ \"" + String.join("\", \"", ids) + "\" ]";
    }

    @Test
    public void doneWhenResponse400() {
        assertDone(400, arrayToJson(UUID.randomUUID().toString()));
    }

    @Test
    public void doneWhenResponse522() {
        assertDone(522, arrayToJson(UUID.randomUUID().toString()));
    }

    @Test
    public void doneWhenResponse500ButFailedIdsNotReturned() {
        assertDone(500, "{}");
    }

    @Test
    public void doneWhenResponse500ButFailedIdsEmpty() {
        assertDone(500, toFailedIdsResponse());
    }

    private String toFailedIdsResponse(String... ids) {
        return "{ \"failedIds\": " + arrayToJson(ids) + " }";
    }

    @Test
    public void retryWithSameItemIdsWhenResponse500AndFailedToParseResponse() {

        final String[] expectedItemIds = {UUID.randomUUID().toString(), UUID.randomUUID().toString()};

        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(500).withBody("d[g.0g,y/"))
                        .inScenario(MALFORMED_RESPONSE_SCENARIO));
        HttpNotificationTask task = new HttpNotificationTask(endpoint, USER_ID, Arrays.asList(expectedItemIds));
        assertEquals(RETRY, task.call());

        EqualToJsonPattern expectedRequestBody = new EqualToJsonPattern(arrayToJson(expectedItemIds), true, false);
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH)).withRequestBody(expectedRequestBody));

        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(200).withBody("{}"))
                        .inScenario(MALFORMED_RESPONSE_SCENARIO));
        assertEquals(DONE, task.call());

        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH)).withRequestBody(expectedRequestBody));
    }

    @Test
    public void retryWithFailedItemsWhenResponse500() {

        final String failedId = UUID.randomUUID().toString();
        final String successId = UUID.randomUUID().toString();

        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(500).withBody(toFailedIdsResponse(failedId)))
                        .inScenario(RETRY_SCENARIO));
        HttpNotificationTask task = new HttpNotificationTask(endpoint, USER_ID, Arrays.asList(failedId, successId));
        assertEquals(RETRY, task.call());
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH))
                       .withRequestBody(new EqualToJsonPattern(arrayToJson(failedId, successId), true, false)));

        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(200).withBody("{}"))
                        .inScenario(RETRY_SCENARIO));
        assertEquals(DONE, task.call());
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH))
                       .withRequestBody(new EqualToJsonPattern(arrayToJson(failedId), true, false)));
    }

    @Test
    public void userIdSentToServer() {
        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(200)));
        HttpNotificationTask task = new HttpNotificationTask(endpoint, USER_ID, Collections.emptyList());
        assertEquals(DONE, task.call());
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH)).withHeader("USER_ID", new EqualToPattern(USER_ID)));
    }
}
