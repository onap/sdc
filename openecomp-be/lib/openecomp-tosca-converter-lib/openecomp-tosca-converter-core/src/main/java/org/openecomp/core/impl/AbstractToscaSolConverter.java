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

import static org.openecomp.core.converter.datatypes.Constants.globalStName;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;


public abstract class AbstractToscaSolConverter extends AbstractToscaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractToscaSolConverter.class);
    private final Set<String> handledDefinitionFilesList = new HashSet<>();

    @Override
    public ToscaServiceModel convert(FileContentHandler fileContentHandler) throws IOException {
        Map<String, byte[]> csarFiles = new HashMap<>(fileContentHandler.getFiles());
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
        FileContentHandler artifacts = new FileContentHandler();
        GlobalSubstitutionServiceTemplate gsst = new GlobalSubstitutionServiceTemplate();
        String mServiceDefinitionPath = getMainServiceDefinitionFileName(fileContentHandler);
        handleMainServiceTemplate(csarFiles, serviceTemplates, gsst, mServiceDefinitionPath);
        handleExternalArtifacts(csarFiles, serviceTemplates, artifacts);
        handleMetadataFile(csarFiles);
        updateToscaServiceModel(toscaServiceModel, serviceTemplates, artifacts, gsst, csarFiles, getSimpleName(mServiceDefinitionPath));
        return toscaServiceModel;
    }

    private void handleMainServiceTemplate(Map<String, byte[]> csarFiles, Map<String, ServiceTemplate> serviceTemplates,
                                           GlobalSubstitutionServiceTemplate gsst, String mServiceDefinitionFileName) {
        if (mServiceDefinitionFileName != null) {
            handleServiceTemplate(getSimpleName(mServiceDefinitionFileName), mServiceDefinitionFileName, csarFiles, serviceTemplates);
            handleImportDefinitions(mServiceDefinitionFileName, csarFiles, gsst);
        }
    }

    private void handleExternalArtifacts(Map<String, byte[]> csarFiles, Map<String, ServiceTemplate> serviceTemplates, FileContentHandler artifacts) {
        for (Map.Entry<String, byte[]> fileEntry : csarFiles.entrySet()) {
            if (!handledDefinitionFilesList.contains(fileEntry.getKey()) && !isMetadataFile(fileEntry.getKey())) {
                if (isGlobalServiceTemplate(fileEntry.getKey())) {
                    handleServiceTemplate(globalStName, fileEntry.getKey(), csarFiles, serviceTemplates);
                } else {
                    artifacts.addFile(
                            getConcreteArtifactFileName(fileEntry.getKey()), fileEntry.getValue());
                }
            }
        }
    }

    private void handleImportDefinitions(final String fileName, final Map<String, byte[]> csarFiles
        , final GlobalSubstitutionServiceTemplate gsst) {
        final ToscaDefinitionImportHandler toscaDefinitionImportHandler = new ToscaDefinitionImportHandler(csarFiles, fileName);
        if (toscaDefinitionImportHandler.hasError()) {
            throw new InvalidToscaDefinitionImportException(toscaDefinitionImportHandler.getErrors());
        }
        final Map<String, ServiceTemplateReaderService> handledImportDefinitionFileMap =
            toscaDefinitionImportHandler.getHandledImportDefinitionFileMap();
        handledDefinitionFilesList.addAll(handledImportDefinitionFileMap.keySet());
        for (final String file : handledDefinitionFilesList) {
            handleDefinitionTemplate(file, csarFiles, gsst);
        }
    }

    private String getMainServiceDefinitionFileName(FileContentHandler contentHandler) throws IOException {
        try {
            ToscaMetadata toscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(
                    contentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME.getName()));
            return toscaMetadata.getMetaEntries().get(ENTRY_DEFINITIONS.getName());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
    }

    private String getSimpleName(String path) {
        if (path != null && path.contains("/")) {
            path = path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

}