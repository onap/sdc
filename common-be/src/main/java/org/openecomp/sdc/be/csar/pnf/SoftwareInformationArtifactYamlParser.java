/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.be.csar.pnf;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareInformation.PnfSoftwareInformationField;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareVersion.PnfSoftwareVersionField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the parsing of the non-mano software information file.
 */
public class SoftwareInformationArtifactYamlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareInformationArtifactYamlParser.class);

    private SoftwareInformationArtifactYamlParser() {

    }

    /**
     * Parses the non-mano software information yaml file.
     *
     * @param softwareInformationYamlFileBytes the file byte array
     * @return an {@code Optional<PnfSoftwareInformation>} if the file was successful parsed, otherwise {@code
     * Optional.empty()}
     */
    @SuppressWarnings("unchecked")
    public static Optional<PnfSoftwareInformation> parse(final byte[] softwareInformationYamlFileBytes) {
        final Map<String, Object> softwareVersionYamlObject;
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(softwareInformationYamlFileBytes)) {
            final Object yaml = YamlUtil.read(byteArrayInputStream);
            if (!(yaml instanceof Map)) {
                return Optional.empty();
            }

            softwareVersionYamlObject = (Map<String, Object>) yaml; // unchecked warning suppressed
        } catch (final Exception ex) {
            LOGGER.warn("Could not parse the software information yaml file", ex);
            return Optional.empty();
        }

        final PnfSoftwareInformation pnfSoftwareInformation = new PnfSoftwareInformation();
        pnfSoftwareInformation.setDescription(
            (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.DESCRIPTION.getFieldName()));
        pnfSoftwareInformation.setProvider(
            (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.PROVIDER.getFieldName()));
        pnfSoftwareInformation.setVersion(
            (String) softwareVersionYamlObject.get(PnfSoftwareInformationField.VERSION.getFieldName()));
        final List<Map<String, String>> pnfSoftwareInformationYaml = (List<Map<String, String>>) softwareVersionYamlObject
            .get(PnfSoftwareInformationField.PNF_SOFTWARE_INFORMATION.getFieldName()); // unchecked warning suppressed

        pnfSoftwareInformationYaml.forEach(stringStringMap -> {
            final String description = stringStringMap.get(PnfSoftwareVersionField.DESCRIPTION.getFieldName());
            final String version = stringStringMap.get(PnfSoftwareVersionField.PNF_SOFTWARE_VERSION.getFieldName());
            pnfSoftwareInformation.addToSoftwareVersionSet(new PnfSoftwareVersion(version, description));
        });

        return Optional.of(pnfSoftwareInformation);
    }


}
