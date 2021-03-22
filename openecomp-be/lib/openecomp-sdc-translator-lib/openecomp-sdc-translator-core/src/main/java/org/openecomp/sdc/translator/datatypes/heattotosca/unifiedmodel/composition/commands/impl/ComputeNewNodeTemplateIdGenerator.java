/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl;

import java.util.Optional;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil;

public class ComputeNewNodeTemplateIdGenerator implements UnifiedSubstitutionNodeTemplateIdGenerator {

    @Override
    public Optional<String> generate(UnifiedCompositionTo unifiedCompositionTo, String originalNodeTemplateId) {
        return Optional
            .ofNullable(UnifiedCompositionUtil.getNewComputeNodeTemplateId(unifiedCompositionTo.getServiceTemplate(), originalNodeTemplateId));
    }
}
