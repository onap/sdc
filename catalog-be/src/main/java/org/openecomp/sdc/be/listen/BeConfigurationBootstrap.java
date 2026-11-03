/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.listen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Loads the catalog-be configuration and watches the TLS files it names. Needs no servlet container: the caller
 * passes the source that {@link ExternalConfiguration#load} returned.
 */
public final class BeConfigurationBootstrap {

    private static final Logger log = Logger.getLogger(BeConfigurationBootstrap.class);

    private BeConfigurationBootstrap() {
    }

    public static ConfigurationManager start(ConfigurationSource configurationSource) {
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        log.debug("loading configuration from configDir: {} appName: {}", ExternalConfiguration.getConfigDir(), ExternalConfiguration.getAppName());
        watchTlsFiles(configurationManager.getConfiguration());
        return configurationManager;
    }

    private static void watchTlsFiles(Configuration configuration) {
        final Map<String, IOFileFilter> tlsFileFilters = createTlsFileFilters(configuration);
        if (!tlsFileFilters.isEmpty()) {
            final TlsFileChangeHandler tlsFileChangeHandler = new TlsFileChangeHandler();
            tlsFileFilters.forEach((path, filter) -> listenForChanges(path, tlsFileChangeHandler, filter));
        }
    }

    static Map<String, IOFileFilter> createTlsFileFilters(Configuration configuration) {
        final Map<String, IOFileFilter> filters = new HashMap<>();
        addFilter(filters, configuration.getTlsCert());
        addFilter(filters, configuration.getTlsKey());
        addFilter(filters, configuration.getCaCert());
        return filters;
    }

    private static void addFilter(final Map<String, IOFileFilter> filters, final String path) {
        if (path != null) {
            final File file = new File(path);
            final IOFileFilter fileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter(file.getName()));
            filters.merge(file.getParent(), fileFilter, FileFilterUtils::or);
        }
    }

    private static void listenForChanges(String path, FileAlterationListenerAdaptor changeListener, IOFileFilter ioFileFilter) {
        FileAlterationMonitor monitor = new FileAlterationMonitor();
        final FileAlterationObserver observer = new FileAlterationObserver(path, ioFileFilter);
        observer.addListener(changeListener);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (final Exception exception) {
            log.error("Error starting monitoring of TLS files", exception);
        }
    }
}
