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
package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

/**
 * Created by yavivi on 12/02/2018.
 */
@Component
public class IdMapper {

    private static final Logger log = Logger.getLogger(ExternalReferencesOperation.class.getName());

    public String mapComponentNameToUniqueId(String componentInstanceName, GraphVertex serviceVertex) {
        return map(componentInstanceName, serviceVertex, true);
    }

    public String mapUniqueIdToComponentNameTo(String compUniqueId, GraphVertex serviceVertex) {
        return map(compUniqueId, serviceVertex, false);
    }

    private String map(String componentUniqueIdOrName, GraphVertex serviceVertex, boolean fromCompName) {
        String result = null;
        try {
            Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) serviceVertex.getJson();
            CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
            Optional<ComponentInstanceDataDefinition> componentInstanceDataDefinitionOptional;
            if (fromCompName) {
                componentInstanceDataDefinitionOptional = compositionDataDefinition.getComponentInstances().values().stream()
                    .filter(c -> c.getNormalizedName().equals(componentUniqueIdOrName)).findAny();
                if (componentInstanceDataDefinitionOptional.isPresent()) {
                    result = componentInstanceDataDefinitionOptional.get().getUniqueId();
                    log.debug("Component Instance Unique Id = {}", result);
                }
            } else {
                componentInstanceDataDefinitionOptional = compositionDataDefinition.getComponentInstances().values().stream()
                    .filter(c -> c.getUniqueId().equals(componentUniqueIdOrName)).findAny();
                if (componentInstanceDataDefinitionOptional.isPresent()) {
                    result = componentInstanceDataDefinitionOptional.get().getNormalizedName();
                    log.debug("Component Instance Normalized Name = {}", result);
                }
            }
        } catch (Exception e) {
            log.error(EcompLoggerErrorCode.DATA_ERROR, "Failed to map UUID or Normalized name of {}", componentUniqueIdOrName, e);
        }
        return result;
    }
}
