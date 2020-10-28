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

import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.pnfd.PnfdTransformationEngine;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class PnfTransformationEngineParameterizedTest {

    private static final String TEST_CASES_PATH = "transformation/pnfParseEngine";
    private static final String TRANSFORMATION_DESCRIPTOR_FOLDER = "transformationDescriptor";
    private static final String OUTPUT_FOLDER = "expectedOutput";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "defaultOutput.yaml";

    private final YamlUtil yamlUtil = new YamlUtil();
    private final ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();

    private static Collection<Object[]> buildTestCase(final Path testCasePath) throws IOException {
        final Path inputFilePath = Files.list(testCasePath)
            .filter(path -> path.toFile().getName().endsWith("yaml"))
            .findAny().orElse(null);

        if (inputFilePath == null) {
            return Collections.emptyList();
        }
        final List<Path> transformationDescriptorList;
        try (final Stream<Path> files = Files.walk(testCasePath.resolve(TRANSFORMATION_DESCRIPTOR_FOLDER))) {
            transformationDescriptorList = files.filter(Files::isRegularFile)
                .map(path -> Paths.get(TEST_CASES_PATH, testCasePath.getFileName().toString()
                    , TRANSFORMATION_DESCRIPTOR_FOLDER, path.getFileName().toString()))
                .collect(Collectors.toList());
        }

        final List<Path> outputList;
        try (final Stream<Path> files = Files.walk(testCasePath.resolve(OUTPUT_FOLDER))) {
            outputList = files.filter(Files::isRegularFile).collect(Collectors.toList());
        }
        final Path defaultOutput = outputList.stream()
            .filter(path -> path.toFile().getName().equals(DEFAULT_OUTPUT_FILE_NAME))
            .findFirst().orElse(null);

        final List<Object[]> testCaseList = new ArrayList<>();

        for (final Path transformationDescriptorPath : transformationDescriptorList) {
            final Path outputPath = outputList.stream()
                .filter(path -> path.toFile().getName().equals(transformationDescriptorPath.toFile().getName()))
                .findFirst().orElse(defaultOutput);
            if (outputPath != null) {
                testCaseList.add(new Object[]{ inputFilePath, outputPath, transformationDescriptorPath});
            }
        }
        return testCaseList;
    }

    @Test
    public void testTopologyTemplateConversions() throws IOException, URISyntaxException {
        Files.list(getPathFromClasspath()).forEach(testCasePath -> {
            try {
                var paths = buildTestCase(testCasePath);
                paths.forEach(p -> {
                    try {
                        final ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(Files.readAllBytes((Path) p[0]));
                        final ServiceTemplate serviceTemplate = new ServiceTemplate();

                        final PnfdTransformationEngine pnfdTransformationEngine = new PnfdNodeTemplateTransformationEngine(
                                serviceTemplateReaderService, serviceTemplate, p[2].toString());
                        pnfdTransformationEngine.transform();

                        final String result = yamlUtil.objectToYaml(serviceTemplate);
                        final String expectedResult = parseToYaml((Path) p[1]);
                        assertEquals(expectedResult, result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );
    }

    private String parseToYaml(final Path filePath) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(filePath)) {
            ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(inputStream, ServiceTemplate.class);
            return yamlUtil.objectToYaml(serviceTemplate);
        }
    }

    private static Path getPathFromClasspath() throws URISyntaxException {
        return Paths.get(PnfTransformationEngineParameterizedTest.class.getClassLoader().getResource(PnfTransformationEngineParameterizedTest.TEST_CASES_PATH).toURI());
    }
}