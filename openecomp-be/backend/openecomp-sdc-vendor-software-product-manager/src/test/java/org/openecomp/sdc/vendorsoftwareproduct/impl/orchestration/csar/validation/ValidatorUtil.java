/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides util methods for Validation Test classes.
 */

class ValidatorUtil {

    private ValidatorUtil() {

    }

    /**
     * Reads a file and coverts it to a byte array.
     *
     * @param filePath      The file path
     * @return
     *  The file byte array
     * @throws IOException
     *  When the file was not found or the input stream could not be opened
     */
    public static byte[] getFileResource(final String filePath) throws IOException {
        try(final InputStream inputStream = ClassLoader.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException(String.format("Could not find the resource on path \"%s\"", filePath));
            }
            return IOUtils.toByteArray(inputStream);
        } catch (final IOException ex) {
            throw new IOException(String.format("Could not open the input stream for resource on path \"%s\"", filePath), ex);
        }
    }
}
