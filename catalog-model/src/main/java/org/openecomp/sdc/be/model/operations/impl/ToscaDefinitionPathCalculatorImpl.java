package org.openecomp.sdc.be.model.operations.impl;

import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.operations.api.ToscaDefinitionPathCalculator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component("tosca-path-calculator")
public class ToscaDefinitionPathCalculatorImpl implements ToscaDefinitionPathCalculator {

    @Override
    public List<String> calculateToscaDefinitionPath(ComponentInstance componentInstance, GraphEdge edge) {
        String ownerId = getCapReqOwner(edge);
        String instanceId = componentInstance.getUniqueId();
        return ownerId.equals(instanceId) ? Collections.singletonList(instanceId) : Arrays.asList(instanceId, ownerId);
    }

    private String getCapReqOwner(GraphEdge edge) {
        String ownerIdKey = GraphEdgePropertiesDictionary.OWNER_ID.getProperty();
        return (String)edge.getProperties().get(ownerIdKey);
    }
}
