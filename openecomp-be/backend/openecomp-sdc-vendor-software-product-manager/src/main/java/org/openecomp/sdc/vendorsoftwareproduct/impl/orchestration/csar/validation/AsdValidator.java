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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import com.google.common.collect.ImmutableSet;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AbstractOnboardingManifest;
import org.openecomp.sdc.tosca.csar.AsdManifestOnboarding;
import org.openecomp.sdc.tosca.csar.ToscaMetaEntryAsd;

import java.util.Map;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ASD_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_ASD_METADATA;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion251.ENTRY_MANIFEST;

/**
 * Validates the contents of the package to ensure it complies with the ASD specifications
 */
@NoArgsConstructor
public class AsdValidator extends SOL004MetaDirectoryValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsdValidator.class);

    protected <T extends AbstractOnboardingManifest> T getOnboardingManifest() {
        return (T) new AsdManifestOnboarding();
    }

    @Override
    protected void handleEntry(final Map.Entry<String, String> entry) {
        final String key = entry.getKey();
        final var toscaMetaEntry = ToscaMetaEntryAsd.parse(entry.getKey()).orElse(null);
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
            case ETSI_ENTRY_MANIFEST:
                validateManifestFile(value);
                break;
            case ETSI_ENTRY_CHANGE_LOG:
                validateChangeLog(value);
                break;
            case ETSI_ENTRY_TESTS:
            case ETSI_ENTRY_LICENSES:
                validateOtherEntries(entry);
                break;
            case ETSI_ENTRY_CERTIFICATE:
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


    protected boolean isValidEntryDefinitionType(final String key, final String value) {
        if (!ASD_DEFINITION_TYPE.equalsIgnoreCase(value)) {
            reportMetadataInvalidValue(key, value);
            return false;
        }
        return true;
    }

    private void reportMetadataInvalidValue(final String key, final String value) {
        reportError(ErrorLevel.ERROR, String.format(Messages.MANIFEST_INVALID_LINE.getErrorMessage(), key, value));
        LOGGER.error("{}: key {} - value {} ", Messages.MANIFEST_INVALID_LINE.getErrorMessage(), key, value);
    }

    @Override
    protected ImmutableSet<String> getManifestMetadata(final Map<String, String> metadata) {
        return MANIFEST_ASD_METADATA;
    }

    @Override
    protected void handleMetadataEntries(final Map<String, String> metadata) {
        getManifestMetadata(metadata).stream().filter(requiredEntry -> !metadata.containsKey(requiredEntry) ||
                (ENTRY_DEFINITION_TYPE.getToken().equalsIgnoreCase(requiredEntry)
                        && !isValidEntryDefinitionType(requiredEntry, metadata.get(requiredEntry)))).forEach(
                requiredEntry -> reportError(ErrorLevel.ERROR,
                        String.format(Messages.MANIFEST_METADATA_MISSING_ENTRY.getErrorMessage(), requiredEntry)));
    }

}
