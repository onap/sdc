/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.util;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Test resources utility class.
 */
public class TestResourcesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResourcesUtil.class);

    private TestResourcesUtil() {

    }

    /**
     * Reads a file and coverts it to a byte array.
     *
     * @param resourcePath      The resource file path
     * @return
     *  The resource file byte array
     * @throws IOException
     *  When the file was not found or the input stream could not be opened
     */
    public static byte[] getFileResourceBytes(final String resourcePath) throws IOException {
        try(final InputStream inputStream = ClassLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException(String.format("Could not find the resource on path \"%s\"", resourcePath));
            }
            return IOUtils.toByteArray(inputStream);
        } catch (final IOException ex) {
            throw new IOException(String.format("Could not open the input stream for resource on path \"%s\"", resourcePath), ex);
        }
    }

    /**
     * Reads a file in the given path.
     * The method forces an assertion fail if the resource could not be loaded.
     * @param resourcePath      The resource file path
     * @return
     *  The resource file byte array
     */
    public static byte[] getResourceBytesOrFail(final String resourcePath) {
        try {
            return getFileResourceBytes(resourcePath);
        } catch (final IOException e) {
            final String errorMsg = String.format("Could not load resource '%s'", resourcePath);
            LOGGER.error(errorMsg, e);
            fail(errorMsg);
        }

        return null;
    }

    /**
     * Gets the input stream of a resource file
     *
     * @param resourcePath      The resource file path
     * @return
     *  The resource input stream
     */
    public static InputStream getFileResourceAsStream(final String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    }

    /**
     * Gets the input stream of a resource file
     *
     * @param resourcePath      The resource file path
     * @return
     *  The resource input stream
     */
    public static URL getFileUrl(final String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResource(resourcePath);
    }

}
