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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.AsyncNotifier;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.Notifier;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * Notifies the Catalog via an HTTP.
 *
 * @author evitaliy
 * @since 21 Nov 2018
 */
public class HttpTaskProducer implements BiFunction<Collection<String>, ItemAction, Callable<AsyncNotifier.NextAction>>, Notifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTaskProducer.class);
    private static final String CATALOG_HTTP_PROTOCOL = "HTTP";
    private static final String CATALOG_HTTPS_PROTOCOL = "HTTPS";
    private static final Map<ItemAction, String> ACTION_PATHS;

    static {
        ACTION_PATHS = new EnumMap<>(ItemAction.class);
        ACTION_PATHS.put(ItemAction.ARCHIVE, "archived");
        ACTION_PATHS.put(ItemAction.RESTORE, "restored");
    }

    private final String notifyCatalogUrl;

    /**
     * Initializes the producer from a provided configuration.
     *
     * @param config HTTP-specific configuration, cannot be null
     */
    public HttpTaskProducer(HttpConfiguration config) {
        String protocol = ensureEntryConfigured(config.getCatalogBeProtocol(), "Protocol");
        String host = ensureEntryConfigured(config.getCatalogBeFqdn(), "Catalog host");
        String url = ensureEntryConfigured(config.getCatalogNotificationUrl(), "Notification URL");
        String port = getPortConfiguration(protocol, config);
        this.notifyCatalogUrl = String.format(url, protocol, host, port);
    }

    private static String ensureEntryConfigured(String value, String entryName) {
        if (value == null) {
            throw new EntryNotConfiguredException(entryName);
        }
        return value;
    }

    private static String getPortConfiguration(String protocol, HttpConfiguration config) {
        if (CATALOG_HTTP_PROTOCOL.equalsIgnoreCase(protocol)) {
            return ensureEntryConfigured(config.getCatalogBeHttpPort(), "HTTP port");
        } else if (CATALOG_HTTPS_PROTOCOL.equalsIgnoreCase(protocol)) {
            return ensureEntryConfigured(config.getCatalogBeSslPort(), "SSL port");
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
    }

    static String getApiPath(ItemAction action) {
        String path = ACTION_PATHS.get(action);
        if (path == null) {
            throw new IllegalArgumentException("Unsupported action: " + action.name());
        }
        return path;
    }

    @Override
    public Callable<AsyncNotifier.NextAction> apply(Collection<String> itemIds, ItemAction action) {
        return createNotificationTask(itemIds, action);
    }

    private HttpNotificationTask createNotificationTask(Collection<String> itemIds, ItemAction action) {
        String userId = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();
        String notificationEndpoint = notifyCatalogUrl + getApiPath(action);
        LOGGER.debug("Catalog notification URL: {}", notificationEndpoint);
        return new HttpNotificationTask(notificationEndpoint, userId, itemIds);
    }

    @Override
    public void execute(Collection<String> itemIds, ItemAction action) {
        HttpNotificationTask task = createNotificationTask(itemIds, action);
        task.call();
    }
}
