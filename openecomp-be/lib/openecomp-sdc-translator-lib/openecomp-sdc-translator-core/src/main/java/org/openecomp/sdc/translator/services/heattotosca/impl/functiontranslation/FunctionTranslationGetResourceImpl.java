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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.Optional;

public class FunctionTranslationGetResourceImpl implements FunctionTranslation {
    @Override
    public Object translateFunction(FunctionTranslator functionTranslator) {
        Object returnValue;
        Optional<String> resourceTranslatedId = ResourceTranslationBase.getResourceTranslatedId(functionTranslator
                .getHeatFileName(), functionTranslator.getHeatOrchestrationTemplate(),
                (String) functionTranslator.getFunctionValue(), functionTranslator.getContext());
        returnValue = resourceTranslatedId.orElseGet(() -> functionTranslator.getUnsupportedResourcePrefix()
                + functionTranslator.getFunctionValue());
        return returnValue;
    }
}
