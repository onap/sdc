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

package org.openecomp.core.converter.impl.pnfd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.pnfd.PnfdTransformationEngine;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

class PnfTransformationEngineTest {

    private static final String INPUT_DIR = "pnfDescriptor/in/";
    private static final String OUTPUT_DIR = "pnfDescriptor/out/";
    private final YamlUtil yamlUtil = new YamlUtil();
    private final ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();

    private static List<String> input() throws IOException {
        try (final Stream<Path> files = Files.list(getPathFromClasspath(INPUT_DIR))) {
            return files.map(path -> path.getFileName().toString()).collect(Collectors.toList());
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("input")
    void testTopologyTemplateConversions(final String inputFilename) throws IOException {
        final byte[] descriptor = getInputFileResource(inputFilename);
        final ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        final ServiceTemplate serviceTemplate = new ServiceTemplate();

        PnfdTransformationEngine pnfdTransformationEngine = new PnfdNodeTypeTransformationEngine(serviceTemplateReaderService, serviceTemplate);
        pnfdTransformationEngine.transform();
        pnfdTransformationEngine = new PnfdNodeTemplateTransformationEngine(serviceTemplateReaderService, serviceTemplate);
        pnfdTransformationEngine.transform();

        final String yamlContent = yamlUtil.objectToYaml(serviceTemplate);
        final String result = yamlUtil.objectToYaml(yamlUtil.yamlToObject(yamlContent, ServiceTemplate.class));
        final String expectedResult = getExpectedResultFor(inputFilename);
        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("input")
    void testReadDefinition(final String inputFilename) throws IOException {
        final byte[] descriptor = getInputFileResource(inputFilename);
        final ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        final ServiceTemplate serviceTemplate = new ServiceTemplate();
        AbstractPnfdTransformationEngine engine = new PnfdNodeTemplateTransformationEngine(serviceTemplateReaderService, serviceTemplate, "test.txt");
        engine.transform();
        Assertions.assertNotNull(engine.getDescriptorResourcePath());
    }

    private String getExpectedResultFor(final String inputFilename) throws IOException {
        try (final InputStream inputStream = getOutputFileResourceCorrespondingTo(inputFilename)) {
            final ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(inputStream, ServiceTemplate.class);
            return yamlUtil.objectToYaml(serviceTemplate);
        }
    }

    private static Path getPathFromClasspath(final String location) {
        return Paths.get(Thread.currentThread().getContextClassLoader().getResource(location).getPath());
    }

    private byte[] getInputFileResource(final String inputFilename) throws IOException {
        return getFileResource(INPUT_DIR + inputFilename);
    }

    private InputStream getOutputFileResourceCorrespondingTo(final String inputFilename) {
        final String outputFilename = getOutputFilenameFrom(inputFilename);
        return getFileResourceAsInputStream(OUTPUT_DIR + outputFilename);
    }

    private String getOutputFilenameFrom(final String inputFilename) {
        return inputFilename.replace("pnfDescriptor", "topologyTemplate");
    }

    private byte[] getFileResource(final String filePath) throws IOException {
        try (final InputStream inputStream = getFileResourceAsInputStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private InputStream getFileResourceAsInputStream(final String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

}