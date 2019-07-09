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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * @author evitaliy
 * @since 26 Nov 2018
 */
public class HttpTaskProducerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void uniquePathExistsForEveryAction() {

        ItemAction[] availableActions = ItemAction.values();
        Set<String> collectedPaths = new HashSet<>(availableActions.length);
        for (ItemAction action : availableActions) {
            String path = HttpTaskProducer.getApiPath(action);
            assertFalse("Path empty for action '" + action.name() + "'", path == null || path.isEmpty());
            collectedPaths.add(path);
        }

        assertEquals("Paths not unique for some actions", availableActions.length, collectedPaths.size());
    }

    @Test
    public void restorePathEqualsRestored() {
        assertEquals("restored", HttpTaskProducer.getApiPath(ItemAction.RESTORE));
    }

    @Test
    public void archivePathEqualsArchived() {
        assertEquals("archived", HttpTaskProducer.getApiPath(ItemAction.ARCHIVE));
    }

    @Test
    public void errorWhenProtocolNotDefined() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol(null);
        exception.expect(EntryNotConfiguredException.class);
        exception.expectMessage(containsString("Protocol"));
        new HttpTaskProducer(config);
    }

    @Test
    public void errorWhenFqdnNotDefined() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeFqdn(null);
        exception.expect(EntryNotConfiguredException.class);
        exception.expectMessage(containsString("Catalog host"));
        new HttpTaskProducer(config);
    }

    @Test
    public void errorWhenNotificationUrlNotDefined() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogNotificationUrl(null);
        exception.expect(EntryNotConfiguredException.class);
        exception.expectMessage(containsString("Notification URL"));
        new HttpTaskProducer(config);
    }

    @Test
    public void errorWhenUnknownProtocol() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("invented-protocol");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Unsupported protocol"));
        new HttpTaskProducer(config);
    }

    @Test
    public void errorWhenHttpUsedButHttpPortUndefined() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("http");
        config.setCatalogBeHttpPort(null);
        exception.expect(EntryNotConfiguredException.class);
        exception.expectMessage(containsString("HTTP port"));
        new HttpTaskProducer(config);
    }

    @Test
    public void errorWhenSslUsedButHttpsPortUndefined() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("https");
        config.setCatalogBeSslPort(null);
        exception.expect(EntryNotConfiguredException.class);
        exception.expectMessage(containsString("SSL port"));
        new HttpTaskProducer(config);
    }

    @Test
    public void okWhenProtocolHttps() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("https");
        new HttpTaskProducer(config);
    }

    @Test
    public void okWhenProtocolHttpsMixedCase() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("hTTpS");
        new HttpTaskProducer(config);
    }

    @Test
    public void okWhenProtocolHttpMixedCase() {
        HttpConfiguration config = mockConfiguration();
        config.setCatalogBeProtocol("HTtp");
        new HttpTaskProducer(config);
    }

    private HttpConfiguration mockConfiguration() {
        HttpConfiguration config = new HttpConfiguration();
        config.setCatalogBeFqdn("fqdn");
        config.setCatalogBeHttpPort("http-port");
        config.setCatalogBeProtocol("http");
        config.setCatalogBeSslPort("ssl-port");
        config.setCatalogNotificationUrl("url");
        return config;
    }
}
