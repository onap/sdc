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

package org.openecomp.core.impl;

import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.impl.pnfd.PnfdNodeTemplateTransformationEngine;
import org.openecomp.core.converter.impl.pnfd.PnfdNodeTypeTransformationEngine;
import org.openecomp.core.converter.pnfd.PnfdTransformationEngine;

public class ToscaSolModelDrivenConverterPnf extends AbstractToscaSolConverter {

    /**
     * Calls the model driven engine to parse the onboarding PNF descriptor
     * @param serviceTemplate
     * @param readerService
     */
    @Override
    public void convertTopologyTemplate(final ServiceTemplate serviceTemplate,
                                        final ServiceTemplateReaderService readerService) {
        final PnfdTransformationEngine pnfdTransformationEngine =
            new PnfdNodeTemplateTransformationEngine(readerService, serviceTemplate);
        pnfdTransformationEngine.transform();
    }

    @Override
    protected void convertNodeTypes(final ServiceTemplate serviceTemplate,
                                    final ServiceTemplateReaderService readerService) {
        final PnfdTransformationEngine pnfdTransformationEngine =
            new PnfdNodeTypeTransformationEngine(readerService, serviceTemplate);
        pnfdTransformationEngine.transform();
    }
}