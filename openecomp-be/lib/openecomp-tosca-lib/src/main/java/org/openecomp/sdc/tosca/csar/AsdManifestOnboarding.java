/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
 *
 *
 */
package org.openecomp.sdc.tosca.csar;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

/**
 * Processes a ASD Manifest.
 */
public class AsdManifestOnboarding extends SOL004ManifestOnboarding {


    public static boolean isAsdPackage(final FileContentHandler fileContentHandler) throws IOException {
        final Manifest manifest = loadAsdManifest(fileContentHandler);
        return null != manifest && manifest.getMetadata().entrySet().stream()
                .anyMatch(manifestEntry -> ENTRY_DEFINITION_TYPE.getToken().equalsIgnoreCase(manifestEntry.getKey()));
    }

    protected boolean isMetadataEntry(final String metadataEntry) {
        final Optional<ManifestTokenType> manifestTokenType = ManifestTokenType.parse(metadataEntry);
        return manifestTokenType.map(ManifestTokenType::isMetadataAsdEntry).orElse(false);
    }

    /**
     * Processes {@link ManifestTokenType#SOURCE} entries in {@link ManifestTokenType#NON_MANO_ARTIFACT_SETS}.
     *
     * @return A list of sources paths
     */
    protected List<String> readNonManoSourceList() {
        final List<String> nonManoSourceList = new ArrayList<>();
        while (getCurrentLine().isPresent()) {
            final ManifestTokenType manifestTokenType = detectLineEntry().orElse(null);
            if (!(manifestTokenType == ManifestTokenType.SOURCE || manifestTokenType == ManifestTokenType.VENDOR_NAME || manifestTokenType == ManifestTokenType.ARTIFACT_TYPE)) {
                break;
            }
            if (manifestTokenType == ManifestTokenType.SOURCE) {
                final String value = readCurrentEntryValue().orElse(null);
                if (!StringUtils.isEmpty(value)) {
                    nonManoSourceList.add(value);
                } else {
                    reportError(Messages.MANIFEST_EMPTY_NON_MANO_SOURCE);
                    break;
                }
            }
            readNextNonEmptyLine();
        }
        return nonManoSourceList;
    }

    /**
     * Validates the manifest metadata content, reporting errors found.
     *
     * @return {@code true} if the metadata content is valid, {@code false} otherwise.
     */
    protected boolean validateMetadata() {
        if (metadata.isEmpty()) {
            reportError(Messages.MANIFEST_NO_METADATA);
            return false;
        }
        String key = metadata.keySet().stream().filter(k -> !COMPATIBLE_SPECIFICATION_VERSIONS.getToken().equals(k)).findFirst().orElse(null);
        final ManifestTokenType firstManifestEntryTokenType = ManifestTokenType.parse(key).orElse(null);
        if (firstManifestEntryTokenType == null) {
            reportError(Messages.MANIFEST_METADATA_INVALID_ENTRY1, key);
            return false;
        }
        for (final Map.Entry<String, String> manifestEntry : metadata.entrySet()) {
            final ManifestTokenType manifestEntryTokenType = ManifestTokenType.parse(manifestEntry.getKey()).orElse(null);
            if (manifestEntryTokenType == null) {
                reportError(Messages.MANIFEST_METADATA_INVALID_ENTRY1, manifestEntry.getKey());
                return false;
            }
        }
        if (metadata.entrySet().size() != getMaxAllowedManifestMetaEntries()) {
            reportError(Messages.MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT, getMaxAllowedManifestMetaEntries());
            return false;
        }
        return true;
    }

    private static Manifest loadAsdManifest(final FileContentHandler handler) throws IOException {
        final Manifest manifest;
        try {
            manifest = getAsdManifest(handler);
        } catch (final IOException ex) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("An error occurred while getting the manifest file", ex);
            }
            throw ex;
        }
        return manifest;
    }

    private static Manifest getAsdManifest(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = getAsdMetadata(handler);
        return null != metadata ? getAsdManifest(handler, getEntryManifestLocation(metadata)) : null;
    }

    private static String getEntryManifestLocation(final ToscaMetadata metadata) {
        return metadata.getMetaEntries().containsKey(ETSI_ENTRY_MANIFEST.getName()) ?
                metadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName()):
                metadata.getMetaEntries().get(ENTRY_MANIFEST.getName());
    }

    private static Manifest getAsdManifest(FileContentHandler handler, String manifestLocation) throws IOException {
        try (InputStream manifestInputStream = getAsdManifestInputStream(handler, manifestLocation)) {
            Manifest onboardingManifest = new AsdManifestOnboarding();
            onboardingManifest.parse(manifestInputStream);
            return onboardingManifest;
        }
    }

    private static InputStream getAsdManifestInputStream(FileContentHandler handler, String manifestLocation) throws IOException {
        InputStream io;
        if (manifestLocation == null || !handler.containsFile(manifestLocation)) {
            io = handler.getFileContentAsStream(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME);
        } else {
            io = handler.getFileContentAsStream(manifestLocation);
        }
        return io;
    }

    private static ToscaMetadata getAsdMetadata(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata = null;
        if (handler.containsFile(TOSCA_META_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } else if (handler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_ORIG_PATH_FILE_NAME));
        }
        return metadata;
    }
}
