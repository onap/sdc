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
package org.openecomp.sdcrests.item.rest.services.catalog.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.http.HttpConfiguration;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.http.HttpTaskProducer;
import org.openecomp.sdcrests.item.types.ItemAction;

/**
 * Creates an instance of {@link Notifier}, initialized according to current configuration. The configuration must be passed via the {@link
 * #CONFIG_FILE_PROPERTY} JVM argument.
 */
public class NotifierFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierFactory.class);
    private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    private static final String CONFIG_SECTION = "catalogNotificationsConfig";
    private static final Notifier INSTANCE;

    static {
        INSTANCE = createInstance();
    }

    private NotifierFactory() {
        // prevent instantiation
    }

    /**
     * Returns a {@link Notifier} instance according to the provided configuration. If no configuration was not provided, or the given configuration
     * is incorrect, then an instance with reduced functionality will be returned.
     *
     * @return available instance of {@link Notifier}
     */
    public static Notifier getInstance() {
        return INSTANCE;
    }

    static Notifier createInstance() {
        try {
            String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
                "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);
            Object config = getNotificationConfiguration(file);
            ObjectMapper mapper = new ObjectMapper();
            HttpConfiguration httpConfig = mapper.convertValue(config, HttpConfiguration.class);
            return new AsyncNotifier(new HttpTaskProducer(httpConfig));
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize notifier. Notifications will not be sent", e);
            return new UnsupportedConfigurationNotifier();
        }
    }

    private static Object getNotificationConfiguration(String file) throws IOException {
        Map<?, ?> configuration = Objects.requireNonNull(readConfigurationFile(file), "Configuration cannot be empty");
        Object notificationConfig = configuration.get(CONFIG_SECTION);
        if (notificationConfig == null) {
            throw new EntryNotConfiguredException(CONFIG_SECTION + " section");
        }
        return notificationConfig;
    }

    private static Map<?, ?> readConfigurationFile(String file) throws IOException {
        try (InputStream fileInput = new FileInputStream(file)) {
            YamlUtil yamlUtil = new YamlUtil();
            return yamlUtil.yamlToMap(fileInput);
        }
    }

    static class UnsupportedConfigurationNotifier implements Notifier {

        @Override
        public void execute(Collection<String> itemIds, ItemAction action) {
            throw new IllegalStateException("Cannot send notifications. The notifier was not properly initialized");
        }
    }
}
