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
package org.openecomp.sdc.common.impl;

import java.io.File;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.openecomp.sdc.common.api.ConfigurationSource;


/**
 * Save the
 *
 * @author esofer
 */
public class ExternalConfiguration {

    private static String appName;
    private static String appVersion;
    private static String configDir;
    private static ConfigurationSource configurationSource;
    private static FileAlterationMonitor monitor;
    private static ConfigFileChangeListener changeListener = new ConfigFileChangeListener();
    private static boolean enableReconfigure = true;

    public static String getAppName() {
        return appName;
    }

    public static void setAppName(String appName) {
        ExternalConfiguration.appName = appName;
    }

    public static String getAppVersion() {
        return appVersion;
    }

    public static void setAppVersion(String appVersion) {
        ExternalConfiguration.appVersion = appVersion;
    }

    public static String getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(String configDir) {
        ExternalConfiguration.configDir = configDir;
    }

    public static ConfigurationSource getConfigurationSource() {
        return configurationSource;
    }

    public static void setConfigurationSource(ConfigurationSource configurationSource) {
        ExternalConfiguration.configurationSource = configurationSource;
    }

    public static ConfigFileChangeListener getChangeListener() {
        return changeListener;
    }

    public static void listenForChanges() {
        if (enableReconfigure) {
            monitor = new FileAlterationMonitor();
            final String watchingDir = configDir + File.separator + appName;
            final FileAlterationObserver observer = new FileAlterationObserver(watchingDir);
            observer.addListener(changeListener);
            monitor.addObserver(observer);
            try {
                monitor.start();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void stopListenForFileChanges() {
        if (enableReconfigure && monitor != null) {
            try {
                monitor.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            monitor = null;
        }
    }
}
