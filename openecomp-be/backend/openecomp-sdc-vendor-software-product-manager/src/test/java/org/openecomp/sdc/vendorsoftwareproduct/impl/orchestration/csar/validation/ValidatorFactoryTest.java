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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;

class ValidatorFactoryTest {

    private String metaFile;
    private FileContentHandler handler;
    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp(){
        validatorFactory = new ValidatorFactory();
        handler = new FileContentHandler();
        metaFile = new StringBuilder()
            .append(TOSCA_META_FILE_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.0").append("\n")
            .append(CSAR_VERSION_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" 1.1").append("\n")
            .append(CREATED_BY_ENTRY.getName())
                .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" Vendor").append("\n")
            .toString();
    }

    @Test
    void testGivenEmptyMetaFile_thenIOExceptionIsThrown() {
        handler.addFile(TOSCA_META_PATH_FILE_NAME, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> validatorFactory.getValidator(handler));
    }

    @Test
    void testGivenEmptyBlock0_thenONAPCsarValidatorIsReturned() throws IOException {
        handler.addFile(TOSCA_META_PATH_FILE_NAME, "onap_csar: true".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, validatorFactory.getValidator(handler).getClass());
    }


    @Test
    void testGivenNonSOL004MetaDirectoryCompliantMetaFile_thenONAPCSARValidatorIsReturned() throws IOException {
        metaFile = metaFile +
                ENTRY_DEFINITIONS.getName() + ATTRIBUTE_VALUE_SEPARATOR.getToken() + TOSCA_DEFINITION_FILEPATH + "\nonap_csar: true";
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFile.getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, validatorFactory.getValidator(handler).getClass());
    }

    @Test
    void testGivenSOL004MetaDirectoryCompliantMetafile_thenONAPCsarValidatorIsReturned() throws IOException {
        metaFile = metaFile +
            ENTRY_DEFINITIONS.getName() + ATTRIBUTE_VALUE_SEPARATOR.getToken() + TOSCA_DEFINITION_FILEPATH + "\n"
            + ETSI_ENTRY_MANIFEST.getName() + ATTRIBUTE_VALUE_SEPARATOR.getToken() + TOSCA_MANIFEST_FILEPATH + "\n"
            + ETSI_ENTRY_CHANGE_LOG.getName() + ATTRIBUTE_VALUE_SEPARATOR.getToken() + TOSCA_CHANGELOG_FILEPATH + "\n";
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFile.getBytes(StandardCharsets.UTF_8));

       assertEquals(SOL004MetaDirectoryValidator.class, validatorFactory.getValidator(handler).getClass());
    }

    @Test
    void testGivenMultiBlockMetadataWithSOL00CompliantMetaFile_thenSOL004MetaDirectoryValidatorReturned()
            throws IOException {
        handler.addFile(TOSCA_META_PATH_FILE_NAME,
            getResourceBytesOrFail("validation.files/metafile/metaFileWithMultipleBlocks.meta"));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(SOL004MetaDirectoryValidator.class, validatorFactory.getValidator(handler).getClass());

    }

    @Test
    void testGetValidators() {
        final List<Validator> validatorList = validatorFactory.getValidators("test");
        assertFalse(validatorList.isEmpty());
        assertEquals(2, validatorList.size());
        assertTrue(validatorList.get(0) instanceof TestModelValidator1);
        assertTrue(validatorList.get(1) instanceof TestModelValidator2);

        final List<Validator> validatorList1 = validatorFactory.getValidators("test1");
        assertTrue(validatorList1.isEmpty());
    }

}
