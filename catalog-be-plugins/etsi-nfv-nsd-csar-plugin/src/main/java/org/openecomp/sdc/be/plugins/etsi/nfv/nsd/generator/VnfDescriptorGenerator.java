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

import java.util.Optional;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.VnfDescriptorException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;

/**
 * Generator of a VNF Descriptor from the ONBOARDED_PACKAGE
 */
public interface VnfDescriptorGenerator {

    /**
     * Generates the a VNF Descriptor based on the ONBOARDED_PACKAGE artifact.
     *
     * @param name the name of the VNF package
     * @param onboardedPackageArtifact the onboarded package for the VNF
     * @return a representation of the VNF package
     * @throws VnfDescriptorException when a problem happens during the generation
     */
    Optional<VnfDescriptor> generate(final String name, final ArtifactDefinition onboardedPackageArtifact)
        throws VnfDescriptorException;

}
