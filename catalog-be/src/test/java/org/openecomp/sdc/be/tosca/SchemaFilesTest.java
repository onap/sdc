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

package org.openecomp.sdc.be.tosca;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.test.util.TestResourcesHandler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

class SchemaFilesTest {

    @Test
    void testValidateYamlNormativeFiles() {
        String importToscaPath = "src/main/resources/import/tosca";
        assertTrue(checkValidYamlInFileTree(importToscaPath));
    }

    @Test
    void testRainyYamlNormativeFiles() {
        String importToscaPathTest = "src/test/resources/yamlValidation";
        assertFalse(checkValidYamlInFileTree(importToscaPathTest));
    }

    private boolean checkValidYamlInFileTree(final String fileTree) {
        AtomicBoolean ret = new AtomicBoolean(true);
        try (final Stream<Path> pathStream = Files.walk(Paths.get(fileTree))) {
            pathStream
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".yml"))
                .forEach(yamlFile -> {
                    try {
                        new Yaml().load(new FileInputStream(yamlFile.toAbsolutePath().toString()));
                    } catch (final Exception e) {
                        System.out.println("Not valid yaml in file creation : " + yamlFile.toAbsolutePath());
                        ret.set(false);
                    }
                });
        } catch (final IOException e) {
            System.out.println("Error in reading file from folder : " + fileTree);
            return false;
        }
        return ret.get();
    }

    @Test
    void yamlValidation_test_no_valid() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonSchemaFactory factory =
            JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)).objectMapper(mapper).build();

        try (final Stream<Path> pathStream = Files.walk(Paths.get("src/test/resources/yamlValidation"))) {
            pathStream
                .filter(path -> path.getFileName().toString().toLowerCase().startsWith("data_types_no-valid-"))
                .forEach(path -> {
                    try (final InputStream schemaFile = TestResourcesHandler.getResourceAsStream("yamlValidation/noValid/" + "schema.json");
                        final InputStream yamlFile = TestResourcesHandler.getResourceAsStream("yamlValidation/noValid/" + path.getFileName())) {
                        final Set<ValidationMessage> validationMessages = factory.getSchema(schemaFile).validate(mapper.readTree(yamlFile));
                        validationMessages.forEach(System.out::println);
                        assertFalse(validationMessages.isEmpty());
                    } catch (JsonParseException e) {
                        assertTrue(e.getCause() instanceof ParserException || e.getCause() instanceof ScannerException);
                    } catch (IOException e) {
                        fail(e.getMessage());
                    }
                });
        } catch (final IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void yamlValidation_test_valid() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JsonSchemaFactory factory =
            JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)).objectMapper(mapper).build();

        final String dir = "yamlValidation/noValid/";
        try (final InputStream schemaFile = TestResourcesHandler.getResourceAsStream(dir + "schema.json");
            final InputStream yamlFile = TestResourcesHandler.getResourceAsStream(dir + "data_types_valid.yaml")) {
            Set<ValidationMessage> validationMessages = factory.getSchema(schemaFile).validate(mapper.readTree(yamlFile));
            validationMessages.forEach(System.out::println);
            assertTrue(validationMessages.isEmpty());
        } catch (final IOException e) {
            fail(e.getMessage());
        }
    }

}
