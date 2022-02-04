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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ATTRIBUTE_VALUE_SEPARATOR;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntryVersion261.OTHER_DEFINITIONS;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;

import org.openecomp.sdc.tosca.csar.ManifestBuilder;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManager;

public class SOL004Version3MetaDirectoryValidatorTest extends SOL004MetaDirectoryValidatorTest {

    @Override
    public SOL004MetaDirectoryValidator getSOL004MetaDirectoryValidator() {
        return new SOL004Version3MetaDirectoryValidator();
    }

    @Override
    public StringBuilder getMetaFileBuilder() {
        return super.getMetaFileBuilder().append(OTHER_DEFINITIONS.getName())
        .append(ATTRIBUTE_VALUE_SEPARATOR.getToken()).append(" ").append(TOSCA_DEFINITION_FILEPATH).append("\n");
    }

    @Override
    protected SOL004MetaDirectoryValidator getSol004WithSecurity(SecurityManager securityManagerMock) {
        return new SOL004Version3MetaDirectoryValidator(securityManagerMock);
    }

    @Override
    protected ManifestBuilder getVnfManifestSampleBuilder() {
        return super.getVnfManifestSampleBuilder()
            .withMetaData(ManifestTokenType.VNFD_ID.getToken(), "2116fd24-83f2-416b-bf3c-ca1964793aca")
            .withMetaData(ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS.getToken(), "2.7.1,3.3.1")
            .withMetaData(ManifestTokenType.VNF_SOFTWARE_VERSION.getToken(), "1.0.0")
            .withMetaData(ManifestTokenType.VNFM_INFO.getToken(), "etsivnfm:v2.3.1,0:myGreatVnfm-1");
    }

    @Override
    protected ManifestBuilder getPnfManifestSampleBuilder() {
        return super.getPnfManifestSampleBuilder()
            .withMetaData(ManifestTokenType.COMPATIBLE_SPECIFICATION_VERSIONS.getToken(), "2.7.1,3.3.1");
    }

    @Override
    protected int getManifestDefinitionErrorCount() {
        return 2;
    }
}
