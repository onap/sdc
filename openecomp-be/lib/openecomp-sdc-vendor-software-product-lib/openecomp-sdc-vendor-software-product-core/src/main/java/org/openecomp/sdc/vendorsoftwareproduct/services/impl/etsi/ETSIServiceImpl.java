/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ARTIFACTS_FOLDER;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.be.config.NonManoConfiguration;
import org.openecomp.sdc.be.config.NonManoConfigurationManager;
import org.openecomp.sdc.be.config.NonManoFolderType;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public class ETSIServiceImpl implements ETSIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETSIServiceImpl.class);

    private final NonManoConfiguration nonManoConfiguration;

    public ETSIServiceImpl() {
        nonManoConfiguration = NonManoConfigurationManager.getInstance().getNonManoConfiguration();
    }

    public ETSIServiceImpl(final NonManoConfiguration nonManoConfiguration) {
        this.nonManoConfiguration = nonManoConfiguration;
    }

    @Override
    public boolean isSol004WithToscaMetaDirectory(FileContentHandler handler) throws IOException {
        final Map<String, byte[]> templates = handler.getFiles();
        return isMetaFilePresent(templates) && hasMetaMandatoryEntries(getMetadata(handler));
    }

    @Override
    public Optional<Map<String, Path>> moveNonManoFileToArtifactFolder(final FileContentHandler handler) throws IOException {
        final Manifest manifest;
        try {
            manifest = getManifest(handler);
        } catch (final IOException ex) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("An error occurred while getting the manifest file", ex);
            }
            throw ex;
        }
        final Path originalManifestPath;
        try {
            originalManifestPath = getOriginalManifestPath(handler);
        } catch (final IOException ex) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("An error occurred while getting the original manifest path", ex);
            }
            throw ex;
        }
        final Map<String, Path> fromToPathMap = new HashMap<>();
        final Map<String, NonManoFolderType> nonManoKeyFolderMapping = nonManoConfiguration.getNonManoKeyFolderMapping();
        manifest.getNonManoSources().entrySet().stream()
            .filter(manifestNonManoSourceEntry -> nonManoKeyFolderMapping.containsKey(manifestNonManoSourceEntry.getKey()))
            .forEach(manifestNonManoSourceEntry -> {
                final NonManoFolderType nonManoFolderType = nonManoKeyFolderMapping.get(manifestNonManoSourceEntry.getKey());
                final List<String> nonManoFileList = manifestNonManoSourceEntry.getValue();
                final Map<String, Path> actualFromToPathMap = nonManoFileList.stream()
                    .map(nonManoFilePath -> {
                        final Path normalizedFilePath = resolveNonManoFilePath(originalManifestPath, nonManoFilePath);
                        final Optional<Path> changedPath = updateNonManoPathInHandler(handler, nonManoFolderType, normalizedFilePath);
                        if (changedPath.isPresent()) {
                            final Map<String, Path> fromAndToPathMap = new HashMap<>();
                            fromAndToPathMap.put(nonManoFilePath, Paths.get(ARTIFACTS_FOLDER).resolve(changedPath.get()));
                            return fromAndToPathMap;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                        fromToPathEntry -> fromToPathEntry.keySet().iterator().next(),
                        fromToPathEntry -> fromToPathEntry.values().iterator().next()
                    ));
                fromToPathMap.putAll(actualFromToPathMap);
            });

        return MapUtils.isEmpty(fromToPathMap) ? Optional.empty() : Optional.of(fromToPathMap);
    }

    /**
     * Resolves the non mano file path based on the original manifest path of the onboarded package.
     *
     * @param originalManifestPath The original path from the onboarded package manifest
     * @param nonManoFilePath The non mano file path defined in the manifest
     * @return The resolved and normalized non mano path.
     */
    private Path resolveNonManoFilePath(final Path originalManifestPath, final String nonManoFilePath) {
        return originalManifestPath.resolve(Paths.get(nonManoFilePath)).normalize();
    }

    /**
     * Updates the non mano file path in the package file handler based on the non mano type.
     *
     * @param handler The package file handler
     * @param nonManoFolderType The Non Mano type of the file to update
     * @param nonManoOriginalFilePath The Non Mano file original path
     * @return The new file path if it was updated in the package file handler, otherwise empty.
     */
    private Optional<Path> updateNonManoPathInHandler(final FileContentHandler handler, final NonManoFolderType nonManoFolderType,
                                                      final Path nonManoOriginalFilePath) {
        final Path fixedSourcePath = fixNonManoPath(nonManoOriginalFilePath);
        final Map<String, byte[]> packageFileMap = handler.getFiles();
        if (packageFileMap.containsKey(fixedSourcePath.toString())) {
            final Path newNonManoPath = Paths.get(nonManoFolderType.getType(), nonManoFolderType.getLocation()
                , fixedSourcePath.getFileName().toString());
            if (!packageFileMap.containsKey(newNonManoPath.toString())) {
                packageFileMap.put(newNonManoPath.toString(), packageFileMap.remove(fixedSourcePath.toString()));
                return Optional.of(newNonManoPath);
            }
        }

        return Optional.empty();
    }

    /**
     * Fix the original non mano file path to the ONAP package file path.
     *
     * Non mano artifacts that were inside the {@link org.openecomp.sdc.tosca.csar.CSARConstants#ARTIFACTS_FOLDER} path
     * are not moved when parsed to ONAP package, but the Manifest declaration can still have the {@link
     * org.openecomp.sdc.tosca.csar.CSARConstants#ARTIFACTS_FOLDER} reference in it. If so, that reference is removed.
     *
     * @param nonManoOriginalFilePath The original non mano file path
     * @return The non mano fixed path to ONAP package structure.
     */
    private Path fixNonManoPath(final Path nonManoOriginalFilePath) {
        final Path rootArtifactsPath = Paths.get("/", ARTIFACTS_FOLDER);
        if (nonManoOriginalFilePath.startsWith(rootArtifactsPath)) {
            return rootArtifactsPath.relativize(nonManoOriginalFilePath);
        }
        final Path relativeArtifactsPath = Paths.get(ARTIFACTS_FOLDER);
        if (nonManoOriginalFilePath.startsWith(relativeArtifactsPath)) {
            return relativeArtifactsPath.relativize(nonManoOriginalFilePath);
        }

        return nonManoOriginalFilePath;
    }

    @Override
    public void updateMainDescriptorPaths(final ToscaServiceModel toscaServiceModel,
                                          final Map<String, Path> fromToMovedArtifactMap) {
        final ServiceTemplate entryDefinition = toscaServiceModel.getServiceTemplates()
            .get(toscaServiceModel.getEntryDefinitionServiceTemplate());
        final YamlUtil yamlUtil = new YamlUtil();
        final String[] entryDefinitionYaml = {yamlUtil.objectToYaml(entryDefinition)};
        fromToMovedArtifactMap.forEach((fromPath, toPath) -> entryDefinitionYaml[0] = entryDefinitionYaml[0]
            .replaceAll(fromPath, toPath.toString()));

        toscaServiceModel.addServiceTemplate(toscaServiceModel.getEntryDefinitionServiceTemplate()
            , yamlUtil.yamlToObject(entryDefinitionYaml[0], ServiceTemplate.class));
    }

    private boolean hasMetaMandatoryEntries(final ToscaMetadata toscaMetadata) {
        final Map<String, String> metaDataEntries = toscaMetadata.getMetaEntries();
        return metaDataEntries.containsKey(TOSCA_META_ENTRY_DEFINITIONS) && metaDataEntries
            .containsKey(TOSCA_META_ETSI_ENTRY_MANIFEST)
            && metaDataEntries.containsKey(TOSCA_META_ETSI_ENTRY_CHANGE_LOG);
    }

    private boolean isMetaFilePresent(Map<String, byte[]> handler) {
        return handler.containsKey(TOSCA_META_PATH_FILE_NAME) || handler.containsKey(TOSCA_META_ORIG_PATH_FILE_NAME);
    }

    public ResourceTypeEnum getResourceType(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = getMetadata(handler);
        Manifest manifest = getManifest(handler, metadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_MANIFEST));
        return getResourceType(manifest);
    }

    public ResourceTypeEnum getResourceType(Manifest manifest) {
        // Valid manifest should contain whether vnf or pnf related metadata data exclusively in SOL004 standard,
        // validation of manifest done during package upload stage
        if (manifest != null && !manifest.getMetadata().isEmpty()
            && MANIFEST_PNF_METADATA.stream().anyMatch(e -> manifest.getMetadata().containsKey(e))) {
            return ResourceTypeEnum.PNF;
        }
        // VNF is default resource type
        return ResourceTypeEnum.VF;
    }

    public Manifest getManifest(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = getMetadata(handler);
        return getManifest(handler, metadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_MANIFEST));
    }

    private Manifest getManifest(FileContentHandler handler, String manifestLocation) throws IOException {
        try (InputStream manifestInputStream = getManifestInputStream(handler, manifestLocation)) {
            Manifest onboardingManifest = new SOL004ManifestOnboarding();
            onboardingManifest.parse(manifestInputStream);
            return onboardingManifest;
        }
    }

    public Path getOriginalManifestPath(final FileContentHandler handler) throws IOException {
        final ToscaMetadata metadata = getOriginalMetadata(handler);
        final String originalMetadataPath = metadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_MANIFEST);
        final Path path = Paths.get(originalMetadataPath);
        return path.getParent() == null ? Paths.get("") : path.getParent();
    }

    private ToscaMetadata getMetadata(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata;
        if (handler.containsFile(TOSCA_META_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata
                .parseToscaMetadataFile(handler.getFileContent(TOSCA_META_PATH_FILE_NAME));
        } else if (handler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata
                .parseToscaMetadataFile(handler.getFileContent(TOSCA_META_ORIG_PATH_FILE_NAME));
        } else {
            throw new IOException("TOSCA.meta file not found!");
        }
        return metadata;
    }

    private ToscaMetadata getOriginalMetadata(final FileContentHandler handler) throws IOException {
        if (handler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            return OnboardingToscaMetadata
                .parseToscaMetadataFile(handler.getFileContent(TOSCA_META_ORIG_PATH_FILE_NAME));
        } else {
            throw new IOException(String.format("%s file not found", TOSCA_META_ORIG_PATH_FILE_NAME));
        }
    }

    private InputStream getManifestInputStream(FileContentHandler handler, String manifestLocation) throws IOException {
        InputStream io;
        if (manifestLocation == null || !handler.containsFile(manifestLocation)) {
            io = handler.getFileContent(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME);
        } else {
            io = handler.getFileContent(manifestLocation);
        }

        if (io == null) {
            throw new IOException("Manifest file not found!");
        }
        return io;
    }

    public NonManoConfiguration getConfiguration() {
        return nonManoConfiguration;
    }
}
