/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.core.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ToscaSolConverterPnfTest {

    public static final String INPUT_DIR = "pnfDescriptor/in/";
    public static final String OUTPUT_DIR = "pnfDescriptor/out/";
    private String inputFilename;
    private YamlUtil yamlUtil = new YamlUtil();
    private ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();

    public ToscaSolConverterPnfTest(String inputFilename) {
        this.inputFilename = inputFilename;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<String> input() throws IOException {
        try (Stream<Path> files = Files.list(getPathFromClasspath(INPUT_DIR))) {
            return files.map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void testTopologyTemplateConversions() throws IOException {
        final byte[] descriptor = getInputFileResource(inputFilename);
        ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        ToscaSolConverterPnf toscaSolConverter = new ToscaSolConverterPnf();
        toscaSolConverter.convertTopologyTemplate(serviceTemplate, serviceTemplateReaderService);

        String result = yamlUtil.objectToYaml(serviceTemplate);
        String expectedResult = getExpectedResultFor(inputFilename);
        assertEquals(expectedResult, result);
    }

    private String getExpectedResultFor(String inputFilename) throws IOException {
        try (InputStream inputStream = getOutputFileResourceCorrespondingTo(inputFilename)) {
            ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(inputStream, ServiceTemplate.class);
            return yamlUtil.objectToYaml(serviceTemplate);
        }
    }

    private static Path getPathFromClasspath(String location) {
        return Paths.get(Thread.currentThread().getContextClassLoader().getResource(location).getPath());
    }

    private byte[] getInputFileResource(String inputFilename) throws IOException {
        return getFileResource(INPUT_DIR + inputFilename);
    }

    private InputStream getOutputFileResourceCorrespondingTo(String inputFilename) {
        String outputFilename = getOutputFilenameFrom(inputFilename);
        return getFileResourceAsInputStream(OUTPUT_DIR + outputFilename);
    }

    private String getOutputFilenameFrom(String inputFilename) {
        return inputFilename.replace("pnfDescriptor", "topologyTemplate");
    }

    private byte[] getFileResource(String filePath) throws IOException {
        try (InputStream inputStream = getFileResourceAsInputStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private InputStream getFileResourceAsInputStream(String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

}