/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;


public class ToscaSolConverterVnfTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaSolConverterVnfTest.class);

    private AbstractToscaSolConverter toscaSolConverter;
    private FileContentHandler fileContentHandler;

    @Before
    public void setUp() {
        toscaSolConverter = new ToscaSolConverterVnf();
        fileContentHandler = new FileContentHandler();
    }

    @Test
    public void testGivenSOL004WithMetadataDirectoryPackage_whenToscaSolConverterIsCalled_validToscaServiceModelIsReturned() {
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta",
            ("TOSCA-Meta-File-Version: 1.0\n " +
                "CSAR-Version: 1.1\n" +
                "Created-By: Ericsson\n" +
                "Entry-Definitions: Definitions/Main.yaml\n" +
                "Entry-Manifest: Main.mf\n" +
                "Entry-Change-Log: Artifacts/ChangeLog.txt")
                .getBytes(StandardCharsets.UTF_8));

        final String mainServiceTemplate = "Main.yaml";
        final String mainManifest = "Main.mf";

        fileContentHandler.addFile("Definitions/" + mainServiceTemplate, getFileResource("/toscaSOlConverter/Main.yaml"));
        fileContentHandler.addFile(mainManifest, "".getBytes());
        fileContentHandler.addFile("Definitions/sample_import1.yaml", getFileResource("/toscaSOlConverter/sample_import1.yaml"));
        fileContentHandler.addFile("Definitions/sample_import2.yaml", getFileResource("/toscaSOlConverter/sample_import2.yaml"));
        fileContentHandler.addFile("Artifacts/sample_import3.yaml", getFileResource("/toscaSOlConverter/sample_import3.yaml"));
        fileContentHandler.addFile("Artifacts/sample_import4.yaml", getFileResource("/toscaSOlConverter/sample_import4.yaml"));
        fileContentHandler.addFile("sample_import5.yaml", getFileResource("/toscaSOlConverter/sample_import3.yaml"));

        final ToscaServiceModel toscaServiceModel = convertToscaSol();
        final FileContentHandler contentHandler = toscaServiceModel.getArtifactFiles();
        final Map<String, ServiceTemplate> serviceTemplateMap = toscaServiceModel.getServiceTemplates();
        final String entryDefinitionTemplateName = toscaServiceModel.getEntryDefinitionServiceTemplate();
        Assert.assertTrue("Artifacts should contain external files", contentHandler.containsFile(mainManifest));
        Assert.assertTrue("Main service template should exist", serviceTemplateMap.containsKey(mainServiceTemplate));
        Assert.assertEquals("Entry Definition name should be same as passed in TOSCA.meta",
            mainServiceTemplate, entryDefinitionTemplateName);
    }

    @Test(expected = RuntimeException.class)
    public void testGivenSOL004InvalidDirectoryPackage_whenToscaSolConverterIsCalled_exceptionIsExpected() {
        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta",
            ("TOSCA-Meta-File-Version: 1.0\n " +
                "CSAR-Version: 1.1\n" +
                "Created-by: Ericsson\n" +
                "Entry-Definitions: Definitions/Main.yaml\n" +
                "Entry-Manifest: Main.mf\n" +
                "Entry-Change-Log: Artifacts/ChangeLog.txt")
                .getBytes(StandardCharsets.UTF_8));

        fileContentHandler.addFile("Definitions/Main.yaml", getFileResource("/toscaSOlConverter/Main.yaml"));
        fileContentHandler.addFile("Main.mf", "".getBytes());
        fileContentHandler.addFile("Definitions/sample_import1.yaml", getFileResource("/toscaSOlConverter/sample_import3.yaml"));

        convertToscaSol();
    }

    @Test(expected = IOException.class)
    public void testGivenMetaFileDoesNotExist_thenAnExceptionIsThrown() throws IOException {
        toscaSolConverter.convert(fileContentHandler);
    }

    @Test(expected = CoreException.class)
    public void testGivenInvalidServiceTemplate_thenAnExceptionIsThrown() {

        fileContentHandler.addFile("TOSCA-Metadata/TOSCA.meta",
            ("TOSCA-Meta-File-Version: 1.0\n " +
                "CSAR-Version: 1.1\n" +
                "Created-By: Ericsson\n" +
                "Entry-Definitions: Definitions/Main.yaml\n" +
                "Entry-Manifest: Main.mf\n" +
                "Entry-Change-Log: Artifacts/ChangeLog.txt")
                .getBytes(StandardCharsets.UTF_8));

        fileContentHandler.addFile("Definitions/Main.yaml", getFileResource("/toscaSOlConverter/invalidMainService.yaml"));
        convertToscaSol();
    }

    private ToscaServiceModel convertToscaSol() {
        try {
            return toscaSolConverter.convert(fileContentHandler);
        } catch (final IOException e) {
            final String errorMsg = "Could convert file content handler";
            LOGGER.error(errorMsg, e);
            fail(errorMsg);
        }
        return null;
    }

    private byte[] getFileResource(final String filePath) {
        try (final InputStream inputStream = this.getClass().getResourceAsStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (final IOException ex) {
            fail(String.format("Could not load file: %s", filePath));
        }

        return null;
    }

}
