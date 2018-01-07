/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.logging.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * <p>Binds to a concrete implementation of logging services.</p>
 *
 * <p>In order to use the factory, a particular (e.g. framework-specific) implementation of a service must be
 * configured as described in
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">java.util.ServiceLoader</a>).</p>
 *
 * @author evitaliy
 * @since 13/09/2016.
 *
 * @see ServiceLoader
 */

// No advanced logging can be used here because we don't know
// which underlying implementation will be used
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "squid:S106"})
class ServiceBinder {

    private static final LoggingServiceProvider PROVIDER = lookupProvider();

    private ServiceBinder () {
        // prevent instantiation
    }

    private static LoggingServiceProvider lookupProvider() {

        ServiceLoader<LoggingServiceProvider> loader = ServiceLoader.load(LoggingServiceProvider.class);
        Iterator<LoggingServiceProvider> iterator = loader.iterator();

        if (!iterator.hasNext()) {
            System.err.printf("[ERROR] No provider configured for logging services %s. " +
                            "Default implementation will be used.\n",
                    LoggingServiceProvider.class.getName());
            return null;
        }

        try {

            LoggingServiceProvider provider = iterator.next();
            if (!iterator.hasNext()) {
                return provider;
            }

            Logger logger = provider.getLogger(ServiceBinder.class);
            if (logger.isWarnEnabled()) {
                logger.warn("More than one provider for logging services {} found",
                        LoggingServiceProvider.class.getName());
            }

            return provider;

        } catch (Exception e) {
            // don't fail if the provider cannot be instantiated
            e.printStackTrace(System.err);
            return null;
        }
    }

    static Optional<LoggingContextService> getContextServiceBinding() {
        return PROVIDER == null ? Optional.empty() : Optional.of(PROVIDER);
    }

    static Optional<LoggerCreationService> getCreationServiceBinding() {
        return PROVIDER == null ? Optional.empty() : Optional.of(PROVIDER);
    }
}

