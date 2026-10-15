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

package org.onap.sdc.frontend.ci.tests.execute.setup;

import java.io.File;
import org.onap.sdc.backend.ci.tests.config.Config;

/**
 * One download directory per test thread, so parallel tests neither pick up nor delete each other's files.
 */
final class ThreadDownloadDirectory {

    /**
     * Where the remote browser container sees the host's download automation folder (bind mount in integration-tests/pom.xml).
     */
    private static final String REMOTE_BROWSER_DOWNLOAD_ROOT = "/home/seluser/Desktop";

    private ThreadDownloadDirectory() {
    }

    static String hostPath(final Config config) {
        final File directory = new File(config.getDownloadAutomationFolder(), name());
        directory.mkdirs();
        // The browser container writes as its own user, and this JVM deletes the files afterwards.
        directory.setReadable(true, false);
        directory.setWritable(true, false);
        directory.setExecutable(true, false);
        return directory.getPath();
    }

    static String remoteBrowserPath() {
        return REMOTE_BROWSER_DOWNLOAD_ROOT + "/" + name();
    }

    private static String name() {
        return "thread-" + Thread.currentThread().getId();
    }
}
