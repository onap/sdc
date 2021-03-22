/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.translator.services.heattotosca.impl.unifiedcomposition;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedComposition;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

public class UnifiedCompositionNestedSingleCompute implements UnifiedComposition {
    // There is no consolidation in NestedSingleCompute implementation.

    // In case of  nested single compute, if there is more than one entry in the

    // unifiedCompositionDataList, each one should be handed separately, no consolidation between

    // them.
    @Override
    public void createUnifiedComposition(ServiceTemplate serviceTemplate, ServiceTemplate nestedServiceTemplate,
                                         List<UnifiedCompositionData> unifiedCompositionDataList, TranslationContext context) {
        UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();
        if (CollectionUtils.isEmpty(unifiedCompositionDataList)) {
            return;
        }
        UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate, nestedServiceTemplate, unifiedCompositionDataList,
            context, null);
        for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
            unifiedCompositionService.handleUnifiedNestedDefinition(unifiedCompositionTo, unifiedCompositionData);
            String nestedNodeTemplateId = unifiedCompositionData.getNestedTemplateConsolidationData().getNodeTemplateId();
            unifiedCompositionService.createNestedVfcInstanceGroup(nestedNodeTemplateId, unifiedCompositionTo, unifiedCompositionData);
            unifiedCompositionService.updateUnifiedNestedConnectivity(unifiedCompositionTo, unifiedCompositionData);
            unifiedCompositionService.cleanUnifiedNestedEntities(unifiedCompositionTo, unifiedCompositionData);
            unifiedCompositionService.updateSubstitutionNodeTypePrefix(nestedServiceTemplate);
        }
    }
}
