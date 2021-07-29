/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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

import static org.openecomp.core.converter.datatypes.Constants.GLOBAL_ST_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public class ToscaModelConverter extends AbstractToscaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaModelConverter.class);

    private final Set<String> handledDefinitionFilesList = new HashSet<>();
    private ToscaServiceModel toscaServiceModel;
    private Map<String, ServiceTemplate> serviceTemplateMap;
    private FileContentHandler csarFileContentHandler;
    private FileContentHandler artifactFileContentHandler;
    private Map<String, byte[]> csarFileMap;
    private GlobalSubstitutionServiceTemplate globalSubstitutionServiceTemplate;
    private String mainDefinitionFilePath;

    @Override
    public ToscaServiceModel convert(final FileContentHandler fileContentHandler) throws IOException {
        init(fileContentHandler);
        handleMainServiceTemplate();
        handleExternalArtifacts();
        handleMetadataFile(csarFileMap);
        updateToscaServiceModel();
        return toscaServiceModel;
    }

    private void init(final FileContentHandler fileContentHandler) throws IOException {
        csarFileContentHandler = fileContentHandler;
        csarFileMap = new HashMap<>(fileContentHandler.getFiles());
        toscaServiceModel = new ToscaServiceModel();
        serviceTemplateMap = new HashMap<>();
        artifactFileContentHandler = new FileContentHandler();
        globalSubstitutionServiceTemplate = new GlobalSubstitutionServiceTemplate();
        mainDefinitionFilePath = getMainServiceDefinitionFileName();
    }

    @Override
    public void convertTopologyTemplate(final ServiceTemplate serviceTemplate, final ServiceTemplateReaderService readerService) {
        new VnfTopologyTemplateConverter().convertTopologyTemplate(serviceTemplate, readerService);
    }

    private void handleMainServiceTemplate() {
        if (mainDefinitionFilePath == null) {
            return;
        }
        final String mainServiceTemplateFileName = FilenameUtils.getName(mainDefinitionFilePath);
        handleServiceTemplate(mainServiceTemplateFileName, mainDefinitionFilePath, csarFileMap, serviceTemplateMap);
        handleImportDefinitions(mainDefinitionFilePath);
    }

    private void handleExternalArtifacts() {
        if (MapUtils.isEmpty(csarFileMap)) {
            return;
        }
        csarFileMap.entrySet().stream()
            .filter(fileEntry -> !handledDefinitionFilesList.contains(fileEntry.getKey()) && !isMetadataFile(fileEntry.getKey()))
            .forEach(fileEntry -> {
                if (isGlobalServiceTemplate(fileEntry.getKey())) {
                    handleServiceTemplate(GLOBAL_ST_NAME, fileEntry.getKey(), csarFileMap, serviceTemplateMap);
                } else {
                    artifactFileContentHandler.addFile(getConcreteArtifactFileName(fileEntry.getKey()), fileEntry.getValue());
                }
            });
    }

    private void handleImportDefinitions(final String fileName) {
        final var toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(csarFileMap, fileName);
        if (toscaDefinitionImportHandler.hasError()) {
            throw new InvalidToscaDefinitionImportException(toscaDefinitionImportHandler.getErrors());
        }
        final Map<String, ServiceTemplateReaderService> handledImportDefinitionFileMap =
            toscaDefinitionImportHandler.getHandledImportDefinitionFileMap();
        handledDefinitionFilesList.addAll(handledImportDefinitionFileMap.keySet());
        handledDefinitionFilesList.forEach(file -> handleDefinitionTemplate(file, csarFileMap, globalSubstitutionServiceTemplate));
    }

    private String getMainServiceDefinitionFileName() throws IOException {
        try {
            var toscaMetadata = OnboardingToscaMetadata
                .parseToscaMetadataFile(csarFileContentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
            return toscaMetadata.getMetaEntries().get(ENTRY_DEFINITIONS.getName());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }

    private void updateToscaServiceModel() {
        final String mainDefinitionSimpleName = FilenameUtils.getName(mainDefinitionFilePath);
        updateToscaServiceModel(toscaServiceModel, serviceTemplateMap, artifactFileContentHandler, globalSubstitutionServiceTemplate, csarFileMap,
            mainDefinitionSimpleName);
    }

}
