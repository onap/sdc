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

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;

/**
 * Generator for a ETSI NFV NSD CSAR
 */
public interface EtsiNfvNsdCsarGenerator {

    /**
     * Generates the ETSI NFV Network Service Descriptor based on a SERVICE SDC component.
     *
     * @param component the service component
     * @return the CSAR package content
     */
    byte[] generateNsdCsar(Component component) throws NsdException;

}
