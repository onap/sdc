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
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.CsarEntryGenerator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a Network Service CSAR based on a SERVICE component and wraps it in a SDC CSAR entry.
 */
@org.springframework.stereotype.Component("etsiNfvNsCsarEntryGenerator")
public class EtsiNfvNsCsarEntryGenerator implements CsarEntryGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiNfvNsCsarEntryGenerator.class);
    static final String ETSI_NS_COMPONENT_CATEGORY = "ETSI Network Service";
    static final String NSD_FILE_PATH_FORMAT = "Artifacts/%s/%s.csar";

    private final EtsiNfvNsdCsarGenerator etsiNfvNsdCsarGenerator;

    public EtsiNfvNsCsarEntryGenerator(final EtsiNfvNsdCsarGenerator etsiNfvNsdCsarGenerator) {
        this.etsiNfvNsdCsarGenerator = etsiNfvNsdCsarGenerator;
    }

    /**
     * Generates a Network Service CSAR based on a SERVICE component of category {@link
     * EtsiNfvNsCsarEntryGenerator#ETSI_NS_COMPONENT_CATEGORY} and wraps it in a SDC CSAR entry.
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

        final boolean isEOTemplate = component.getCategories().stream()
            .anyMatch(category -> ETSI_NS_COMPONENT_CATEGORY.equals(category.getName()));
        if (!isEOTemplate) {
            LOGGER.debug("Ignoring NSD CSAR generation for component '{}' as it does not belong to the category '{}'",
                componentName, ETSI_NS_COMPONENT_CATEGORY);
            return Collections.emptyMap();
        }

        final byte[] nsdCsar;
        try {
            nsdCsar = etsiNfvNsdCsarGenerator.generateNsdCsar(component);
        } catch (final NsdException e) {
            LOGGER.error("Could not create NSD CSAR entry for component '{}'"
                , component.getName(), e);
            return Collections.emptyMap();
        } catch (final Exception e) {
            LOGGER.error("Could not create NSD CSAR entry for component '{}'. An unexpected exception occurred"
                , component.getName(), e);
            return Collections.emptyMap();
        }

        return createEntry(component.getNormalizedName(), nsdCsar);
    }

    private Map<String, byte[]> createEntry(final String csarName, final byte[] nsdCsar) {
        final Map<String, byte[]> entryMap = new HashMap<>();
        final String entryKey = String.format(NSD_FILE_PATH_FORMAT, ETSI_PACKAGE, csarName);
        entryMap.put(entryKey, nsdCsar);
        return entryMap;
    }
}
