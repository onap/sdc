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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

@RunWith(Parameterized.class)
public class PnfTransformationEngineParameterizedTest {

    private static final String TEST_CASES_PATH = "transformation/pnfParseEngine";
    private static final String TRANSFORMATION_DESCRIPTOR_FOLDER = "transformationDescriptor";
    private static final String OUTPUT_FOLDER = "expectedOutput";
    private static final String DEFAULT_OUTPUT_FILE_NAME = "defaultOutput.yaml";

    private final String inputFileName;
    private final Path inputFilePath;
    private final String outputFileName;
    private final Path outputFilePath;
    private final String transformationDescriptorFileName;
    private final Path transformationDescriptorFilePath;
    private final YamlUtil yamlUtil = new YamlUtil();
    private final ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();

    public PnfTransformationEngineParameterizedTest(final String inputFileName, final Path inputFilePath,
            final String outputFileName, final Path outputFilePath,
            final String transformationDescriptorFileName, final Path transformationDescriptorFilePath) {
        this.inputFileName = inputFileName;
        this.inputFilePath = inputFilePath;
        this.outputFileName = outputFileName;
        this.outputFilePath = outputFilePath;
        this.transformationDescriptorFileName = transformationDescriptorFileName;
        this.transformationDescriptorFilePath = transformationDescriptorFilePath;
    }


    @Parameterized.Parameters(name = "{index}: input: {0}, descriptor: {4}, output: {2}")
    public static Collection input() throws IOException {
        return Files.list(getPathFromClasspath(TEST_CASES_PATH)).map(path -> {
            try {
                return buildTestCase(path);
            } catch (final IOException e) {
                return null;
            }
        }).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private static Collection buildTestCase(final Path testCasePath) throws IOException {
        final Path inputFilePath = Files.list(testCasePath)
            .filter(path -> path.toFile().getName().endsWith("yaml"))
            .findAny().orElse(null);

        if (inputFilePath == null) {
            return Collections.emptyList();
        }
        ;
        final List<Path> transformationDescriptorList;
        try (final Stream<Path> files = Files.walk(testCasePath.resolve(TRANSFORMATION_DESCRIPTOR_FOLDER))) {
            transformationDescriptorList = files.filter(path -> Files.isRegularFile(path))
                .map(path -> Paths.get(TEST_CASES_PATH, testCasePath.getFileName().toString()
                    , TRANSFORMATION_DESCRIPTOR_FOLDER, path.getFileName().toString()))
                .collect(Collectors.toList());
        }

        final List<Path> outputList;
        try (final Stream<Path> files = Files.walk(testCasePath.resolve(OUTPUT_FOLDER))) {
            outputList = files.filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
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
                testCaseList.add(new Object[] {inputFilePath.toFile().getName(), inputFilePath,
                    outputPath.toFile().getName(), outputPath,
                    transformationDescriptorPath.toFile().getName(), transformationDescriptorPath});
            }
        }

        return testCaseList;

    }

    @Test
    public void testTopologyTemplateConversions() throws IOException {
        final byte[] descriptor = Files.readAllBytes(inputFilePath);
        final ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        final ServiceTemplate serviceTemplate = new ServiceTemplate();

        final PnfdTransformationEngine pnfdTransformationEngine = new PnfdTransformationEngine(serviceTemplateReaderService, serviceTemplate
            , transformationDescriptorFilePath.toString());
        pnfdTransformationEngine.transform();

        final String result = yamlUtil.objectToYaml(serviceTemplate);
        final String expectedResult = parseToYaml(outputFilePath);
        assertEquals(expectedResult, result);
    }

    private String parseToYaml(final Path filePath) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(filePath)) {
            ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(inputStream, ServiceTemplate.class);
            return yamlUtil.objectToYaml(serviceTemplate);
        }
    }

    private static Path getPathFromClasspath(final String location) {
        return Paths.get(Thread.currentThread().getContextClassLoader().getResource(location).getPath());
    }
}