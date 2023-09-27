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
package org.openecomp.sdc.be.listen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.monitoring.BeMonitoringService;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class BEAppContextListener extends AppContextListener implements ServletContextListener {

    private static final String MANIFEST_FILE_NAME = "/META-INF/MANIFEST.MF";
    private static final Logger log = Logger.getLogger(BEAppContextListener.class);

    public void contextInitialized(ServletContextEvent context) {
        super.contextInitialized(context);
        ConfigurationManager configurationManager = new ConfigurationManager(ExternalConfiguration.getConfigurationSource());
        log.debug("loading configuration from configDir: {} appName: {}", ExternalConfiguration.getConfigDir(), ExternalConfiguration.getAppName());
        context.getServletContext().setAttribute(Constants.CONFIGURATION_MANAGER_ATTR, configurationManager);
        WebAppContextWrapper webAppContextWrapper = new WebAppContextWrapper();
        context.getServletContext().setAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR, webAppContextWrapper);
        context.getServletContext().setAttribute(Constants.ASDC_RELEASE_VERSION_ATTR, getVersionFromManifest(context));
        // Monitoring service
        BeMonitoringService bms = new BeMonitoringService(context.getServletContext());
        bms.start(configurationManager.getConfiguration().getSystemMonitoring().getProbeIntervalInSeconds(15));
        initTlsFileMonitoring();
        log.debug("After executing {}", this.getClass());
    }

    private String getVersionFromManifest(ServletContextEvent context) {
        ServletContext servletContext = context.getServletContext();
        InputStream inputStream = servletContext.getResourceAsStream(MANIFEST_FILE_NAME);
        String version = null;
        try {
            Manifest mf = new Manifest(inputStream);
            Attributes atts = mf.getMainAttributes();
            version = atts.getValue(Constants.ASDC_RELEASE_VERSION_ATTR);
            if (version == null || version.isEmpty()) {
                log.warn("failed to read ASDC version from MANIFEST.");
            } else {
                log.info("ASDC version from MANIFEST is {}", version);
            }
        } catch (IOException e) {
            log.warn("failed to read ASDC version from MANIFEST", e);
        }
        return version;
    }
    
    private void initTlsFileMonitoring() {
        final Map<String, IOFileFilter> tlsFileFilters = createTlsFileFilters();
        if (!tlsFileFilters.isEmpty()) {
            final TlsFileChangeHandler tlsFileChangeHandler = new TlsFileChangeHandler();
            tlsFileFilters.entrySet().stream().forEach(entry -> listenForChanges(entry.getKey(), tlsFileChangeHandler, entry.getValue()));
        }
    }
    
    private Map<String, IOFileFilter> createTlsFileFilters() {        
        final Map<String, IOFileFilter> filters = new HashMap<>();
        addFilter(filters, ConfigurationManager.getConfigurationManager().getConfiguration().getTlsCert());
        addFilter(filters, ConfigurationManager.getConfigurationManager().getConfiguration().getTlsKey());
        addFilter(filters, ConfigurationManager.getConfigurationManager().getConfiguration().getCaCert());
        return filters;
    }
    
    private void addFilter(final Map<String, IOFileFilter> filters, final String path) {
        if (path != null) {
            final File file = new File(path);
            final IOFileFilter caCertFileFilter =
                    FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter(file.getName()));
            
            if (filters.containsKey(file.getParent())) {
                filters.put(file.getParent(), FileFilterUtils.or(filters.get(file.getParent()), caCertFileFilter));
            } else {
                filters.put(file.getParent(), caCertFileFilter);  
            }
        }
    }
    
    private void listenForChanges(String path, FileAlterationListenerAdaptor changeListener, IOFileFilter ioFileFilter) {
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
