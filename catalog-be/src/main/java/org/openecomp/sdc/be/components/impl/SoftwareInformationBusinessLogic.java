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

package org.openecomp.sdc.be.components.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareInformation;
import org.openecomp.sdc.be.csar.pnf.PnfSoftwareVersion;
import org.openecomp.sdc.be.csar.pnf.SoftwareInformationArtifactYamlParser;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.openecomp.sdc.be.components.impl.ImportUtils.getPropertyJsonStringValue;

@Component("softwareInformationBusinessLogic")
public class SoftwareInformationBusinessLogic {

    private final PropertyBusinessLogic propertyBusinessLogic;
    private static final String SOFTWARE_VERSION_PROPERTY_NAME = "software_versions";

    @Autowired
    public SoftwareInformationBusinessLogic(final PropertyBusinessLogic propertyBusinessLogic) {
        this.propertyBusinessLogic = propertyBusinessLogic;
    }

    /**
     * Adds the software information from a csar package to the resource {@link SoftwareInformationBusinessLogic#SOFTWARE_VERSION_PROPERTY_NAME}
     * property.<br/> The csar package must contain the expected non-mano yaml file with the software information. Also
     * the resource must have the {@link SoftwareInformationBusinessLogic#SOFTWARE_VERSION_PROPERTY_NAME} property.
     *
     * @param resource the resource to add the software information
     * @param csarInfo the csar package representation
     * @return if the expected property exists in the resource and the csar package contains the software information
     * file, an Optional<PropertyDefinition> with the updated property; otherwise Optional.empty().
     * @throws BusinessLogicException when there was a problem while updating the property
     */
    public Optional<PropertyDefinition> setSoftwareInformation(final Resource resource,
                                                               final CsarInfo csarInfo) throws BusinessLogicException {
        final Optional<String> softwareInformation = csarInfo.getSoftwareInformationPath();
        if (!softwareInformation.isPresent()) {
            return Optional.empty();
        }
        final PropertyDefinition propertyDefinition = findSoftwareVersionPropertyDefinition(resource).orElse(null);
        if (propertyDefinition == null) {
            return Optional.empty();
        }
        final byte[] softwareInformationYaml = csarInfo.getCsar().get(softwareInformation.get());
        final PnfSoftwareInformation pnfSoftwareInformation =
            parseSoftwareInformation(softwareInformationYaml).orElse(null);
        if (pnfSoftwareInformation == null) {
            return Optional.empty();
        }

        final List<String> versionList = pnfSoftwareInformation.getSoftwareVersionSet().stream()
            .map(PnfSoftwareVersion::getVersion).collect(toList());
        final String softwareVersionInformation =
            getPropertyJsonStringValue(versionList, ToscaPropertyType.LIST.getType());
        propertyDefinition.setValue(softwareVersionInformation);

        final PropertyDefinition updatedPropertyDefinition =
            propertyBusinessLogic.updateComponentProperty(resource.getUniqueId(), propertyDefinition);
        return Optional.ofNullable(updatedPropertyDefinition);
    }

    /**
     * Parses the non-mano software information yaml file.
     *
     * @param softwareInformationYaml the file byte array
     * @return an {@code Optional<PnfSoftwareInformation>} if the file was successful parsed, otherwise {@code
     * Optional.empty()}
     */
    private Optional<PnfSoftwareInformation> parseSoftwareInformation(byte[] softwareInformationYaml) {
        return SoftwareInformationArtifactYamlParser.parse(softwareInformationYaml);
    }

    /**
     * Finds the {@link SoftwareInformationBusinessLogic#SOFTWARE_VERSION_PROPERTY_NAME} property in a Resource
     * @param resource the resource to search for the property
     * @return an {@code Optional<PnfSoftwareInformation>} if the property was found, otherwise {@code Optional.empty()}
     */
    private Optional<PropertyDefinition> findSoftwareVersionPropertyDefinition(final Resource resource) {
        if (CollectionUtils.isEmpty(resource.getProperties())) {
            return Optional.empty();
        }
        return resource.getProperties().stream()
            .filter(propertyDefinition -> propertyDefinition.getName().equals(SOFTWARE_VERSION_PROPERTY_NAME))
            .findFirst();
    }

    /**
     * Removes the non-mano software information file from the csar package
     *
     * @param csarInfo the csar package representation
     * @return {@code true} if the file was removed, otherwise {@code false}
     */
    public boolean removeSoftwareInformationFile(final CsarInfo csarInfo) {
        final Optional<String> softwareInformation = csarInfo.getSoftwareInformationPath();
        if (!softwareInformation.isPresent()) {
            return false;
        }

        csarInfo.getCsar().remove(softwareInformation.get());
        return true;
    }
}
