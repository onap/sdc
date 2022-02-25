/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.sdc.tosca.services.impl;

import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CREATED_BY_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.CSAR_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.TOSCA_META_FILE_VERSION_ENTRY;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AsdManifestOnboarding;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.exceptions.CsarCreationErrorBuilder;
import org.openecomp.sdc.tosca.exceptions.CsarMissingEntryPointErrorBuilder;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;

public class ToscaFileOutputServiceCsarImpl implements ToscaFileOutputService {

    static final String EXTERNAL_ARTIFACTS_FOLDER_NAME = "Artifacts";
    private static final String DEFINITIONS_FOLDER_NAME = "Definitions";
    private static final String ARTIFACTS_FOLDER_NAME = "Artifacts";
    //todo currently duplicated, to be changed when external artifacts are separated from internal
    private static final String TOSCA_META_FOLDER_NAME = "TOSCA-Metadata";
    private static final String TOSCA_META_FILE_VERSION_VALUE = "1.0";
    private static final String TOSCA_META_FILE_NAME = "TOSCA.meta";
    private static final String CSAR_VERSION_VALUE = "1.1";
    private static final String CREATED_BY_VALUE = "ASDC Onboarding portal";
    private static final String META_FILE_DELIMITER = ":";
    private static final String SPACE = " ";
    private static final String FILE_SEPARATOR = File.separator;
    private static final Logger logger = LoggerFactory.getLogger(ToscaFileOutputServiceCsarImpl.class);

