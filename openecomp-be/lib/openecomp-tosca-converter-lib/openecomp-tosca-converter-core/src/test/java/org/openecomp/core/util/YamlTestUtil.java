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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.onap.sdc.tosca.services.YamlUtil;

public class YamlTestUtil {

    private YamlTestUtil() {
    }

    /**
     * Reads the description file that has the required YAML format.
     *
     * @param yamlFile The yaml file
     * @return The yaml parsed to Object
     */
    public static Object read(final File yamlFile) throws IOException {
        try (final InputStream fileInputStream = new FileInputStream(yamlFile)) {
            return read(fileInputStream);
        }
    }

    public static Object read(final String yamlFilePath) throws IOException {
        try (final InputStream resourceInputStream = TestResourcesUtil.getFileResourceAsStream(yamlFilePath)) {
            return read(resourceInputStream);
        }
    }

    public static Object read(final InputStream yamlFileInputStream) {
        return YamlUtil.read(yamlFileInputStream);
    }

    public static Object readOrFail(final String yamlFilePath) {
        try {
            return read(yamlFilePath);
        } catch (final IOException ignored) {
            fail(String.format("Could not load '%s'", yamlFilePath));
            return null;
        }
    }


}
