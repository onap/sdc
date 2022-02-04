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

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.openecomp.sdc.tosca.csar.CSARConstants.MAIN_SERVICE_TEMPLATE_MF_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ORIG_PATH_FILE_NAME;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

/**
 * Offers method utils dealing with the manifest
 */
public class ManifestUtils {

    public ManifestUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestUtils.class);

    /**
     * Loads a manifest given the file handler and the type to manifest to load.
     *
     * @param fileContentHandler The package file handler
     * @param manifestHandler The type of abstract manifest to load
     * @return The loaded Manifest.
     */
    public <T extends AbstractOnboardingManifest> Manifest loadManifest(final FileContentHandler fileContentHandler, final T manifestHandler) throws IOException {
        final Manifest manifest;
        try {
            manifest = getManifest(fileContentHandler, manifestHandler);
        } catch (final IOException ex) {
            LOGGER.error("An error occurred while getting the manifest file", ex);
            throw ex;
        }
        return manifest;
    }

   /**
     * Retrieves the manifest file from the CSAR
     *
     * @param fileContentHandler contains csar artifacts
     * @param manifestHandler The type of abstract manifest to load
     * @return The retrieved Manifest
     * @throws IOException when TOSCA.meta file or manifest file is invalid
     */
    public <T extends AbstractOnboardingManifest> Manifest getManifest(FileContentHandler fileContentHandler, T manifestHandler) throws IOException {
        ToscaMetadata metadata = getMetadata(fileContentHandler);
        return getManifest(fileContentHandler, getEntryManifestLocation(metadata), manifestHandler);
    }

    /**
     * Retrieves the metadata from the CSAR
     *
     * @param fileContentHandler contains csar artifacts
     * @return The retrieved metadata
     * @throws IOException when TOSCA.meta file or manifest file is invalid
     */
    public ToscaMetadata getMetadata(FileContentHandler fileContentHandler) throws IOException {
        ToscaMetadata metadata;
        if (fileContentHandler.containsFile(TOSCA_META_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(fileContentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } else if (fileContentHandler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(fileContentHandler.getFileContentAsStream(TOSCA_META_ORIG_PATH_FILE_NAME));
        } else {
            throw new IOException("TOSCA.meta file not found!");
        }
        return metadata;
    }

    /**
     * Retrieves the manifest location present in the metadata within the CSAR
     *
     * @param metadata the CSAR metadata
     * @return The path of the location of the manifest within the CSAR
     */
    public String getEntryManifestLocation(final ToscaMetadata metadata) {
        return metadata.getMetaEntries().containsKey(ETSI_ENTRY_MANIFEST.getName()) ?
                metadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName()):
                metadata.getMetaEntries().get(ENTRY_MANIFEST.getName());
    }

    /**
     * Retrieves the manifest given the file handler, the manifest location within the CSAR,
     * and the type to manifest to load.
     *
     * @param fileContentHandler The package file handler
     * @param manifestLocation The path of the location of the manifest within the CSAR
     * @param manifestHandler The type of abstract manifest to load
     * @return The loaded Manifest.
     */
    public <T extends AbstractOnboardingManifest> Manifest getManifest(FileContentHandler fileContentHandler,
        String manifestLocation, T manifestHandler) throws IOException {
        try (InputStream manifestInputStream = getManifestInputStream(fileContentHandler, manifestLocation)) {
            manifestHandler.parse(manifestInputStream);
            return manifestHandler;
        }
    }

    private InputStream getManifestInputStream(FileContentHandler handler, String manifestLocation) throws IOException {
        InputStream io;
        if (manifestLocation == null || !handler.containsFile(manifestLocation)) {
            io = handler.getFileContentAsStream(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME);
        } else {
            io = handler.getFileContentAsStream(manifestLocation);
        }
        if (io == null) {
            throw new IOException("Manifest file not found!");
        }
        return io;
    }
}
