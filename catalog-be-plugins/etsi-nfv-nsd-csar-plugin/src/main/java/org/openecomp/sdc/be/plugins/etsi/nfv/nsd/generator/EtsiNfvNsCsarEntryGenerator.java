/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator;

import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ETSI_PACKAGE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.CategoriesToGenerateNsd;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.CsarEntryGenerator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory.EtsiNfvNsdCsarGeneratorFactory;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a Network Service CSAR based on a SERVICE component and wraps it in a SDC CSAR entry.
 */
@org.springframework.stereotype.Component("etsiNfvNsCsarEntryGenerator")
public class EtsiNfvNsCsarEntryGenerator implements CsarEntryGenerator {

    static final String NSD_FILE_PATH_FORMAT = "Artifacts/%s/%s.%s";
    static final String SIGNED_CSAR_EXTENSION = "zip";
    static final String UNSIGNED_CSAR_EXTENSION = "csar";
    static final String ETSI_VERSION_METADATA = "ETSI Version";
    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiNfvNsCsarEntryGenerator.class);
    private final EtsiNfvNsdCsarGeneratorFactory etsiNfvNsdCsarGeneratorFactory;

    public EtsiNfvNsCsarEntryGenerator(final EtsiNfvNsdCsarGeneratorFactory etsiNfvNsdCsarGeneratorFactory) {
        this.etsiNfvNsdCsarGeneratorFactory = etsiNfvNsdCsarGeneratorFactory;
    }

    /**
     * Generates a Network Service CSAR based on a SERVICE component that has category configured in
     * {@link CategoriesToGenerateNsd } enum and wraps it in a SDC CSAR entry.
     *
     * @param component the component to create the NS CSAR from
     * @return an entry to be added in the Component CSAR by SDC
     */
    @Override
    public Map<String, byte[]> generateCsarEntries(final Component component) {
        final String componentName = component == null ? "null" : component.getName();
        if (component == null || ComponentTypeEnum.SERVICE != component.getComponentType()) {
            LOGGER.debug("Ignoring NSD CSAR generation for component '{}' as it is not a SERVICE", componentName);
            return Collections.emptyMap();
        }
        final boolean isEOTemplate = component.getCategories().stream().anyMatch(category ->
                CategoriesToGenerateNsd.hasCategoryName(category.getName()));
        if (!isEOTemplate) {
            LOGGER.debug("Ignoring NSD CSAR generation for component '{}' as it does not belong to any of the categories '{}'",
                    componentName, CategoriesToGenerateNsd.getCategories());
            return Collections.emptyMap();
        }

        final NsdCsar nsdCsar;
        try {
            final EtsiVersion etsiVersion = getComponentEtsiVersion(component);
            final EtsiNfvNsdCsarGenerator etsiNfvNsdCsarGenerator = etsiNfvNsdCsarGeneratorFactory.create(etsiVersion);
            nsdCsar = etsiNfvNsdCsarGenerator.generateNsdCsar(component);
        } catch (final NsdException e) {
            LOGGER.error("Could not create NSD CSAR entry for component '{}'", component.getName(), e);
            return Collections.emptyMap();
        } catch (final Exception e) {
            LOGGER.error("Could not create NSD CSAR entry for component '{}'. An unexpected exception occurred", component.getName(), e);
            return Collections.emptyMap();
        }

        return createEntry(nsdCsar);
    }

    private EtsiVersion getComponentEtsiVersion(Component component) {
        String etsiVersion = component.getCategorySpecificMetadata().get(ETSI_VERSION_METADATA);
        final String modelName = component.getModel();
        if (etsiVersion == null && modelName.matches(".*\\d+\\.\\d+\\.\\d+.*" )){
            etsiVersion = modelName.replaceAll(".*?(\\d+\\.\\d+\\.\\d+).*", "$1");
        }
        return EtsiVersion.convertOrNull(etsiVersion);
    }

    private Map<String, byte[]> createEntry(final NsdCsar nsdCsar) {
        final Map<String, byte[]> entryMap = new HashMap<>();
        final String extension = nsdCsar.isSigned() ? SIGNED_CSAR_EXTENSION : UNSIGNED_CSAR_EXTENSION;
        final String entryKey = String.format(NSD_FILE_PATH_FORMAT, ETSI_PACKAGE, nsdCsar.getFileName(), extension);
        entryMap.put(entryKey, nsdCsar.getCsarPackage());
        return entryMap;
    }
}
