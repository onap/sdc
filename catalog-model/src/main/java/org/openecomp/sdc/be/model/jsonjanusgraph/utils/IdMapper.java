package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

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

            Optional<ComponentInstanceDataDefinition> componentInstanceDataDefinitionOptional = null;
            if (fromCompName) {
                componentInstanceDataDefinitionOptional = compositionDataDefinition.getComponentInstances().values().stream().filter(c -> c.getNormalizedName().equals(componentUniqueIdOrName)).findAny();
                result = componentInstanceDataDefinitionOptional.get().getUniqueId();
                log.debug("Compponent Instance Unique Id = {}", result);
            } else {
                componentInstanceDataDefinitionOptional = compositionDataDefinition.getComponentInstances().values().stream().filter(c -> c.getUniqueId().equals(componentUniqueIdOrName)).findAny();
                result = componentInstanceDataDefinitionOptional.get().getNormalizedName();
                log.debug("Compponent Instance Normalized Name = {}", result);
            }

        } catch (Exception e) {
            log.error("Failed to map UUID or Normalized name of " + componentUniqueIdOrName, e);
        }
        return result;
    }

}
