/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.helper.FunctionTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.Optional;

/**
 * @author SHIRIA
 * @since December 15, 2016.
 */
public class FunctionTranslationGetResourceImpl implements FunctionTranslation {
  @Override
  public Object translateFunction(ServiceTemplate serviceTemplate, String resourceId,
                                  String propertyName, String functionKey,
                                  Object functionValue, String heatFileName,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  Template toscaTemplate, TranslationContext context) {
    Object returnValue;
    Optional<String> resourceTranslatedId = ResourceTranslationBase
        .getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, (String) functionValue,
            context);
    returnValue = resourceTranslatedId
        .orElseGet(() -> FunctionTranslationHelper.getUnsupportedResourcePrefix() + functionValue);
    return returnValue;
  }
}
