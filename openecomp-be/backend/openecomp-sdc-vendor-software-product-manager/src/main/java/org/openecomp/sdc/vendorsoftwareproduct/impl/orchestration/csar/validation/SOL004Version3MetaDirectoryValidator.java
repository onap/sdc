/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA_LIMIT_VERSION_3;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_PNF_METADATA_VERSION_3;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_VNF_METADATA_LIMIT_VERSION_3;
import static org.openecomp.sdc.tosca.csar.CSARConstants.MANIFEST_VNF_METADATA_VERSION_3;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.ToscaMetaEntry;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;

/**
 * Validates the contents of the package to ensure it complies with the "CSAR with TOSCA-Metadata directory" structure as defined in ETSI GS NFV-SOL
 * 004 v3.3.1.
 */
class SOL004Version3MetaDirectoryValidator extends SOL004MetaDirectoryValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOL004Version3MetaDirectoryValidator.class);

    public SOL004Version3MetaDirectoryValidator() {
        super();
    }

    SOL004Version3MetaDirectoryValidator(final SecurityManager securityManager) {
        super(securityManager);
    }

    @Override
    protected void handleOtherEntry(final Map.Entry<String, String> entry) {
        if (!ToscaMetaEntry.OTHER_DEFINITIONS.getName().equals(entry.getKey())) {
            reportError(ErrorLevel.ERROR, Messages.METADATA_UNSUPPORTED_ENTRY.formatMessage(entry.getKey()));
            LOGGER.warn(Messages.METADATA_UNSUPPORTED_ENTRY.getErrorMessage(), entry.getKey());
        } else {
            validateDefinitionFile(entry.getValue());
        }
    }

    @Override
    protected boolean validMetaLimit(Map<String, String> metadata) {
        int maxAllowedEntries = isPnfMetadata(metadata) ? MANIFEST_PNF_METADATA_LIMIT_VERSION_3 : MANIFEST_VNF_METADATA_LIMIT_VERSION_3;
        return metadata.size() == maxAllowedEntries;
    }

    @Override
    protected ImmutableSet<String> getManifestMetadata(final Map<String, String> metadata) {
        return isPnfMetadata(metadata) ? MANIFEST_PNF_METADATA_VERSION_3 : MANIFEST_VNF_METADATA_VERSION_3;
    }

    @Override
    protected boolean isPnfMetadata(final Map<String, String> metadata) {
        List<String> keys = metadata.keySet().stream().collect(Collectors.toList());
        //Both VNF and PNF share this attribute
        return validatorUtils.isPnfMetadata(keys);
    }
}
