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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_MANIFEST_FILE_EXT;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_CERTIFICATE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_MANIFEST;

import java.util.Map;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure as defined in ETSI GS NFV-SOL
 * 004 v2.5.1.
 */
@NoArgsConstructor
public class EtsiSol004Version251Validator extends SOL004MetaDirectoryValidator {

    @Override
    public boolean appliesTo(final String model) {
        return "ETSI SOL001 v2.5.1".equals(model);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected Optional<String> getCertificatePath() {
        return getToscaMetadata().getEntry(ENTRY_CERTIFICATE);
    }

    @Override
    protected void handleEntry(final Map.Entry<String, String> entry) {
        final String key = entry.getKey();
        final var toscaMetaEntry = ToscaMetaEntryVersion251.parse(entry.getKey()).orElse(null);
        // allows any other unknown entry
        if (toscaMetaEntry == null) {
            return;
        }
        final String value = entry.getValue();
        switch (toscaMetaEntry) {
            case TOSCA_META_FILE_VERSION_ENTRY:
            case CSAR_VERSION_ENTRY:
            case CREATED_BY_ENTRY:
                verifyMetadataEntryVersions(key, value);
                break;
            case ENTRY_DEFINITIONS:
                validateDefinitionFile(value);
                break;
            case ENTRY_MANIFEST:
                validateManifestFile(value, new SOL004ManifestOnboarding());
                break;
            case ENTRY_CHANGE_LOG:
                validateChangeLog(value);
                break;
            case ENTRY_TESTS:
            case ENTRY_LICENSES:
                validateOtherEntries(entry);
                break;
            case ENTRY_CERTIFICATE:
                validateCertificate(value);
                break;
            default:
                handleOtherEntry(entry);
                break;
        }
    }

    @Override
    protected String getManifestFilePath() {
        return getToscaMetadata().getMetaEntries().get(ENTRY_MANIFEST.getName());
    }

    @Override
    protected void verifyManifestNameAndExtension() {
        final Map<String, String> entries = getToscaMetadata().getMetaEntries();
        final String manifestFileName = getFileName(entries.get(ENTRY_MANIFEST.getName()));
        final String manifestExtension = getFileExtension(entries.get(ENTRY_MANIFEST.getName()));
        final String mainDefinitionFileName = getFileName(entries.get(ENTRY_DEFINITIONS.getName()));
        if (!(TOSCA_MANIFEST_FILE_EXT).equals(manifestExtension)) {
            reportError(ErrorLevel.ERROR, Messages.MANIFEST_INVALID_EXT.getErrorMessage());
        }
        if (!mainDefinitionFileName.equals(manifestFileName)) {
            reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_NAME.getErrorMessage(), manifestFileName, mainDefinitionFileName));
        }
    }

}
