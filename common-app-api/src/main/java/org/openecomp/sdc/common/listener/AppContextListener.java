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
package org.openecomp.sdc.common.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class AppContextListener implements ServletContextListener {

    private static Logger log = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent context) {
        log.debug("ServletContextListener initialized ");
        log.debug("After read values from Manifest {}", getManifestInfo(context.getServletContext()));
        Map<String, String> manifestAttr = getManifestInfo(context.getServletContext());
        if (manifestAttr.isEmpty()) {
            manifestAttr = getPackageInfo(getClass().getPackage());
        }
        String appName = setAndGetAttributeInContext(context, manifestAttr, Constants.APPLICATION_NAME);
        String appVersion = setAndGetAttributeInContext(context, manifestAttr, Constants.APPLICATION_VERSION);
        ConfigurationSource configurationSource = ExternalConfiguration.load(appName, appVersion, System.getProperty(Constants.CONFIG_HOME));
        context.getServletContext().setAttribute(Constants.CONFIGURATION_SOURCE_ATTR, configurationSource);
    }

    @Override
    public void contextDestroyed(ServletContextEvent context) {
        log.debug("ServletContextListener destroyed");
        ExternalConfiguration.stopListenForFileChanges();
    }

    private String setAndGetAttributeInContext(ServletContextEvent context, Map<String, String> manifestAttr, String attr) {
        String name = manifestAttr.get(attr);
        if (name != null) {
            context.getServletContext().setAttribute(attr, name);
        }
        return name;
    }

    /**
     * An application started from an executable jar has no web-app manifest. The jar that holds the listener carries
     * the same Implementation-Title and Implementation-Version entries.
     */
    private static Map<String, String> getPackageInfo(Package listenerPackage) {
        Map<String, String> result = new HashMap<>();
        if (listenerPackage.getImplementationTitle() != null) {
            result.put(Constants.APPLICATION_NAME, listenerPackage.getImplementationTitle());
        }
        if (listenerPackage.getImplementationVersion() != null) {
            result.put(Constants.APPLICATION_VERSION, listenerPackage.getImplementationVersion());
        }
        return result;
    }

    public static Map<String, String> getManifestInfo(ServletContext application) {
        Map<String, String> result = new HashMap<>();
        InputStream inputStream = null;
        try {
            inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (inputStream == null) {
                return result;
            }
            Manifest manifest = new Manifest(inputStream);
            Attributes attr = manifest.getMainAttributes();
            String name = attr.getValue("Implementation-Title");
            if (name != null) {
                result.put(Constants.APPLICATION_NAME, name);
            }
            String version = attr.getValue("Implementation-Version");
            if (version != null) {
                result.put(Constants.APPLICATION_VERSION, version);
            }
        } catch (IOException e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.info("close FileOutputStream failed - {}", e);
                }
            }
        }
        return result;
    }
}