    @Override
    public byte[] createOutputFile(ToscaServiceModel toscaServiceModel, FileContentHandler externalArtifacts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(baos))) {
            packDefinitions(zos, toscaServiceModel.getServiceTemplates());
            FileContentHandler artifactFiles = toscaServiceModel.getArtifactFiles();
            if (artifactFiles != null && !artifactFiles.isEmpty()) {
                packArtifacts(zos, artifactFiles);
            }
            if (toscaServiceModel.getEntryDefinitionServiceTemplate() == null) {
                throw new CoreException(new CsarMissingEntryPointErrorBuilder().build());
            }
            createAndPackToscaMetaFile(zos, toscaServiceModel.getEntryDefinitionServiceTemplate(), isAsdPackage(artifactFiles));
            if (externalArtifacts != null) {
                packExternalArtifacts(zos, externalArtifacts);
            }
        } catch (IOException ex) {
            throw new CoreException(new CsarCreationErrorBuilder().build(), ex);
        }
        return baos.toByteArray();
    }

    @Override
    public String createMetaFile(String entryDefinitionsFileName) {
        return TOSCA_META_FILE_VERSION_ENTRY.getName() + META_FILE_DELIMITER + SPACE + TOSCA_META_FILE_VERSION_VALUE + System.lineSeparator()
            + CSAR_VERSION_ENTRY.getName() + META_FILE_DELIMITER + SPACE + CSAR_VERSION_VALUE + System.lineSeparator() + CREATED_BY_ENTRY.getName()
            + META_FILE_DELIMITER + SPACE + CREATED_BY_VALUE + System.lineSeparator() + ENTRY_DEFINITIONS.getName() + META_FILE_DELIMITER + SPACE
            + DEFINITIONS_FOLDER_NAME + FILE_SEPARATOR + entryDefinitionsFileName;
    }

    @Override
    public String getArtifactsFolderName() {
        return ARTIFACTS_FOLDER_NAME;
    }

    private void createAndPackToscaMetaFile(ZipOutputStream zos, String entryDefinitionsFileName, boolean isAsdPackage) throws IOException {
        String metaFile = createMetaFile(entryDefinitionsFileName);
        metaFile += isAsdPackage ? System.lineSeparator() + ENTRY_DEFINITION_TYPE.getName() + META_FILE_DELIMITER + SPACE + "asd" : "";
        zos.putNextEntry(new ZipEntry(TOSCA_META_FOLDER_NAME + FILE_SEPARATOR + TOSCA_META_FILE_NAME));
        writeBytesToZip(zos, new ByteArrayInputStream(metaFile.getBytes()));
    }

    private void packDefinitions(ZipOutputStream zos, Map<String, ServiceTemplate> serviceTemplates) throws IOException {
        for (Map.Entry<String, ServiceTemplate> serviceTemplate : serviceTemplates.entrySet()) {
            String fileName = serviceTemplate.getKey();
            zos.putNextEntry(new ZipEntry(DEFINITIONS_FOLDER_NAME + FILE_SEPARATOR + fileName));
            writeBytesToZip(zos, FileUtils.convertToInputStream(serviceTemplate.getValue(), FileUtils.FileExtension.YAML));
        }
    }

    private void packExternalArtifacts(ZipOutputStream zos, FileContentHandler externalArtifacts) {
        for (String filenameIncludingPath : externalArtifacts.getFileList()) {
            try {
                zos.putNextEntry(new ZipEntry(filenameIncludingPath));
                writeBytesToZip(zos, externalArtifacts.getFileContentAsStream(filenameIncludingPath));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    zos.closeEntry();
                } catch (IOException ignore) {
                    logger.debug(ignore.getMessage(), ignore);
                }
            }
        }
    }

    private void packArtifacts(ZipOutputStream zos, FileContentHandler artifacts) {
        for (String fileName : artifacts.getFileList()) {
            try {
                zos.putNextEntry(new ZipEntry(ARTIFACTS_FOLDER_NAME + FILE_SEPARATOR + fileName));
                writeBytesToZip(zos, artifacts.getFileContentAsStream(fileName));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    zos.closeEntry();
                } catch (IOException ignore) {
                    logger.debug(ignore.getMessage(), ignore);
                }
            }
        }
    }

    private void writeBytesToZip(ZipOutputStream zos, InputStream is) throws IOException {
        if (is != null) {
            IOUtils.copy(is, zos);
        }
    }

    private boolean isAsdPackage(final FileContentHandler fileContentHandler) throws IOException {
        if (null == fileContentHandler) {
            return false;
        }
        final Manifest manifest = loadAsdManifest(fileContentHandler);
        return null != manifest && manifest.getMetadata().entrySet().stream()
                .anyMatch(manifestEntry -> ENTRY_DEFINITION_TYPE.getName().equalsIgnoreCase(manifestEntry.getKey()));
    }

    private Manifest loadAsdManifest(final FileContentHandler handler) throws IOException {
        return getAsdManifest(handler);
    }

    private Manifest getAsdManifest(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = getAsdMetadata(handler);
        return null != metadata ? getAsdManifest(handler, getEntryManifestLocation(metadata)) : null;
    }

    private ToscaMetadata getAsdMetadata(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = null;
        if (handler.containsFile(TOSCA_META_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } else if (handler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_ORIG_PATH_FILE_NAME));
        }
        return metadata;
    }

    private String getEntryManifestLocation(final ToscaMetadata metadata) {
        return metadata.getMetaEntries().containsKey(ETSI_ENTRY_MANIFEST.getName()) ?
                metadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName()):
                metadata.getMetaEntries().get(ENTRY_MANIFEST.getName());
    }

    private Manifest getAsdManifest(FileContentHandler handler, String manifestLocation) throws IOException {
        try (InputStream manifestInputStream = getAsdManifestInputStream(handler, manifestLocation)) {
            Manifest onboardingManifest = new AsdManifestOnboarding();
            onboardingManifest.parse(manifestInputStream);
            return onboardingManifest;
        }
    }

    private InputStream getAsdManifestInputStream(FileContentHandler handler, String manifestLocation) {
        InputStream io;
        if (manifestLocation == null || !handler.containsFile(manifestLocation)) {
            io = handler.getFileContentAsStream(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME);
        } else {
            io = handler.getFileContentAsStream(manifestLocation);
        }
        return io;
    }
}
