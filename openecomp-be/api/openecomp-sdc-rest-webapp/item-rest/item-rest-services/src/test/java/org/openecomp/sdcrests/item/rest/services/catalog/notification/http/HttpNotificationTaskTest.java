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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.util.Collections;
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

    private static String endpoint;

    @BeforeClass
    public static void initConfiguration() {
        endpoint = "http://localhost:" + wireMockRule.port() + NOTIFICATION_PATH;
    }

    @Test
    public void doneWhenResponseOk() {
        stubFor(post(NOTIFICATION_PATH).willReturn(aResponse().withStatus(200).withBody("{}")));
        HttpNotificationTask task = new HttpNotificationTask(endpoint, USER_ID, Collections.emptyList());
        assertEquals(DONE, task.call());
        verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH)).withHeader("USER_ID", new EqualToPattern(USER_ID)));
    }

    @Test
    public void doneWhenResponseNot500() {

    }

    @Test
    public void retryWhenResponse500() {

    }
    
    @Test
    public void userIdSentToServer() {
        
    }
}