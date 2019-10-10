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

package org.openecomp.core.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

public class ToscaDefinitionImportHandlerTest {

    private static final Path RESOURCES_FILE_PATH = Paths.get("toscaDefinitionImportHandler");
    private Map<String, byte[]> descriptorFileMap;

    @Before
    public void setUp() {
        descriptorFileMap = new HashMap<>();
    }

    /**
     * Tests correct descriptor files.
     */
    @Test
    public void testGivenDescriptorFiles_whenMainDescriptorImportsAreHandled_allDescriptorsAreProcessedWithoutError() {
        final List<String> filesToHandleList = Arrays.asList("Definitions/Main.yaml", "Definitions/descriptorBasicImport.yaml",
            "Definitions/descriptorWithRelativePaths.yaml", "Artifacts/descriptorWithAbsolutePaths.yaml",
            "Artifacts/descriptorCyclicReference.yaml");

        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            "Definitions/Main.yaml");
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
        assertThat("No errors should be detected", validationErrorList, hasSize(0));
    }

    /**
     * Tests an empty package.
     */
    @Test
    public void testGivenEmptyPackage_whenMainDescriptorIsHandled_aMissingFileErrorIsReported() {
        final List<String> filesToHandleList = Collections.emptyList();

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            "Definitions/Main.yaml");
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            Messages.MISSING_IMPORT_FILE.formatMessage("Definitions/Main.yaml")));

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

    /**
     * Tests a file imported in a descriptor but missing in the package.
     */
    @Test
    public void testGivenOneMissingDescriptorFile_whenMainDescriptorImportsAreHandled_aMissingFileErrorIsReported() {
        final List<String> filesToHandleList = Arrays.asList("Definitions/Main.yaml",
            "Definitions/descriptorBasicImport.yaml", "Definitions/descriptorWithRelativePaths.yaml",
            "Artifacts/descriptorWithAbsolutePaths.yaml");
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            String.format(Messages.MISSING_IMPORT_FILE.getErrorMessage(), "Artifacts/descriptorCyclicReference.yaml")));

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            "Definitions/Main.yaml");
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

    /**
     * Tests a descriptor with invalid import statements.
     */
    @Test
    public void testGivenDescriptorWithInvalidImportStatement_whenMainDescriptorImportsAreHandled_aInvalidImportStatementErrorIsReported() {
        final String mainDefinitionFile = "Definitions/MainWithInvalidImportedFile.yaml";

        final List<String> filesToHandleList = Arrays.asList(mainDefinitionFile,
            "Definitions/descriptorInvalidImportStatement.yaml");
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            Messages.INVALID_IMPORT_STATEMENT.formatMessage("Definitions/descriptorInvalidImportStatement.yaml", "null")));

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            mainDefinitionFile);
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

    /**
     * Tests an invalid main descriptor file path.
     */
    @Test
    public void testGivenInvalidMainDescriptorFilePath_whenDescriptorIsHandled_aMissingImportErrorIsReported() {
        final String mainDefinitionFilePath = "Definitions/Main1.yaml";
        final String invalidMainDefinitionFilePath = "../Definitions/InvalidMainDefinitionFile.yaml";

        final List<String> filesToHandleList = Arrays.asList(mainDefinitionFilePath);
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR, Messages.MISSING_IMPORT_FILE.formatMessage(invalidMainDefinitionFilePath)));

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            invalidMainDefinitionFilePath);
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("No files should be handled", actualHandledFiles, hasSize(0));

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();

        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

    /**
     * Tests a descriptor with invalid yaml.
     */
    @Test
    public void testGivenInvalidYamlDescriptorFile_whenDescriptorIsHandled_aInvalidYamlFormatErrorIsReported() {
        final String mainDefinitionFile = "Definitions/descriptorInvalid.yaml";

        final List<String> filesToHandleList = Arrays.asList(mainDefinitionFile);
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR, String.format(Messages.INVALID_YAML_FORMAT.getErrorMessage()
            , "while scanning a simple key\n"
                + " in 'string', line 5, column 3:\n"
                + "      template_author= onap\n"
                + "      ^\n"
                + "could not find expected ':'\n"
                + " in 'string', line 6, column 1:\n"
                + "    description: vCPE_vgw\n"
                + "    ^\n")));

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            mainDefinitionFile);
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("No files should be handled", actualHandledFiles, hasSize(0));

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();

        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

    /**
     * Tests all forms of import statements.
     */
    @Test
    public void testGivenDescriptorFiles_whenMainDescriptorWithDifferentImportStatementsIsHandled_noErrorsAreReported() {
        final String mainDefinitionFile = "Definitions/descriptorFileWithValidImportStatements.yaml";

        final List<String> filesToHandleList = Arrays.asList(mainDefinitionFile, "Artifacts/descriptorCyclicReference.yaml");
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler =
            new ToscaDefinitionImportHandler(descriptorFileMap, mainDefinitionFile);
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();
        assertThat("No errors should be detected", validationErrorList, hasSize(0));
    }

    /**
     * Tests a descriptor with nonexistent import paths.
     */
    @Test
    public void testGivenDescriptorFileWithNonexistentRelativeImport_whenIncorrectMainDescriptorIsHandled_aMissingFileErrorIsReported() {
        final String mainDefinitionFile = "Definitions/MainWithNonexistentReferences.yaml";

        final List<String> filesToHandleList = Arrays.asList(mainDefinitionFile,
            "Definitions/descriptorNonexistentImport.yaml", "Artifacts/descriptorCyclicReference.yaml");
        filesToHandleList.forEach(file ->
            descriptorFileMap.put(file, getResourceBytesOrFail(RESOURCES_FILE_PATH.resolve(file)))
        );

        final List<ErrorMessage> expectedErrorList = new ArrayList<>();
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            String.format(Messages.MISSING_IMPORT_FILE.getErrorMessage(),
                "Definitions/descriptorCyclicReference.yaml"))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            String.format(Messages.MISSING_IMPORT_FILE.getErrorMessage(),
                "Definitions/descriptorCyclicReference.yaml"))
        );
        expectedErrorList.add(new ErrorMessage(ErrorLevel.ERROR,
            String.format(Messages.MISSING_IMPORT_FILE.getErrorMessage(),
                "Definitions/descriptorCyclicReference.yaml"))
        );

        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(
            descriptorFileMap,
            mainDefinitionFile);
        final Set<String> actualHandledFiles = toscaDefinitionImportHandler.getHandledDefinitionFilesList();

        assertThat("The handled files should be the same", actualHandledFiles, hasSize(filesToHandleList.size()));
        assertThat("The handled files should be the same"
            , actualHandledFiles, containsInAnyOrder(filesToHandleList.toArray(new String[0]))
        );

        final List<ErrorMessage> validationErrorList = toscaDefinitionImportHandler.getErrors();

        assertThat("The errors should be the same", validationErrorList, hasSize(expectedErrorList.size()));
        assertThat("The errors should be the same"
            , validationErrorList, containsInAnyOrder(expectedErrorList.toArray(new ErrorMessage[0]))
        );
    }

}