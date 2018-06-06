package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Component
public class PolicyOperation {

    private static final Logger log = LoggerFactory.getLogger(PolicyOperation.class);
    private TopologyTemplateOperation topologyTemplateOperation;
    private TitanDao titanDao;

    public PolicyOperation(TopologyTemplateOperation topologyTemplateOperation, TitanDao titanDao) {
        this.topologyTemplateOperation = topologyTemplateOperation;
        this.titanDao = titanDao;
    }

    /**
     * updates a list of policy properties by overriding the existing ones with the same name
     * @param containerComponent the container of the policy of which its properties are to be updated
     * @param policyId the id of the policy of which its properties are to be updated
     * @param propertiesToUpdate the policy properties to update
     * @return the update operation status
     */
    public StorageOperationStatus updatePolicyProperties(Component containerComponent, String policyId, List<PropertyDataDefinition> propertiesToUpdate) {
        log.debug("#updatePolicyProperties - updating the properties of policy {} in component {}", policyId, containerComponent.getUniqueId());
        PolicyDefinition policy = containerComponent.getPolicyById(policyId);
        return titanDao.getVertexById(containerComponent.getUniqueId(), JsonParseFlagEnum.NoParse)
                .either(containerVertex -> updatePolicyProperties(containerVertex, policy, propertiesToUpdate),
                        DaoStatusConverter::convertTitanStatusToStorageStatus);
    }

    private StorageOperationStatus updatePolicyProperties(GraphVertex container, PolicyDefinition policy, List<PropertyDataDefinition> propertiesToUpdate) {
        List<PropertyDataDefinition> policyProperties = policy.getProperties();
        List<PropertyDataDefinition> updatedPolicyProperties = updatePolicyProperties(policyProperties, propertiesToUpdate);
        policy.setProperties(updatedPolicyProperties);
        return topologyTemplateOperation.updatePolicyOfToscaElement(container, policy);
    }

    private List<PropertyDataDefinition> updatePolicyProperties(List<PropertyDataDefinition> currentPolicyProperties, List<PropertyDataDefinition> toBeUpdatedProperties) {
        Map<String, PropertyDataDefinition> currPropsByName = MapUtil.toMap(currentPolicyProperties, PropertyDataDefinition::getName);
        overrideCurrentPropertiesWithUpdatedProperties(currPropsByName, toBeUpdatedProperties);
        return new ArrayList<>(currPropsByName.values());
    }

    private void overrideCurrentPropertiesWithUpdatedProperties(Map<String, PropertyDataDefinition> currPropsByName, List<PropertyDataDefinition> toBeUpdatedProperties) {
        toBeUpdatedProperties.forEach(prop -> currPropsByName.put(prop.getName(), prop));
    }

}
