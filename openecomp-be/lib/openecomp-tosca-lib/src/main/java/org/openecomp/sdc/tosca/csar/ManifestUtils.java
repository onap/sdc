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

public class ManifestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestUtils.class);

    public static <T extends AbstractOnboardingManifest> Manifest loadManifest(final FileContentHandler handler, final T abstractManifest) throws IOException {
        final Manifest manifest;
        try {
            manifest = getManifest(handler, abstractManifest);
        } catch (final IOException ex) {
            LOGGER.error("An error occurred while getting the manifest file", ex);
            throw ex;
        }
        return manifest;
    }

    public static <T extends AbstractOnboardingManifest> Manifest getManifest(FileContentHandler handler, T manifest) throws IOException {
        ToscaMetadata metadata = getMetadata(handler);
        return getManifest(handler, getEntryManifestLocation(metadata), manifest);
    }

    public static ToscaMetadata getMetadata(FileContentHandler handler) throws IOException {
        ToscaMetadata metadata;
        if (handler.containsFile(TOSCA_META_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
        } else if (handler.containsFile(TOSCA_META_ORIG_PATH_FILE_NAME)) {
            metadata = OnboardingToscaMetadata.parseToscaMetadataFile(handler.getFileContentAsStream(TOSCA_META_ORIG_PATH_FILE_NAME));
        } else {
            throw new IOException("TOSCA.meta file not found!");
        }
        return metadata;
    }
    public static String getEntryManifestLocation(final ToscaMetadata metadata) {
        return metadata.getMetaEntries().containsKey(ETSI_ENTRY_MANIFEST.getName()) ?
                metadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName()):
                metadata.getMetaEntries().get(ENTRY_MANIFEST.getName());
    }

    public static <T extends AbstractOnboardingManifest> Manifest getManifest(FileContentHandler handler, String manifestLocation, T manifest) throws IOException {
        try (InputStream manifestInputStream = getManifestInputStream(handler, manifestLocation)) {
            Manifest onboardingManifest = manifest;
            onboardingManifest.parse(manifestInputStream);
            return onboardingManifest;
        }
    }

    private static InputStream getManifestInputStream(FileContentHandler handler, String manifestLocation) throws IOException {
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
